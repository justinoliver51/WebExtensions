package com.IBTrading.Traders;

import com.IBTrading.servlets.IBTradingAPI;
import com.IBTrading.tradeparsers.AwesomePennyStocksParser;

public class AwesomePennyStocksTrader extends Trader
{
	// Passed parameters
	private String tradeString;
	
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
	
	public AwesomePennyStocksTrader(String newTrade, IBTradingAPI newTradingAPI)
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
		parser = new AwesomePennyStocksParser(newTrade);
		hasValidTrade = parser.parseTrade();
		if(hasValidTrade == true)
		{
			lastTraderString = newTrade;
			hasValidTrade = true;
		}
	}
	
	// Parses the passed trade into the necessary information for the trade
	public boolean parseTrade(String newTrade)
	{
		return false;
	}

	// Initiates the trade with TWS
	public boolean trade()
	{
		// Make the purchase
		tradingAPI.placeOrder(BUY, parser.symbol, (parser.quantity * TRADERPERCENTAGE) / 100);
		
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
		tradingAPI.placeOrder(SELL, parser.symbol, (parser.quantity * TRADERPERCENTAGE) / 100);
		
		return true;
	}
}