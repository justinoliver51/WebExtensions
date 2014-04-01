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
            tempPrice = float(trade['T. Price'])
            
            # If the quantity is negative, remove it
            if int(trade['Quantity']) <= 0:
                removeEntries.append(trade)
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
            
            # if this data is too old, remove it
            fmt = '%Y-%m-%d, %H:%M:%S'
            timeString = trade['Date/Time']
            theDate = datetime.datetime.strptime(timeString, fmt)
            eastern = pytz.timezone('US/Eastern')
            eastern_time = eastern.localize(theDate)
            utc_time = eastern_time.astimezone(pytz.utc)
            trade['Timestamp'] = calendar.timegm(utc_time.utctimetuple())
            
            #print time
        except Exception as inst:
            print type(inst)     # the exception instance
            print inst           # __str__ allows args to printed directly
            removeEntries.append(trade)
            
    for badTradeData in removeEntries:
        tradingData.remove(badTradeData)
        
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

def main(argv):
  # Parse the command-line flags.
  flags = parser.parse_args(argv[1:])

  # If the credentials don't exist or are invalid run through the native client
  # flow. The Storage object will ensure that if successful the good
  # credentials will get written back to the file.
  storage = file.Storage('trading_storage.dat')
  credentials = storage.get()
  if credentials is None or credentials.invalid:
    credentials = tools.run_flow(FLOW, storage, flags)

  # Create an httplib2.Http object to handle our HTTP requests and authorize it
  # with our good Credentials.
  http = httplib2.Http()
  http = credentials.authorize(http)

  # Construct the service object for the interacting with the Drive API.
  service = discovery.build('drive', 'v2', http=http)

  try:
    print "Success! Now add code here."    

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
        
        print json.dumps(tradingData, indent=2)

  except client.AccessTokenRefreshError:
    print ("The credentials have been revoked or expired, please re-run"
      "the application to re-authorize")

if __name__ == '__main__':
  main(sys.argv)

# Search my latest downloaded statement.  Get the following data for each trade:
# - Date/Time
# - Number of shares 
# - Price
# - Symbol


# Search through emails. Get the following data for each Jason Bonds trade:
# - Date/Time
# - Number of shares 
# - Price
# - Symbol


# Plot a graph of profits vs percentage difference
