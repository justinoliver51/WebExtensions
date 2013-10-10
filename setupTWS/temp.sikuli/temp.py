click("MacSearchBar.png")

type('trader_workstation_x.app')
click(Pattern("Trader_WorkStation_X-1.png").similar(0.90).targetOffset(-45,0), 5)

# Fixing bug related to the start menu
if(exists("Menu_Trader_Workstation_X_Copy.png", 10)):
    click(Pattern("Trader_WorkStation_X-1.png").similar(0.90).targetOffset(-45,0), 5) 
    click(Pattern("1381190286499.png").targetOffset(15,-5))
    doubleClick(Pattern("Trader_WorkStation_X-1.png").similar(0.90).targetOffset(-45,0), 5)
elif(exists("1381189718398.png", 10)):
    click("1381189718398.png", 5)
    click(Pattern("1381190286499.png").targetOffset(15,-5))
    doubleClick(Pattern("Trader_WorkStation_X-1.png").similar(0.90).targetOffset(-45,0), 5)