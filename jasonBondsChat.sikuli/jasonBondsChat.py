from datetime import datetime, date, timedelta
import time

# Make sure we are in the chat room
click("1381195946550.png")

# Infinite loop looking at chat room
print datetime.now().time()
print 
while datetime.now().strftime('%H:%M') < '20:00':     
    time.sleep(1)

    try:

        if exists("1381358636912.png"):
            click(Pattern("1381358937054.png").targetOffset(0,35))
            rightClick(Pattern("1381358937054.png").targetOffset(0,35))
            click("SelectAll-1.png")
            rightClick(Pattern("1381358937054.png").targetOffset(0,35))
            click("Copy.png")
        
            print 'Clicked copy'
    
    except:
        print 'Error'
            
    




