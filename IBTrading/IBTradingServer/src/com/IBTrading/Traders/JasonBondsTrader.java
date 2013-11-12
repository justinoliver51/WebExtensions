package com.IBTrading.Traders;

import com.IBTrading.servlets.IBTradingAPI;
import com.IBTrading.tradeparsers.JasonBondsTradeParser;
import com.IBTrading.servlets.OrderStatus;

public class JasonBondsTrader extends Trader{
	// Passed parameters
	private String tradeString;
	
	// Parsed trade information
	JasonBondsTradeParser parser;
	
	// List of trader identifiers and their strings
	private static String lastTraderString;
	
	// CONSTANTS
	private final int SECONDS = 1000;
	private final String BUY = "BUY";
	private final String SELL = "SELL";
	private static int TRADERPERCENTAGE = 100;
	
	public JasonBondsTrader(String newTrade, IBTradingAPI newTradingAPI, boolean newRealTimeSystem)
	{	
		super(newTradingAPI);
		
		// If we have already parsed this string, return
		if( (newTrade != null) && (newTrade.equalsIgnoreCase(lastTraderString)) )
		{
			hasValidTrade = false;
			System.out.println("Duplicate trade, " + tradeString);
			return;
		}
		
		lastTraderString = newTrade;
		parser = new JasonBondsTradeParser(newTrade);
		hasValidTrade = parser.parseTrade();
		if(hasValidTrade == true)
		{
			lastTraderString = newTrade;
			hasValidTrade = true;
		}
	}

	// Initiates the trade with TWS
	public String trade()
	{
		// Make the purchase
		boolean isSimulation;
		int quantity;
		int maxCash = 23500;		// The maximum amount of cash to use on a trade
		int maxCashAdded = 6000;	// The maximum amount of cash to spend on an 'Add'
		int totalCash = 6000;		// The total amount of cash in the account
		int simulationQuantity = (parser.quantity * TRADERPERCENTAGE) / 100;
		OrderStatus orderStatus = new OrderStatus();
		
		try
		{
			if(parser.action.equalsIgnoreCase("Added"))
				quantity = super.setQuantity(maxCashAdded, Double.parseDouble(parser.price), TRADERPERCENTAGE, parser.quantity);
			else
				quantity = super.setQuantity(maxCash, Double.parseDouble(parser.price), TRADERPERCENTAGE, parser.quantity);
		}catch(Exception e)
		{
			e.printStackTrace();
			quantity = (parser.quantity * TRADERPERCENTAGE) / 100;
		}
		
		// Do not buy with real money if this is an 'Add'
		if(parser.action.equalsIgnoreCase("Added") == false)
		{
			isSimulation = false;
			orderStatus = tradingAPI.placeOrder(BUY, parser.symbol, quantity, isSimulation);
		}
		
		if(orderStatus == null)
			return "Unable to connect to TWS...";
		
		// Make the purchase with the Simulator
		isSimulation = true;
		tradingAPI.placeOrder(BUY, parser.symbol, simulationQuantity, isSimulation);
		
		// Sleep for 60 seconds, then sell.  If order is rendered "Inactive", try to 
		// buy using only the available cash in the account.
		try
		{			
			int timeTilSell = 60;
			
			// Check the desired information every second for 60 seconds
			for(int numSeconds = 0; numSeconds < timeTilSell; numSeconds++)
			{
				//System.out.println("orderID = " + orderStatus.orderId + ", orderStatus = " + orderStatus.status);
				
				// If we were unable to buy due to the stock being not marginable
				if(orderStatus.status.equalsIgnoreCase("Inactive") == true)
				{
					System.out.println("Order was rendered inactive, trying again with our total cash, " + totalCash);
					
					// Make the trade using only cash (no leverage)
					quantity = super.setQuantity(totalCash, Double.parseDouble(parser.price), TRADERPERCENTAGE, parser.quantity);
					orderStatus = tradingAPI.placeOrder(BUY, parser.symbol, quantity, isSimulation);
					
					if(orderStatus == null)
						return "Unable to connect to TWS...";
					
					// Sleep for the remaining time
					Thread.sleep(timeTilSell - (numSeconds * SECONDS));
					
					break;
				}
				
				// Sleep for one second
				Thread.sleep( 1 * SECONDS );
			}
		}
		catch ( InterruptedException e )
		{
			System.out.println( "awakened prematurely" );
		}
		
		// SELL THE STOCKS
		// Do not sell with real money if this is an 'Add'
		if(parser.action.equalsIgnoreCase("Added") == false)
		{
			isSimulation = false;
			orderStatus = tradingAPI.placeOrder(SELL, parser.symbol, quantity, isSimulation);
		}
		
		if(orderStatus == null)
			return "Unable to connect to TWS...";
		
		// Sell the stocks over the simulator
		isSimulation = true;
		tradingAPI.placeOrder(SELL, parser.symbol, simulationQuantity, isSimulation);
		
		return null;
	}
}