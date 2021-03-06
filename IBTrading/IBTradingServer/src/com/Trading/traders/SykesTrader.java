package com.Trading.traders;

import com.Trading.ib.IBTradingAPI;
import com.Trading.ib.OrderStatus;
import com.Trading.tradeparsers.ProfitlyTradeParser;

public class SykesTrader extends Trader
{
	// Passed parameters
	private String tradeString;
	private boolean websiteMonitorFlag;
	
	// Parsed trade information
	ProfitlyTradeParser parser;
	
	// PROFIT.LY
	// List of trader identifiers and their strings
	private static String lastTraderString;
	
	// CONSTANTS
	private final int SECONDS = 1000;
	private final String BUY = "BUY";
	private final String SELL = "SELL";
	private static int TRADERPERCENTAGE = 25;
	
	public SykesTrader(String newTrade, IBTradingAPI newTradingAPI, boolean newRealTimeSystem)
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
		parser = new ProfitlyTradeParser(newTrade, websiteMonitorFlag);
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
		OrderStatus orderStatus;
		
		try
		{
			quantity = super.getQuantity(maxCash, Double.parseDouble(parser.price), TRADERPERCENTAGE, parser.quantity);
		}catch(Exception e)
		{
			e.printStackTrace();
			quantity = (parser.quantity * TRADERPERCENTAGE) / 100;
		}

		// Make the purchase with the Simulator
		isSimulation = true;
		orderStatus = tradingAPI.placeOrder(BUY, parser.symbol, simulationQuantity, isSimulation, null);
		
		// Sleep for 60 seconds, then sell
		try
		{
			// Check the desired information every second for 60 seconds
			for(int numSeconds = 0; numSeconds < 60; numSeconds++)
			{
				Thread.sleep( 1 * SECONDS );
				numSeconds++;
				
				System.out.println(orderStatus.orderId + "");
				
				//OrderStatus orderStatus = tradingAPI.getOrderStatus(orderId)
			}
		}
		catch ( InterruptedException e )
		{
			System.out.println( "awakened prematurely" );
		}

		// Sell the stocks over the simulator
		isSimulation = true;
		tradingAPI.placeOrder(SELL, parser.symbol, simulationQuantity, isSimulation, null);
		
		return null;
	}
}