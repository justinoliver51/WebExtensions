from datetime import datetime, date, timedelta
import time

def newAlert():
    print 'new alert!'

    # Copy the alert to the clipboard
    click(Pattern("1397192275376.png").targetOffset(-83,19))
    rightClick(Pattern("1397192275376.png").targetOffset(-83,19))
    click("SelectAll-1.png")
    rightClick(Pattern("1397192275376.png").targetOffset(-83,19))
    click("Copy.png")

    # Exit the announcement
    click("1397191952703.png")

# Make sure we are in the chat room
click("1381195946550.png")

# Infinite loop looking at chat room
region = Region(245,644,1372,256)

print datetime.now().time()
print 'a'
while datetime.now().strftime('%H:%M') < '20:00':     
    time.sleep(1)

    try:
        # Make sure we're in 'omniTweet'
        # If an announcement has appeared
        if exists("1397191707592.png"):
            newAlert()
        
        # Look for the announcement not from Jason Bonds
        if exists("1397191952703.png"):
            # If this happens to be from Jason, call the alert 
            if exists("1397191707592.png"):
                newAlert()
           # Otherwise, close the alert 
            else:
               click("1397191952703.png")
                

#        if exists("1381358636912.png"):
#            click(Pattern("1381358937054.png").targetOffset(0,35))
#            rightClick(Pattern("1381358937054.png").targetOffset(0,35))
#            click("SelectAll-1.png")
#            rightClick(Pattern("1381358937054.png").targetOffset(0,35))
#            click("Copy.png")
        
#            print 'Clicked copy'
    
    except:
        print 'Error'
            
    




