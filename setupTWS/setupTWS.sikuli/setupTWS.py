import time

# Set up TraderWorkStation for simulation
# Load the app
click("1381191081596.png")

click(Pattern("Trader_WorkStation_X.png").similar(0.95), 5)

# Login
wait("1381187291514.png", 60)
type("1381187066152.png", 'justin052')
type("1381187105489.png", 'jus0519')
click("1381187328442.png")

# Remove the warning if it exists
if(exists("1381187496742.png", 60)):
    click("1381187799943.png")

# Minimize TraderWorkStation
for button in findAll("1381187908576.png"):
    click(button)
    

# Set up TraderWorkStation_Copy for real money
click("1381191081596.png")

click(Pattern("Trader_WorkStation_X_copy.png").similar(0.90), 5)

# Login
wait("1381187291514.png", 60)
type("1381187066152.png", 'justin051')
type("1381187105489.png", 'jus0519')
click("1381187328442.png")

# Remove the warning if it exists
if(exists("1381187496742.png", 60)):
    click("1381187799943.png")

# Minimize TraderWorkStation
for button in findAll("1381187908576.png"):
    click(button)

# exists("1381887496521.png")
