package com.Trading.traders;

import com.Trading.ib.IBTradingAPI;
import com.Trading.ib.OrderStatus;
import com.Trading.tradeparsers.AwesomePennyStocksParser;

public class AwesomePennyStocksTrader extends Trader
{
	// Passed parameters
	private String tradeString;
	private boolean websiteMonitorFlag;
	
	// Parsed trade information
	AwesomePennyStocksParser parser;
	
	// PROFIT.LY
	// List of trader identifiers and their strings
	private static String lastTraderString;
	
	// CONSTANTS
	private final int SECONDS = 1000;
	private final String BUY = "BUY";
	private final String SELL = "SELL";
	private static int TRADERPERCENTAGE = 25;
	
	public AwesomePennyStocksTrader(String newTrade, IBTradingAPI newTradingAPI, boolean newRealTimeSystem)
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
		parser = new AwesomePennyStocksParser(newTrade);
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
		boolean isSimulation = true;
		OrderStatus orderStatus = tradingAPI.placeOrder(BUY, parser.symbol, (parser.quantity * TRADERPERCENTAGE) / 100, isSimulation, null);
		
		if(orderStatus == null)
			return "Unable to connect to TWS...";
		
		// Sleep for 90 seconds, then sell
		try
		{
			Thread.sleep( 60 * SECONDS );
		}
		catch ( InterruptedException e )
		{
			System.out.println( "awakened prematurely" );
		}
		
		// Sell the stocks
		orderStatus = tradingAPI.placeOrder(SELL, parser.symbol, (parser.quantity * TRADERPERCENTAGE) / 100, isSimulation, null);
		
		if(orderStatus == null)
			return "Unable to connect to TWS...";
		
		return null;
	}
}