from datetime import datetime, date, timedelta
import time
import sys
import subprocess

def debug():
    print 'debug!'
    time.sleep(1)

    # Copy the alert to the clipboard
    doubleClick(Pattern("1397436587563.png").targetOffset(-44,20))
    type('a', KeyModifier.CMD)  
    type('c', KeyModifier.CMD) 
    
    cb = getClipboardData() #Env.getClipboard()
    print "The clipboard contains: " + str(cb)

def getClipboardData(): 
    p = subprocess.Popen(['pbpaste'], stdout=subprocess.PIPE) 
    retcode = p.wait() 
    data = p.stdout.read() 
  
    return data

def newAlert(region):
    print 'new alert!'

    # Copy the alert to the clipboard
    region.doubleClick(Pattern("1397192275376.png").targetOffset(-83,19))
    type('a', KeyModifier.CMD)  
    type('c', KeyModifier.CMD) 
    
    tradeData = getClipboardData() #Env.getClipboard()

### MAIN ###
def main(args):
    debugFlag = False

    if debugFlag == True:
        debug()
        sys.exit()
    
    # Make sure we are in the chat room
    click("1381195946550.png")
    click("1397444289724.png") 
    
    # Infinite loop looking at chat room
    region = Region(480,684,1067,202)
    
    print datetime.now().time()
    while datetime.now().strftime('%H:%M') < '23:00':     
        time.sleep(1)
    
        try:
            # Make sure we're in 'omniTweet'
            # If an announcement has appeared
            if region.exists("1397191707592.png"):
                print 'a'
                newAlert(region)
            else:
                print 'e'
            
            # Look for the announcement not from Jason Bonds
            if region.exists("1397191952703.png"):
                print 'b'
                # If this happens to be from Jason, call the alert 
                if region.exists("1397191707592.png"):
                    print 'c'
                    newAlert(region)
                # Otherwise, close the alert 
                else:
                    print 'd'
                    region.click("1397191952703.png")
            else:
                print 'f'
        except:
            print 'Error'
            
if __name__ == '__main__':
  main(sys.argv)

# vim: ts=8 et sw=4 sts=4 background=light