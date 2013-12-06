package com.IBTrading.Traders;

import java.util.HashMap;

import com.IBTrading.servlets.IBTradingAPI;
import com.IBTrading.tradeparsers.JasonBondsTradeParser;
import com.IBTrading.servlets.OrderStatus;
import com.IBTrading.servlets.Database;

public class DebugTrader extends Trader{
	// Passed parameters
	private String tradeString;
	private boolean websiteMonitorFlag;
	
	// Parsed trade information
	JasonBondsTradeParser parser;
	
	// Public variables
	
	// CONSTANTS
	private final int SECONDS = 1000;
	private final String BUY = "BUY";
	private final String SELL = "SELL";
	private final int TRADERPERCENTAGE = 100;
	private final int MAXLEVERAGE = 4;
	private final int NOLEVERAGE = 1;
	private final String VOLUME = "VOLUME";
	private final String AVERAGEVOLUME = "AVG_VOLUME";
	
	public DebugTrader(String newTrade, IBTradingAPI newTradingAPI, boolean newRealTimeSystem)
	{	
		super(newTradingAPI);
		
		websiteMonitorFlag = newRealTimeSystem;
		parser = new JasonBondsTradeParser(newTrade);
		hasValidTrade = parser.parseTrade();
	}

	// Initiates the trade with TWS
	public String trade()
	{
		// Make the purchase
		boolean isSimulation = true;
		boolean simulationOnly = true;
		int simulationQuantity = (parser.quantity * TRADERPERCENTAGE) / 100;
		int quantity;
		int maxCash;
		int maxCashForAdds = 5900;
		int totalCash;
		OrderStatus buyOrderStatus, sellOrderStatus;
		
		// Get the cash from our account
		totalCash = (int) tradingAPI.getAvailableFunds(false); //tradingAPI.getAvailableFunds(isSimulation);
		
		// If something went wrong and we were unable to get the cash
		// then we cannot get the market data either
		if(totalCash == 0)
		{
			return "Cannot read market data from simulation account.";
		}
		else
		{
			isSimulation = false;
		}
		
		// Give a little wiggle room of ~2% for the maxCash
		maxCash = super.getCash(totalCash, MAXLEVERAGE);
		
		// Wait until we have received the market data
		String marketData = "LAST_PRICE";
		int tickerID = tradingAPI.subscribeToMarketData(parser.symbol);
		while(tradingAPI.getMarketData(tickerID, marketData) == 0.0){};
		
		// If the price/share of the stock was not supplied, get this information from TWS
		if(parser.price.equalsIgnoreCase("0.00"))
		{
			// Get the market price
			parser.price = tradingAPI.getMarketData(tickerID, marketData) + "";
		}
		
		try
		{
			if(parser.action.equalsIgnoreCase("Added"))
				quantity = super.getQuantity(maxCashForAdds, Double.parseDouble(parser.price), TRADERPERCENTAGE, parser.quantity);
			else
				quantity = super.getQuantity(maxCash, Double.parseDouble(parser.price), TRADERPERCENTAGE, parser.quantity);
		}catch(Exception e)
		{
			e.printStackTrace();
			quantity = (parser.quantity * TRADERPERCENTAGE) / 100;
		}
		
		if(quantity <= 0)
		{
			return "Invalid quantity, " + quantity;
		}
		
		// In some circumstances, we don't want to buy with the real money account
		// 		- If we have reached our maximum number of day trades
		//		- If this is an 'Add'
		//		- If this is a 'Bomb Blow Up'
		if( (tradingAPI.getNumberOfDayTrades() == 0) || (parser.action.equalsIgnoreCase("Added")) 
				|| (parser.flagsHashMap.get(parser.BONDBLOWUPS) == true) )
			simulationOnly = true;
		else
			simulationOnly = true;
		
		/*
		orderStatus = tradingAPI.placeOrder(BUY, parser.symbol, quantity, isSimulation);
		
		if(orderStatus == null)
			return "Unable to connect to TWS...";
		*/
		
		// Make the purchase with the Simulator
		isSimulation = true;
		simulationQuantity = quantity;
		buyOrderStatus = tradingAPI.placeOrder(BUY, parser.symbol, simulationQuantity, isSimulation, null);  

		if( (buyOrderStatus == null) || (buyOrderStatus.status == null) )
			return "Unable to connect to TWS...";
		
		// Sleep for 60 seconds, then sell
		try
		{			
			int timeTilSell = 5;  // FIXME: 60 seconds 
			boolean cashOnlyOrderFlag = false;
			
			// Check the desired information every second for 60 seconds
			for(int numSeconds = 0; numSeconds < timeTilSell; numSeconds++)
			{
				//System.out.println("orderID = " + orderStatus.orderId + ", orderStatus = " + orderStatus.status);
				
				// If we were unable to buy due to the stock being not marginable
				if( (buyOrderStatus.status.equalsIgnoreCase("Inactive") == true) && (cashOnlyOrderFlag == false) )
				{
					System.out.println("Order was rendered inactive, trying again with our total cash, " + totalCash);
					cashOnlyOrderFlag = true;
					
					// Make the trade using only cash (no leverage)
					int cash = super.getCash(totalCash, NOLEVERAGE);
					quantity = super.getQuantity(cash, Double.parseDouble(parser.price), TRADERPERCENTAGE, parser.quantity);
					buyOrderStatus = tradingAPI.placeOrder(BUY, parser.symbol, quantity, isSimulation, null);
					
					if( (buyOrderStatus == null) || (buyOrderStatus.status == null) )
						return "Unable to connect to TWS...";
					
					// Sleep for the remaining time
					Thread.sleep((timeTilSell - numSeconds) * SECONDS);
					
					// We are done sleeping, sell!
					break;
				}
				
				// Sleep for one second
				Thread.sleep( 1 * SECONDS );
			}
				
			// If the order was unsuccessful, exit
			if( (buyOrderStatus == null) || (buyOrderStatus.status == null) || (buyOrderStatus.status.equalsIgnoreCase("Inactive") == true) 
					|| buyOrderStatus.status.equalsIgnoreCase("Cancelled") || buyOrderStatus.status.equalsIgnoreCase("PendingCancel") )
			{
				return "We were unable to purchase - " + buyOrderStatus.status;
			}
			
			// Cancel the order if we have not purchased any stock
			if(buyOrderStatus.filled == 0)
			{
				isSimulation = true;
				tradingAPI.cancelOrder(buyOrderStatus, isSimulation);
				return "Order canceled - we did not buy within the time limit";
			}
			// If we have not completed the order, complete it
			else if(buyOrderStatus.status.equalsIgnoreCase("Filled") == false)
			{
				isSimulation = true;
				tradingAPI.cancelOrder(buyOrderStatus, isSimulation);
				
				// Wait until we have either completed the order or it is cancelled
				while( (buyOrderStatus.status.equalsIgnoreCase("Cancelled") == false) &&
						(buyOrderStatus.status.equalsIgnoreCase("Filled") == false) ){};
						
				// Get the quantity to sell
				simulationQuantity = buyOrderStatus.filled;
			}
		}
		catch ( InterruptedException e )
		{
			System.out.println( "awakened prematurely" );
		}
		
		/*
		// Sell the stocks
		isSimulation = false;
		sellOrderStatus = tradingAPI.placeOrder(SELL, parser.symbol, quantity, isSimulation);
		
		if(orderStatus == null)
			return "Unable to connect to TWS...";
		*/
		
		// Sell the stocks over the simulator
		isSimulation = true;
		sellOrderStatus = tradingAPI.placeOrder(SELL, parser.symbol, simulationQuantity, isSimulation, null);
		
		// Save off useful trade information for the purchase
		tradeInfo.put(Database.NUMBEROFSHARES, quantity);
		tradeInfo.put(Database.AVERAGEBUYINGPRICE, buyOrderStatus.avgFillPrice);
		tradeInfo.put(Database.INITIALVOLUME, tradingAPI.getMarketData(tickerID, VOLUME));
		
		// Get the updated market data
		marketData = "VOLUME";
		tickerID = tradingAPI.subscribeToMarketData(parser.symbol);
		while(tradingAPI.getMarketData(tickerID, marketData) == 0.0){};
		
		// Save the volume now that the purchase is over
		tradeInfo.put(Database.VOLUMEAFTERPURCHASE, tradingAPI.getMarketData(tickerID, VOLUME));
		
		// Wait for the sell to be complete
		while(sellOrderStatus.filled != 0){};
		
		// Get the updated marketData
		marketData = "VOLUME";
		tickerID = tradingAPI.subscribeToMarketData(parser.symbol);
		while(tradingAPI.getMarketData(tickerID, marketData) == 0.0){};
		
		// Save the last bits of information from the sell
		tradeInfo.put(Database.FINALVOLUME, tradingAPI.getMarketData(tickerID, VOLUME));
		tradeInfo.put(Database.AVERAGEVOLUME, tradingAPI.getMarketData(tickerID, AVERAGEVOLUME));
		tradeInfo.put(Database.AVERAGESELLINGPRICE, sellOrderStatus.avgFillPrice);

		return null;
	}
}