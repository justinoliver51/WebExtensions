'''
Created on Feb 15, 2014
For now, simply copy/pasted into EmailMonitor.py and SMSMonitor.py

@author: justinoliver
'''

import string

class JasonBondsParser:
    def __init__(self):
        return
    def getTrade(self, tradeBody, startingIndex):
        spacesCount = 0
        index = startingIndex
    
        # Find the end point
        while spacesCount < 5 and tradeBody[index] != '\n' and tradeBody[index] != '\r':
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
        
### MAIN ###