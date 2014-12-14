'''
Created on March 31, 2014

@author: justinoliver
'''
import argparse
import httplib2
import os
import sys
import json

import csv
import time
import datetime
import pytz
import calendar
from decimal import Decimal

from apiclient import discovery
from oauth2client import file
from oauth2client import client
from oauth2client import tools

import matplotlib.pyplot as plt

# Email packages
import imaplib
import time
import email.utils
import urllib2
import urllib
import string
from email.header import decode_header

# GLOBALS
HISTORY_TIMESTAMP = 1388534400

################# GOOGLE DOCS / TRADE HISTORY PARSER #################
# Parser for command-line arguments.
parser = argparse.ArgumentParser(
    description=__doc__,
    formatter_class=argparse.RawDescriptionHelpFormatter,
    parents=[tools.argparser])


# CLIENT_SECRETS is name of a file containing the OAuth 2.0 information for this
# application, including client_id and client_secret. You can see the Client ID
# and Client secret on the APIs page in the Cloud Console:
# <https://cloud.google.com/console#/project/305573432144/apiui>
CLIENT_SECRETS = os.path.join(os.path.dirname(__file__), 'client_secrets.json')

# Set up a Flow object to be used for authentication.
# Add one or more of the following scopes. PLEASE ONLY ADD THE SCOPES YOU
# NEED. For more information on using scopes please see
# <https://developers.google.com/+/best-practices>.
FLOW = client.flow_from_clientsecrets(CLIENT_SECRETS,
  scope=[
      'https://www.googleapis.com/auth/drive',
      'https://www.googleapis.com/auth/drive.appdata',
      'https://www.googleapis.com/auth/drive.apps.readonly',
      'https://www.googleapis.com/auth/drive.file',
      'https://www.googleapis.com/auth/drive.metadata.readonly',
      'https://www.googleapis.com/auth/drive.readonly',
      'https://www.googleapis.com/auth/drive.scripts',
    ],
    message=tools.message_if_missing(CLIENT_SECRETS))



def retrieve_all_files(service):
  """Retrieve a list of File resources.

  Args:
    service: Drive API service instance.
  Returns:
    List of File resources.
  """
  result = []
  page_token = None
  while True:
    try:
      param = {}
      if page_token:
        param['pageToken'] = page_token
      
      files = service.files().list(**param).execute()
      result.extend(files['items'])
      
      page_token = files.get('nextPageToken')
      if not page_token:
        break
    except errors.HttpError, error:
      print 'An error occurred: %s' % error
      break

  return result

def retrieve_all_folders(service):
  """Retrieve a list of File resources.

  Args:
    service: Drive API service instance.
  Returns:
    List of File resources.
  """
  result = []
  page_token = None
  while True:
    try:
      param = {}
      if page_token:
        param['pageToken'] = page_token

      files = service.files().list(**param).execute()
      result.extend(files['items'])
      page_token = files.get('nextPageToken')
      
      if not page_token:
        break
    except errors.HttpError, error:
      print 'An error occurred: %s' % error
      break
  return result

def get_files_in_folder(service, folder_id):
  """Returns files belonging to a folder.

  Args:
    service: Drive API service instance.
    folder_id: ID of the folder to print files from.
  """
  page_token = None
  files = []
  while True:
    try:
      param = {}
      if page_token:
        param['pageToken'] = page_token
      
      children = service.children().list(
          folderId=folder_id, **param).execute()

      for child in children.get('items', []):
        files.append(child)
        
      page_token = children.get('nextPageToken')
      if not page_token:
        break
    except errors.HttpError, error:
      print 'An error occurred: %s' % error
      break

  return files

def insertIntoDatabase(database_table, csv_data):
    csv_data = csv_data.split('\n')
    print csv_data
    mydb = MySQLdb.connect(host='scenecheckdb.ccwd1ptkptjf.us-east-1.rds.amazonaws.com',
                           user='justinoliver51',
                           passwd='annais2AWESOME!',
                           db='SceneCheckDB')
    cursor = mydb.cursor()
    
    # The first row contains the columns
    firstRow = True
    columns = ""
    for row in csv_data:
        # Grab the columns of the table, but skip inserting data
        if firstRow:
            columns = row
            firstRow = False
            continue
        
        row = "\'" + row
        row = row.replace(',', "\',\'")
        row += "\'"
        
        query = 'INSERT INTO ' + database_table + '(' + columns + ') \
              VALUES(' + row + ')'
        print query
        
        try:
            cursor.execute(query)
        except:
            self.connection.rollback()
    #close the connection to the database.
    mydb.commit()
    cursor.close()
    print "Done"
    
    return

def cleanUpTradingData(tradingData):
    removeEntries = []
    copyOfTradingData = list(tradingData)
    for trade in tradingData:
        try:
            # Make sure the price is legal
            trade['Price'] = float(trade['T. Price'])
            
            # If the quantity is negative, remove it
            if int(trade['Quantity']) <= 0:
                removeEntries.append(trade)
                continue
            # Otherwise, find the corresponding seller and save the P/L
            else:
                for correspondingTrade in copyOfTradingData:
                    if ( correspondingTrade['Symbol'] == trade['Symbol'] ) and \
                        ( float(correspondingTrade['Basis']) == (-1 * float(trade['Basis'] )) ) and \
                        ( int(correspondingTrade['Quantity']) == (-1 * int(trade['Quantity'])) ):
                            trade['Realized P/L'] = correspondingTrade['Realized P/L']
                            break
                
            # Remove the '+' from the symbols
            trade['Symbol'] = trade['Symbol'].replace('+', '')
            
            # Get the timestamp for when the trade was initiated
            fmt = '%Y-%m-%d, %H:%M:%S'
            timeString = trade['Date/Time']
            theDate = datetime.datetime.strptime(timeString, fmt)
            eastern = pytz.timezone('US/Eastern')
            eastern_time = eastern.localize(theDate)
            utc_time = eastern_time.astimezone(pytz.utc)
            trade['Timestamp'] = calendar.timegm(utc_time.utctimetuple())
            
            # Remove trades that are too old, before January 1, 2014
            if int(trade['Timestamp']) < HISTORY_TIMESTAMP:
                removeEntries.append(trade)
                continue
            
            #print time
        except Exception as inst:
            print type(inst)     # the exception instance
            print inst           # __str__ allows args to printed directly
            removeEntries.append(trade)
            
    for badTradeData in removeEntries:
        tradingData.remove(badTradeData)
        
    return tradingData

def getTradeHistory(http, service):
    try:
        files = retrieve_all_files(service)
        spreadsheets = []
        #print json.dumps(files, indent=2)
    
        for theFile in files:
            if( (theFile['mimeType'] == 'application/vnd.google-apps.folder') and
               (theFile['title'] == 'IBTrading') ):
                
                # Gets the files
                spreadsheets = get_files_in_folder(service, theFile['id'])
                break
    
        for spreadsheet in spreadsheets:
            theFile = service.files().get(fileId=spreadsheet['id']).execute()
            
            #print json.dumps(theFile, indent=2)
            #print theFile['title']
            
            # If this isn't a spreadsheet
            if(theFile['mimeType'] != 'application/vnd.google-apps.spreadsheet'):
                continue
            
            # If the files name doesn't match
            if(theFile['title'] != 'Updated Trade Data'):
                continue
            
            url = theFile['exportLinks']['application/pdf']
            url = url[:-4] + "=csv" + "&gid=0"
            response, content = http.request(url)
    
            # Build the trading data list
            tradingData = getTradingData(str(content))

    except client.AccessTokenRefreshError:
        print ("The credentials have been revoked or expired, please re-run"
                  "the application to re-authorize")
        
    return tradingData

def getTradingData(csv_data):
    csvReader = csv.reader(csv_data.split('\n'), delimiter=',', quotechar='"')
    firstRow = True
    columns = []
    tradingData = []

    for row in csvReader:
        newTrade = {}
        
        # Get the names of the columns
        if firstRow:
            firstRow = False
            columns = row
            
            continue
        
        for index in range(len(columns)):
            newTrade[columns[index]] = row[index]
        
        if newTrade['Symbol'].find('+') >= 0:
            tradingData.append(newTrade)
            
    tradingData = cleanUpTradingData(tradingData)
    
    return tradingData

################# EMAIL CLASSES/FUNCTIONS #################
class JasonBondsParser:
    def __init__(self):
        return
    
    def getTrade(self, tradeBody, startingIndex):
        spacesCount = 0
        index = startingIndex
    
        # Find the end point
        while (index < len(tradeBody)) and (spacesCount < 5) and (tradeBody[index] != '\n') and (tradeBody[index] != '\r') :
            if tradeBody[index] == ' ':
                spacesCount = spacesCount + 1
            index = index + 1

        index = index - 1
        exclude = set(string.punctuation)
        tradeList = ''.join(ch for ch in tradeBody[startingIndex:index] if (ch == '.') or (ch == '$') or ((ch not in exclude) and (ch != '\r') and (ch != '\n')))  # FIXME: Need to leave in '.'
    
        if( (len(tradeList.split(' ')) == 5) and (tradeList.split(' ')[4].find('$') == 0) ):
            index = tradeList.find('$')
            price = ''.join(ch for ch in tradeList.split(' ')[4] if (ch == '$') or (ch == '.') or (ch in string.digits))
            tradeList = tradeList[:index] + price
        
        if tradeList[-1] == '.':
            tradeList = tradeList[:-1]
        
        return tradeList
    
    def parseTrade(self, tradeString = ""):
        price = ""
        article = ""
        index = 0
        
        if tradeString.lower().find('bought') >= 0:
            index = tradeString.lower().find('bought')
        elif tradeString.lower().find('added') >= 0:
            index = tradeString.lower().find('added')
        elif tradeString.lower().find('taking') >= 0:
            index = tradeString.lower().find('taking')
        else:
            return None
        
        # Get the trade information
        trade = self.getTrade(tradeString, index)
            
        if(len(trade.split(' ')) == 5):
            price = trade.split(' ')[4]
            article = trade.split(' ')[3]
            
        if(price.find('$') >= 0 and article == 'at'):
            trade = trade.replace('$', '')
                
            return trade
        return None
    
def connect(retries=5, delay=3):
    while True:
        try:
            mail = imaplib.IMAP4_SSL('imap.gmail.com','993')
            mail.login('justin.tradealerts@gmail.com', 'utredhead51')
            return mail
        except imaplib.IMAP4_SSL.abort:
            if retries > 0:
                retries -= 1
                time.sleep(delay)
            else:
                raise

def get_emails(email_ids):
    data = []
    for e_id in email_ids:
        _, response = mail.fetch(e_id, '(UID BODY[TEXT])')
        data.append(response[0][1])
    return data

def getEmailBody(email_message):
    
    for part in email_message.walk():
        bodyDecoded, encoding = decode_header(part)[0]
        if encoding == None:
            body = bodyDecoded
        else:
            body = part.get_payload(decode=True).decode(encoding)
                
        #index = 0
        #while body.lower().find('bought', index + 1) > 0:
        #    index = body.lower().find('bought', index)
        #    trade = getTrade(body, index)
        #    price = trade.split(' ')[4]
        #    article = trade.split(' ')[3]   
        
        parser = JasonBondsParser()
        newTrade = parser.parseTrade(body)
        
        if(newTrade != None):
            return newTrade

    return None

def decodeSubject(email_message):
    subjectDecoded, encoding = decode_header(email_message['Subject'])[0]
    if encoding==None:
        subjectDecodedParsed = email_message['Subject']
        return subjectDecoded
    else:
        subjectDecodedParsed = subjectDecoded.decode(encoding)
        return subjectDecodedParsed
    
def getAlertHistory():
    # List of all alerts with the relevant data
    alertHistory = []
    
    try:
        mail = connect()
        latestEmailTimestamp = HISTORY_TIMESTAMP
        
        # Connect to the Inbox
        mail.select("INBOX") 
    
        result, data = mail.uid('search', None, 'ALL') # search and return uids instead
        for negativeIndex in range(-1, (len(data[0].split()) - 1) * -1, -1):
            try:
                latest_email_uid = data[0].split()[negativeIndex]
                index = negativeIndex + len(data[0].split())
        
                result, data1 = mail.fetch(index, 'INTERNALDATE')
                timestamp = time.mktime(imaplib.Internaldate2tuple(data1[0]))
        
                # If this email is newer than our latest, get the message
                # Looking for something like "Bought 10,000 DMD at $5.19"
                if timestamp > latestEmailTimestamp:
                    result, data2 = mail.uid('fetch', latest_email_uid, '(RFC822)') 
                    raw_email = data2[0][1]
                    email_message = email.message_from_string(raw_email)
                    traderID = email.utils.parseaddr(email_message['From'])
                    subject = decodeSubject(email_message)
        
                    # If the subject does not contain 'Bought' or 'Added', move on
                    if subject.lower().find('bought') < 0:
                        continue
                    elif (traderID[0] == 'Jason' or traderID[0] == 'Jason Bond'):
                        try:
                            trade = getEmailBody(email_message)
                            if trade == None or trade == '':
                                continue
                        except:
                            continue
                    else:
                        continue
        
                    # Get the data from the alert
                    tradeData = trade.split(' ')
                    alertData = {
                                 'Timestamp':   timestamp,
                                 'Quantity':    tradeData[1],
                                 'Price':       tradeData[4],
                                 'Symbol':      tradeData[2]
                                }
                    
                    alertHistory.append(alertData)
                    
                # Otherwise, we are finished looking
                else:
                    break;
            except Exception as inst:
                print type(inst)     # the exception instance
                print inst           # __str__ allows args to printed directly
    except Exception as inst:
        print 'An error occurred getting the mail'
        print type(inst)     # the exception instance
        print inst           # __str__ allows args to printed directly
        sys.exit()
    
    return alertHistory


################# PLOTTING DATA #################
def buildPlot(tradingHistory, alertsHistory):
    plot = []
    tradePercentages = []
    profits = []
    
    for trade in tradingHistory:
        # If an alert gets matched, remove it
        removeAlert = None
        
        for alert in alertsHistory:
            # If this trade did not occur in the timespan of this alert, skip it
            # The trade will never come before the alert
            if float(trade['Timestamp']) > (float(alert['Timestamp']) + 3600):
                removeAlert = None
                continue
            
            # Make sure this is the same stock
            if trade['Symbol'] != alert['Symbol']:
                removeAlert = None
                continue
            
            # Calculate the percentage difference in price
            priceDifference = float( (float(trade['Price'] / float(alert['Price']))) - 1 ) * 100
            #priceDifference = float(trade['Price']) - float(alert['Price'])
            
            # Save the data
            tradePercentages.append(priceDifference)
            profits.append(trade['Realized P/L'])
        
            # Remove the alert and break
            removeAlert = alert
            break
        
        if removeAlert != None:
            alertsHistory.remove(removeAlert)
    
    plot.append(tradePercentages)
    plot.append(profits)
    
    return plot
    
################# MAIN FUNCTION #################
def main(argv):
    # Parse the command-line flags.
    flags = parser.parse_args(argv[1:])
    
    # If the credentials don't exist or are invalid run through the native client
    # flow. The Storage object will ensure that if successful the good
    # credentials will get written back to the file.
    storage = file.Storage('Resources/trading_storage.dat')
    credentials = storage.get()
    if credentials is None or credentials.invalid:
        credentials = tools.run_flow(FLOW, storage, flags)
    
    # Create an httplib2.Http object to handle our HTTP requests and authorize it
    # with our good Credentials.
    http = httplib2.Http()
    http = credentials.authorize(http)
    
    # Construct the service object for the interacting with the Drive API.
    service = discovery.build('drive', 'v2', http=http)
    
    # Search my latest downloaded statement. Get the following data for each trade:
    # - Date/Time
    # - Number of shares 
    # - Price
    # - Symbol
    # - P/L
    #tradingHistory = getTradeHistory(http, service)
    #print json.dumps(tradingHistory, indent=2)
    
    # Search through emails. Get the following data for each Jason Bonds trade:
    # - Date/Time
    # - Number of shares 
    # - Price
    # - Symbol
    #tradeAlertsHistory = getAlertHistory()
    #print json.dumps(tradeAlertsHistory, indent=2)
    
    theFile = open('/Users/justinoliver/Desktop/Developer/Trading/TradingScripts/src/Resources/TradingHistory.txt', 'r')
    tradingHistory = json.loads(theFile.read())
    theFile.close()
    
    theFile = open('/Users/justinoliver/Desktop/Developer/Trading/TradingScripts/src/Resources/AlertHistory.txt', 'r')
    tradeAlertsHistory =json.loads(theFile.read())
    theFile.close()
    
    # Build list of profts vs percentage price difference
    xAxisList, yAxisList = buildPlot(tradingHistory, tradeAlertsHistory)
    
    plt.plot(xAxisList, yAxisList, 'ro')
    plt.axis([-100, 100, -1200, 1200])
    #plt.axis([-1, 1, -1200, 1200])
    plt.show()
    
    # Plot a graph of profits vs percentage difference
    
if __name__ == '__main__':
  main(sys.argv)

# vim: ts=8 et sw=4 sts=4 background=light
