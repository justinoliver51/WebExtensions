package com.IBTrading.Traders;

import com.IBTrading.servlets.IBTradingAPI;
import com.IBTrading.tradeparsers.AwesomePennyStocksParser;

public class AwesomePennyStocksTrader extends Trader
{
	// Passed parameters
	private String tradeString;
	private boolean isSimulation;
	
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
	
	public AwesomePennyStocksTrader(String newTrade, IBTradingAPI newTradingAPI, boolean simulation)
	{	
		super(newTradingAPI);
		
		// If we have already parsed this string, return
		if( (newTrade != null) && (newTrade.equalsIgnoreCase(lastTraderString)) )
		{
			hasValidTrade = false;
			System.out.println("Duplicate trade, " + tradeString);
			return;
		}
		
		isSimulation = simulation;
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
		if(isSimulation == false)
			return "No purchases for AwesomePennyStocks with real money";
		
		// Make the purchase
		String tradeError = tradingAPI.placeOrder(BUY, parser.symbol, (parser.quantity * TRADERPERCENTAGE) / 100);
		
		if(tradeError != null)
			return tradeError;
		
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
		return tradingAPI.placeOrder(SELL, parser.symbol, (parser.quantity * TRADERPERCENTAGE) / 100);
	}
}