'''
Created on Feb 15, 2014

@author: justinoliver
'''

from googlevoice import Voice
import BeautifulSoup
import string
import time
import urllib2
import urllib

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
def extractsms(htmlsms) :
    """
    extractsms  --  extract SMS messages from BeautifulSoup tree of Google Voice SMS HTML.

    Output is a list of dictionaries, one per message.
    """
    newMessages = []  
    message = ""   
    returnMap = {}                                 
    #    Extract all conversations by searching for a DIV with an ID at top level.
    tree = BeautifulSoup.BeautifulSoup(htmlsms)            # parse HTML into tree
    conversations = tree.findAll("div",attrs={"id" : True},recursive=False)
    for conversation in conversations :
        #    For each conversation, extract each row, which is one SMS message.
        rows = conversation.findAll(attrs={"class" : "gc-message-sms-row"})
        for row in rows :                                # for all rows
            #    For each row, which is one message, extract all the fields.
            msgitem = {"id" : conversation["id"]}        # tag this message with conversation ID
            spans = row.findAll("span",attrs={"class" : True}, recursive=False)
            for span in spans :                            # for all spans in row
                cl = span["class"].replace('gc-message-sms-', '')
                msgitem[cl] = (" ".join(span.findAll(text=True))).strip()    # put text in dict
            
            newMessages.append(msgitem)                    # add msg dictionary to list
        
    if(len(newMessages) == 0):
        returnMap['newMessages'] = []
    else:
        returnMap['newMessages'] = newMessages
    
    return returnMap

### MAIN ###
debug = False
email = "justin.tradealerts@gmail.com"
password = "utredhead51"
JUSTINS_CELL = 12144762900
TYLERS_CELL  = 16302446933
DANS_CELL    = 13013999397
lastReadSMS = ""

voice = Voice()

# Login and delete all messages
voice.login(email, password)
for message in voice.sms().messages:
    message.delete()
# voice.logout()

while True:
    try:
        time.sleep(1)
        # voice.login(email, password)
        
        # Get the latest SMS messages
        for message in voice.sms().messages:
            # If there is an unread message, 
            if message.isRead == False:
                newMessagesInfo =  extractsms(voice.sms.html)
                newMessagesArray = newMessagesInfo['newMessages']
                
                for newMessage in newMessagesArray:
                    messageText = newMessage['text']
                    print messageText
                    
                    if(messageText == lastReadSMS):
                        continue
                    else:
                        lastReadSMS = messageText
                    
                    # Parse the message for trade information
                    parser = JasonBondsParser()
                    newTrade = parser.parseTrade(messageText)
        
                    if(newTrade == None):
                        continue

                    # Build the url
                    paramDic = {'traderID':         'Jason Bond',
                                'newTrade':         newTrade,
                                'realTimeSystem':   'email'
                                }
                    url = "http://localhost:8080/IBTradingServer/RemoteProcedureCallsServlet?"
                
                    if debug == True:
                        paramDic['traderID'] = 'Justin Oliver'
                    elif message.displayNumber == '(214) 476-2900':
                        tradeList = newTrade.split(' ')
                        tradeList[1] = '1'
                        newTrade = " ".join(tradeList)
                        paramDic['newTrade'] = newTrade
                        #paramDic['traderID'] = 'Justin Oliver'
                
                    encodedParams = urllib.urlencode(paramDic)
                    query = url + encodedParams

                    # Send the alert
                    print query
                    response = urllib2.urlopen(query).read()
                    
                    # If we successfully completed a trade, send the text
                    if response.lower().find('valid trade') >= 0 and response.lower().find('invalid trade') < 0:
                        voice.send_sms(JUSTINS_CELL, response)
                        voice.send_sms(TYLERS_CELL, response)
                        voice.send_sms(DANS_CELL, response)

                # Delete the message
                message.delete()
            
        # voice.logout()
    except:
        print 'Error'
        voice.login(email, password)