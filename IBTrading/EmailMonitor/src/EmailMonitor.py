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

def getTrade(email_body, startingIndex):
    spacesCount = 0
    index = startingIndex
    
    # Find the end point
    while spacesCount < 5:
        if email_body[index] == ' ':
            spacesCount = spacesCount + 1
        index = index + 1

    index = index - 1
    exclude = set(string.punctuation)
    tradeList = ''.join(ch for ch in email_body[startingIndex:index] if (ch == '.') or ((ch not in exclude) and (ch != '\r') and (ch != '\n')))  # FIXME: Need to leave in '.'
    
    return tradeList

def getEmailBody(email_message):
    # Get the important information out of the email
    message = ""
    for part in email_message.walk():
        if part.get_content_type() == "text/plain": # ignore attachments / html
            bodyDecoded, encoding = decode_header(part)[0]
            if encoding == None:
                body = bodyDecoded
            else:
                body = part.get_payload(decode=True).decode(encoding)
                
            if body.find('Bought') >= 0:
                index = body.find('Bought')
                return getTrade(body, index)
            elif body.find('Added') >= 0:
                index = body.find('Added')
                return getTrade(body, index)
            
            message = message + body
        else:
            continue
        
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
latestEmail = ""
latestEmailTimestamp = time.mktime(time.gmtime())
currentEmail = "currentEmail"
mail = connect()
while True:
    try:
        # Connect to the Inbox
        mail.select("INBOX") 

        result, data = mail.uid('search', None, 'ALL') # search and return uids instead
        for negativeIndex in range(-1, len(data[0].split() * -1)):
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

                try:
                    trade = getEmailBody(email_message)
                except:
                    trade = subject
                    print 'Unable to get the body'

                # Build the url
                paramDic = {'traderID':         traderID[0], 
                            'newTrade':         trade,
                            'realTimeSystem':   'email'
                            }
                url = "http://localhost:8080/IBTradingServer/RemoteProcedureCallsServlet?"
                
                encodedParams = urllib.urlencode(paramDic)
                query = url + encodedParams

                # Send the alert
                print query
                urllib2.urlopen(query).read()
                
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

