package com.IBTrading.Traders;

import com.IBTrading.servlets.IBTradingAPI;
import com.IBTrading.tradeparsers.JasonBondsTradeParser;
import com.IBTrading.servlets.OrderStatus;

public class DebugTrader extends Trader{
	// Passed parameters
	private String tradeString;
	private boolean websiteMonitorFlag;
	
	// Parsed trade information
	JasonBondsTradeParser parser;
	
	// List of trader identifiers and their strings
	private static String lastTraderString;
	
	// CONSTANTS
	private final int SECONDS = 1000;
	private final String BUY = "BUY";
	private final String SELL = "SELL";
	private static int TRADERPERCENTAGE = 100;
	
	public DebugTrader(String newTrade, IBTradingAPI newTradingAPI, boolean newRealTimeSystem)
	{	
		super(newTradingAPI);
		
		// If we have already parsed this string, return
		if( (newTrade != null) && (newTrade.equalsIgnoreCase(lastTraderString)) )
		{
			hasValidTrade = false;
			System.out.println("Duplicate trade, " + tradeString);
			return;
		}
		
		websiteMonitorFlag = newRealTimeSystem;
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
		boolean isSimulation = false;
		int simulationQuantity = (parser.quantity * TRADERPERCENTAGE) / 100;
		int quantity;
		int maxCash = 18000;
		int maxCashForAdds = 6000;
		int totalCash = 5900;
		OrderStatus orderStatus;
		
		try
		{
			if(parser.action.equalsIgnoreCase("Added"))
				quantity = super.setQuantity(maxCashForAdds, Double.parseDouble(parser.price), TRADERPERCENTAGE, parser.quantity);
			else
				quantity = super.setQuantity(maxCash, Double.parseDouble(parser.price), TRADERPERCENTAGE, parser.quantity);
		}catch(Exception e)
		{
			e.printStackTrace();
			quantity = (parser.quantity * TRADERPERCENTAGE) / 100;
		}
		/*
		orderStatus = tradingAPI.placeOrder(BUY, parser.symbol, quantity, isSimulation);
		
		if(orderStatus == null)
			return "Unable to connect to TWS...";
		*/
		// Make the purchase with the Simulator
		isSimulation = true;
		orderStatus = tradingAPI.placeOrder(BUY, parser.symbol, simulationQuantity, isSimulation);
		
		// Sleep for 60 seconds, then sell
		try
		{
			// Check the desired information every second for 60 seconds
			for(int numSeconds = 0; (numSeconds < 60) || ((numSeconds >= 60) && (orderStatus.status.equalsIgnoreCase("Filled") == false)); numSeconds++)
			{
				Thread.sleep( 1 * SECONDS );
				numSeconds++;
				System.out.println("orderID = " + orderStatus.orderId + ", orderStatus = " + orderStatus.status);
			}
		}
		catch ( InterruptedException e )
		{
			System.out.println( "awakened prematurely" );
		}
		/*
		// Sell the stocks
		isSimulation = false;
		orderStatus = tradingAPI.placeOrder(SELL, parser.symbol, quantity, isSimulation);
		
		if(orderStatus == null)
			return "Unable to connect to TWS...";
		*/
		// Sell the stocks over the simulator
		isSimulation = true;
		tradingAPI.placeOrder(SELL, parser.symbol, simulationQuantity, isSimulation);
		
		return null;
	}
}