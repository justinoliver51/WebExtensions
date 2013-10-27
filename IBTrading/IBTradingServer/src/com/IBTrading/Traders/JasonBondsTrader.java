package com.IBTrading.Traders;

import com.IBTrading.servlets.IBTradingAPI;
import com.IBTrading.tradeparsers.JasonBondsTradeParser;

public class JasonBondsTrader extends Trader{
	// Passed parameters
	private String tradeString;
	private boolean websiteMonitorFlag;
	
	// Parsed trade information
	JasonBondsTradeParser parser;
	
	// PROFIT.LY
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
		int maxCash = 12000;
		
		try
		{
			quantity = super.setQuantity(maxCash, Double.parseDouble(parser.price), TRADERPERCENTAGE, parser.quantity);
		}catch(Exception e)
		{
			e.printStackTrace();
			quantity = (parser.quantity * TRADERPERCENTAGE) / 100;
		}
		String tradeError = tradingAPI.placeOrder(BUY, parser.symbol, quantity, isSimulation);
		
		if(tradeError != null)
			return tradeError;
		
		// Make the purchase with the Simulator
		isSimulation = true;
		tradeError = tradingAPI.placeOrder(BUY, parser.symbol, simulationQuantity, isSimulation);
		
		if(tradeError != null)
			return tradeError;
		
		// Sleep for 60 seconds, then sell
		try
		{
			Thread.sleep( 60 * SECONDS );
		}
		catch ( InterruptedException e )
		{
			System.out.println( "awakened prematurely" );
		}
		
		// Sell the stocks
		isSimulation = false;
		tradeError = tradingAPI.placeOrder(SELL, parser.symbol, quantity, isSimulation);
		
		if(tradeError != null)
			return tradeError;
		
		// Sell the stocks over the simulator
		isSimulation = true;
		return tradingAPI.placeOrder(SELL, parser.symbol, simulationQuantity, isSimulation);
	}
}