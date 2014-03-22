'''
Created on Sep 18, 2013

@author: justinoliver
'''

import imaplib
import time
import email.utils
import urllib2
import urllib
import string
from email.header import decode_header
from googlevoice import Voice

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
            
            # If this is a bond blow ups, inform the server
            if(tradeString.lower().find('bond blow ups') >= 0):
                trade = "Bond Blow Ups " + trade
                
            return trade
        return None

### FUNCTIONS ###
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
        #print 'I am NOT decoding Subject'
        print subjectDecodedParsed
        
        return subjectDecoded
    else:
        subjectDecodedParsed = subjectDecoded.decode(encoding)
        #print 'I am decoding subject'
        print subjectDecodedParsed.encode('utf8') #<--- Only first line will be presented here
        
        return subjectDecodedParsed
    

### MAIN ###
debug = False

# Email Info
latestEmail = ""
currentEmail = "currentEmail"
mail = connect()

#Google Voice Info
personalEmail = "justin.tradealerts@gmail.com"
password = "utredhead51"
JUSTINS_CELL = 12144762900
TYLERS_CELL  = 16302446933
DANS_CELL    = 13013999397
voice = Voice()

if debug == True:
    latestEmailTimestamp = 100 # Tests every email
else:
    latestEmailTimestamp = time.mktime(time.gmtime()) 

while True:
    try:
        # Connect to the Inbox
        mail.select("INBOX") 

        result, data = mail.uid('search', None, 'ALL') # search and return uids instead
        for negativeIndex in range(-1, (len(data[0].split()) - 1) * -1, -1):
            latest_email_uid = data[0].split()[negativeIndex]
            index = negativeIndex + len(data[0].split())

            result, data1 = mail.fetch(index, 'INTERNALDATE')
            timestamp = time.mktime(imaplib.Internaldate2tuple(data1[0]))

            # If this email is newer than our latest, get the message and send the alert
            if timestamp > latestEmailTimestamp:
                print 'New Email!'
                result, data2 = mail.uid('fetch', latest_email_uid, '(RFC822)') 
                raw_email = data2[0][1]
                email_message = email.message_from_string(raw_email)
                traderID = email.utils.parseaddr(email_message['From'])
                subject = decodeSubject(email_message)

                # If the subject does not contain 'Bought' or 'Added', move on
                if( (subject.lower().find('bought') < 0) and (subject.lower().find('added') < 0) ):
                    continue

                if(traderID[0] == 'Jason' or traderID[0] == 'Jason Bond' or traderID[0] == 'Justin Oliver' or traderID[0] == 'Trade Alerts'):
                    try:
                        trade = getEmailBody(email_message)
                        if trade == None or trade == '':
                            trade = subject
                    except:
                        trade = subject
                        print 'Unable to get the body'
                else:
                    trade = subject

                # Build the url
                paramDic = {'traderID':         traderID[0],
                            'newTrade':         trade,
                            'realTimeSystem':   'email'
                            }
                url = "http://localhost:8080/IBTradingServer/RemoteProcedureCallsServlet?"
                
                if debug == True:
                    if(  ((trade.lower().find('bought') < 0) and (trade.lower().find('added') < 0) ) ): # (traderID[0] != 'Jason Bond' and traderID[0] != 'Jason') or
                        continue
                    
                    paramDic['traderID'] = 'Justin Oliver'
                
                encodedParams = urllib.urlencode(paramDic)
                query = url + encodedParams

                # Send the alert
                print query
                response = urllib2.urlopen(query).read()
                    
                # If we successfully completed a trade, send the text
                if response.lower().find('valid trade') >= 0 and response.lower().find('invalid trade') < 0:
                    voice.login(personalEmail, password)
                    voice.send_sms(JUSTINS_CELL, response)
                    voice.send_sms(TYLERS_CELL, response)
                    voice.send_sms(DANS_CELL, response)
                    voice.logout()
                
            # Otherwise, we are finished looking
            else:
                break;

        # Get the latest email's timestamp
        result, data3 = mail.fetch(len(data[0].split()) - 1, 'INTERNALDATE') 
        latestEmailTimestamp = time.mktime(imaplib.Internaldate2tuple(data3[0]))

        ## print email_message.items() # print all headers
        #
        ## Poll mailbox after alotted time
        time.sleep(1)
    except:
        print 'Error'
        mail = connect()

# vim: ts=8 et sw=4 sts=4 background=light
