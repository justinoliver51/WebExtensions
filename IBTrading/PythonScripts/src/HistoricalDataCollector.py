import imaplib
import time
import email.utils
import urllib2
import urllib
import string
import MySQLdb
import calendar
from email.parser import HeaderParser
import datetime
from dateutil import parser

import httplib2
import os
import sys
import json
from email.header import decode_header

# GLOBALS
HISTORY_TIMESTAMP = 0 # 1388534400 --- New Year         # Starting time for email searching
url = 'http://localhost:8080/TradingServer/RemoteProcedureCallsServlet?'

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
        
        for latest_email_uid in reversed(data[0].split()):
            try:
                #index = latest_email_uid
                #latest_email_uid = data[0].split()[int(indexString) - 1]
        
                result, data1 = mail.fetch(latest_email_uid, 'INTERNALDATE')
                timestamp = time.mktime(imaplib.Internaldate2tuple(data1[0]))
        
                typ, msg = mail.fetch(latest_email_uid, '(BODY[HEADER])')
                headerParser = HeaderParser()
                pmsg = headerParser.parsestr(msg[0][1])
                #theDate = " ".join(pmsg['Date'].split()[:-2])
                #tempDate = datetime.datetime.strptime(theDate,"%a, %d %b %Y %H:%M:%S")
                
                date = parser.parse(pmsg['Date'])
                arrivedTimestamp = calendar.timegm(date.utctimetuple())

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
                                 'Date':        date,
                                 'Timestamp':   arrivedTimestamp,
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

################# DATABASE #################
class Database:
    host        = '127.0.0.1'
    user        = 'justinoliver51'
    password    = 'utredhead51'
    db          = 'IBTradingDB'

    def __init__(self):
        self.connection = MySQLdb.connect(self.host, self.user, self.password, self.db)
        self.cursor = self.connection.cursor()

    def insert(self, query, values):
        try:
            self.cursor.execute(query, values)
            self.connection.commit()
        except Exception as inst:
            print type(inst)     # the exception instance
            print inst           # __str__ allows args to printed directly
            self.connection.rollback()
        
        return self.cursor.lastrowid

    def query(self, query):
        cursor = self.connection.cursor( MySQLdb.cursors.DictCursor )
        cursor.execute(query)

        return cursor.fetchall()

    def getLastRowID(self):
        return self.connection.insert_id()

    def __del__(self):
        self.connection.close()

def insertTradeAlert(database, tradeAlert):
    query = ('''INSERT INTO TradeAlerts (PredictorName, Time, Symbol, Quantity, Price) 
          VALUES (%s,%s,%s,%s,%s)''', \
          ("Jason Bonds", datetime.datetime.fromtimestamp(int(tradeAlert['Timestamp'])).strftime('%Y-%m-%d %H:%M:%S'), tradeAlert['Symbol'], tradeAlert['Quantity'], tradeAlert['Price']))
    
    print query
    id = database.insert(*query)
    
    return id

def insertHistoricalData(database, historicalData, tradeID):
    for data in historicalData:
        query = ('''INSERT INTO HistoricalData (TradeID, Date, Open, High, Low, Close, BarCount, Volume, WAP, HasGaps) 
              VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)''', \
              (tradeID, data['date'], data['open'], data['high'], data['low'], data['close'], data['barCount'], data['volume'], data['WAP'], data['hasGaps']))
        print query
        database.insert(*query)
    
    return

################# MAIN FUNCTION #################
def main():
    # Search through emails. Get the following data for each Jason Bonds trade:
    # - Date/Time
    # - Number of shares 
    # - Price
    # - Symbol
    tradeAlertsHistory = getAlertHistory()
    print json.dumps(tradeAlertsHistory, indent=2)
    
    f = open('/Users/justinoliver/Desktop/Developer/Trading/TradingScripts/src/Resources/AlertHistory.txt','w')
    f.write(json.dumps(tradeAlertsHistory, indent=2)) # python will convert \n to os.linesep
    f.close() # you can omit in most cases as the destructor will call if
    
    #theFile = open('/Users/justinoliver/Desktop/Developer/Trading/TradingScripts/src/Resources/AlertHistory.txt', 'r')
    #tradeAlertsHistory =json.loads(theFile.read())
    #theFile.close()
    
    db = Database()
    twentyMinutes = 60 * 20
    
    sys.exit()
    
    for tradeAlert in tradeAlertsHistory:
        
        # Build the URL
        timestamp = (int(tradeAlert['Timestamp']) + twentyMinutes) * 1000
        paramDic = {
                    'historicalDataSym':        tradeAlert['Symbol'],
                    'historicalDataTimestamp':  str(timestamp),
                    }
    
        encodedParams = urllib.urlencode(paramDic)
        query = url + encodedParams
    
        # Get the historical data by querying my server
        print query
        response = urllib2.urlopen(query).read()
        
        tradeAlertID = insertTradeAlert(db, tradeAlert)
        insertHistoricalData(db, json.loads(response), tradeAlertID)
        
        time.sleep(60)
    
if __name__ == '__main__':
  main()


