from datetime import datetime, date, timedelta
import time
import sys
import subprocess
import urllib2
import urllib
import string

import logging
import os


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

def initialize_logger(output_dir):
    logger = logging.getLogger()
    logger.setLevel(logging.DEBUG)
     
    # create console handler and set level to info
    handler = logging.StreamHandler()
    handler.setLevel(logging.INFO)
    formatter = logging.Formatter("%(levelname)s - %(message)s")
    handler.setFormatter(formatter)
    logger.addHandler(handler)
 
    # create error file handler and set level to error
    handler = logging.FileHandler(os.path.join(output_dir + "error.log"), encoding=None, delay="true")
    handler.setLevel(logging.ERROR)
    formatter = logging.Formatter("%(levelname)s - %(message)s")
    handler.setFormatter(formatter)
    logger.addHandler(handler)
 
    # create debug file handler and set level to debug
    handler = logging.FileHandler(os.path.join(output_dir + "all.log"))
    handler.setLevel(logging.DEBUG)
    formatter = logging.Formatter("%(levelname)s - %(message)s")
    handler.setFormatter(formatter)
    logger.addHandler(handler)

def debug():
    try: 
        initialize_logger('/Users/justinoliver/Desktop/Developer/WebExtensions/JasonBonds/')
        
        logging.debug("debug message")
        logging.info("info message")
        logging.warning("warning message")
        logging.error("error message")
        logging.critical("critical message")
    except Exception, inst:
        handleError(inst, 'An error occurred during debug')

def getClipboardData(): 
    p = subprocess.Popen(['pbpaste'], stdout=subprocess.PIPE) 
    retcode = p.wait() 
    data = p.stdout.read() 
  
    return data

def newAlert(region, debugFlag):
    logging.debug('New alert!') 
    
    # Get the data from the chatroom 
    tradeData = getTradeData(region)

    # Parse the message for trade information
    parser = JasonBondsParser()
    newTrade = parser.parseTrade(tradeData)

    # If we succcessfully parsed a trade, send it to the server
    if newTrade != None:
        sendTradeToServer(newTrade, debugFlag)

def getTradeData(region):
    # Copy the alert to the clipboard
    if region.exists(Pattern("JasonBondsAnnouncement.png")):
        region.doubleClick(Pattern("JasonBondsAnnouncement.png").targetOffset(-83,19))
        type('a', KeyModifier.CMD)  
        type('c', KeyModifier.CMD) 
    else:
        return None
    
    tradeData = getClipboardData() #Env.getClipboard()
    return tradeData

def sendTradeToServer(newTrade, debugFlag):
    try:
        # Build the url
        paramDic = {'traderID':         'Jason Bond',
                    'newTrade':         newTrade,
                    'realTimeSystem':   'websiteMonitor'
                    }
        url = "http://localhost:8080/IBTradingServer/RemoteProcedureCallsServlet?"
        
        if debugFlag == True:
            paramDic['traderID'] = 'Justin Oliver'
            
        encodedParams = urllib.urlencode(paramDic)
        query = url + encodedParams
        
        # Send the alert
        logging.debug(query)
        response = urllib2.urlopen(query).read()
    except Exception, inst:
        handleError(inst, 'An error occurred while trying to send data to the server')

def handleError(inst, errorMessage):
    logging.error(errorMessage + '\n' + str(inst))
    #print errorMessage
    #print type(inst)     # the exception instance
    #print inst           # __str__ allows args to printed directly 

def newAnnouncement(region, debugFlag):
    print 'New announcement!' 
    try:
        if region.exists("1397191952703.png") or region.right().exists("1397191952703.png"):
            theRegion = region
        elif region.right().exists("1397191952703.png"):
            theRegion = region.right()
        else:
            return
        
        # If this happens to be from Jason, call the alert 
        if theRegion.exists("JasonBondsAnnouncement.png"):
            newAlert(theRegion, debugFlag)
        
        # Close the alert
        theRegion.click("1397191952703.png")
    except Exception, inst:
        handleError(inst, 'An error occurred while trying to close the announcement')
        
### MAIN ###
def main(args):
    debugFlag = False
    loopIndex = 0

    initialize_logger('/Users/justinoliver/Desktop/Developer/WebExtensions/JasonBonds/')

    if debugFlag == True:
        debugFunctionFlag = True
        
        if debugFunctionFlag == True:
            debug()
            print 'Completed debug'
            
            return
    
    # Make sure we are in the chat room
    # Make sure we're in 'omniTweet'
    if exists("1381195946550.png"):
        click("1381195946550.png")
        click("1397444289724.png") 
    else:
        print "Jason Bonds Chatroom isn't running!"
        return
    
    # Infinite loop looking at chat room
    region = Region(480,684,1067,202)
    
    print datetime.now().time()
    while True:    #datetime.now().strftime('%H:%M') < '18:00':     
        time.sleep(0.1)
        loopIndex += 1
        if loopIndex == 5 and debugFlag == True:
            break

        try:
            # Look for an announcement 
            if region.exists("1397191952703.png") or region.right().exists("1397191952703.png"):
                newAnnouncement(region, debugFlag)                 
            else:
                print 'Nothing new...'
        except Exception, inst:
            handleError(inst, 'An error occurred in the main loop.')

    return

if __name__ == '__main__':
    main(sys.argv)

# vim: ts=8 et sw=4 sts=4 background=light