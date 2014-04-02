package com.Trading.traders;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import com.Trading.ib.HistoricalData;
import com.Trading.ib.IBTradingAPI;
import com.Trading.ib.OrderStatus;
import com.Trading.servlets.Database;
import com.Trading.tradeparsers.JasonBondsTradeParser;

public class JasonBondsTrader extends Trader{
	// Passed parameters
	private String tradeString;
	private boolean marketOpenFlag = false;
	
	// Parsed trade information
	JasonBondsTradeParser parser;
	
	// List of trader identifiers and their strings
	private static ArrayList<String> lastTradeStrings = new ArrayList<String>();
	
	// CONSTANTS
	private final int SECONDS = 1000;
	private final String BUY = "BUY";
	private final String SELL = "SELL";
	private static final int TRADERPERCENTAGE = 100;
	private static final int MAXLEVERAGE = 4;
	private static final int NOLEVERAGE = 1;
	private final String VOLUME = "VOLUME";
	
	// Trade information
	public String parsedSymbol = "";
	public int parsedQuantity = 0;
	public String parsedAction = "";
	public String parsedPrice = "";
	public HashMap<String, Boolean> parsedFlagsHashMap = new HashMap<String, Boolean>();
	
	public JasonBondsTrader(String newTrade, IBTradingAPI newTradingAPI, boolean newRealTimeSystem, boolean newMarketOpenFlag)
	{	
		super(newTradingAPI);
		
		// If we have already parsed this string, return
		for(String tradeString : lastTradeStrings)
		{
			if( (newTrade != null) && (newTrade.equalsIgnoreCase(tradeString)) )
			{
				hasValidTrade = false;
				System.out.println("Duplicate trade, " + tradeString);
				return;
			}
		}
		
		marketOpenFlag = newMarketOpenFlag;
		parser = new JasonBondsTradeParser(newTrade);
		hasValidTrade = parser.parseTrade();
		if(hasValidTrade == true)
		{
			lastTradeStrings.add(newTrade);
		}
		
		parsedSymbol = parser.symbol;
		parsedQuantity = parser.quantity;
		parsedAction = parser.action;
		parsedPrice = parser.price;
		parsedFlagsHashMap = parser.flagsHashMap;
	}
	
	private int getHistoricalData(int durationInt, String durationStr, String endDateTime, String barSizeSetting, String whatToShow)
	{
		int tickerID = tradingAPI.subscribeToHistoricalData(parsedSymbol, endDateTime, durationStr, barSizeSetting, whatToShow);
		
		if(tickerID == -1)
			return tickerID;
		
		// Wait until we have received the market data
		while(tradingAPI.getHistoricalData(tickerID) == null){};
		
		return tickerID;
	}
	
	// Determines if this trade may be profitable
	public boolean isTradeable()
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Calendar cal = Calendar.getInstance();
        
        // Calculate the appropriate endDateTime
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        
        // If it is a Sunday or Monday, use Friday's date
        if(dayOfWeek == 1)
        	cal.add(Calendar.DATE, -2); 
        else if(dayOfWeek == 2)
        	cal.add(Calendar.DATE, -3); 
        // Use yesterday's date
        else 
        	cal.add(Calendar.DATE, -1);    
        	  
        String endDateTime = dateFormat.format(cal.getTime()) + " 15:00:00 CST";
		
		// Get the data over the last 30 days
		String barSizeSetting = IBTradingAPI.ONEDAY;
		String whatToShow = IBTradingAPI.TRADES;
		
		// Gets the VWAP of yesterday, last week, and last month
		String durationStr = IBTradingAPI.ONEDAYINTEGER + IBTradingAPI.DAYS;
		int tickerID = getHistoricalData(IBTradingAPI.ONEDAYINTEGER, durationStr, endDateTime, barSizeSetting, whatToShow);
		ArrayList<HistoricalData> historicalDataArray = tradingAPI.getHistoricalData(tickerID);
		totalCashTradedYesterday = historicalDataArray.get(0).totalCashTradedInInterval;
		
		if(tickerID == -1)
			return false;
		
		durationStr = IBTradingAPI.ONEWEEKINTEGER + IBTradingAPI.DAYS;
		tickerID = getHistoricalData(IBTradingAPI.ONEWEEKINTEGER, durationStr, endDateTime, barSizeSetting, whatToShow);
		historicalDataArray = tradingAPI.getHistoricalData(tickerID);
		for(int i = 0; i < historicalDataArray.size(); i++)
		{
			totalCashTradedLastWeek += historicalDataArray.get(i).totalCashTradedInInterval;
		}
		
		if(tickerID == -1)
			return false;
		
		durationStr = IBTradingAPI.ONEMONTHINTEGER + IBTradingAPI.DAYS;
		tickerID = getHistoricalData(IBTradingAPI.ONEMONTHINTEGER, durationStr, endDateTime, barSizeSetting, whatToShow);
		historicalDataArray = tradingAPI.getHistoricalData(tickerID);
		for(int i = 0; i < historicalDataArray.size(); i++)
		{
			totalCashTradedLastMonth += historicalDataArray.get(i).totalCashTradedInInterval;
		}
		
		if(tickerID == -1)
			return false;
		
		// Get the information about the volume of the stock over the last 30 minutes
        // If it is a Sunday or Monday, use Friday's date
		
        if(dayOfWeek != 1 && dayOfWeek != 2)
        	cal.add(Calendar.DATE, 1);
		
		barSizeSetting = IBTradingAPI.ONEMINUTE;
		dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		endDateTime = dateFormat.format(cal.getTime()) + " CST";

		// Get the average/medium volume from the last 30 minutes
		ArrayList<Integer> volumeList = new ArrayList<Integer>();
		durationStr = IBTradingAPI.EIGHTEENHUNDREDSECONDINTEGER + IBTradingAPI.SECONDS;
		tickerID = getHistoricalData(IBTradingAPI.THIRTYMINUTEINTEGER, durationStr, endDateTime, barSizeSetting, whatToShow);
		historicalDataArray = tradingAPI.getHistoricalData(tickerID);
		for(int i = 0; i < historicalDataArray.size(); i++)
		{
			volumeList.add(historicalDataArray.get(i).volume);
			averageVolumeInLast30Minutes += historicalDataArray.get(i).volume;
		}
		
		if(tickerID == -1)
			return false;

		java.util.Collections.sort(volumeList);
		medianVolumeInLast30Minutes = volumeList.get(volumeList.size()/2);
		averageVolumeInLast30Minutes /= volumeList.size();
		
		System.out.println("Median Volume in the last 30 minutes: " + medianVolumeInLast30Minutes);
		System.out.println("Average Volume in the last 30 minutes: " + averageVolumeInLast30Minutes);
		
		// If the total amount of money thrown around is estimated to move the market, go ahead
		if(totalCashTradedYesterday < 999999999.9)
			return true;
		else
			return false;
	}

	// Initiates the trade with TWS
	public String trade()
	{
		// Make the purchase
		boolean isSimulation = false;
		boolean simulationOnly = false;
		int quantity;
		int maxCash;
		int totalCash;
		OrderStatus buyOrderStatus = null, sellOrderStatus = null;
		
		// Get the cash from our account
		totalCash = (int) tradingAPI.getAvailableFunds(isSimulation);
		
		// If something went wrong and we were unable to get the cash
		if(totalCash == 0)
			totalCash = 5900;
		
		// Get the cash
		maxCash = super.getCash(totalCash, MAXLEVERAGE);
		
		// Wait until we have received the market data
		String marketData;
		if(marketOpenFlag == true)
			marketData = "LAST_PRICE";  
		else
			marketData = "CLOSE_PRICE";	// Should only be using this in debug
		
		int tickerID = tradingAPI.subscribeToMarketData(parsedSymbol);
		while(tradingAPI.getMarketData(tickerID, marketData) == null){};
		
		// Get the market price
		parsedPrice = tradingAPI.getMarketData(tickerID, marketData) + "";
		
		// If the price/share of the stock was not supplied, get this information from TWS
		if(parsedPrice.equalsIgnoreCase("0.00"))
		{
			// Get the market price
			parsedPrice = tradingAPI.getMarketData(tickerID, marketData) + "";
		}
		
		try
		{
			quantity = super.getQuantity(maxCash, Double.parseDouble(parsedPrice), TRADERPERCENTAGE, parsedQuantity);
		}catch(Exception e)
		{
			e.printStackTrace();
			quantity = (parsedQuantity * TRADERPERCENTAGE) / 100;
		}
		
		if(quantity <= 0)
		{
			return "Invalid quantity, " + quantity;
		}
		
		// In some circumstances, we don't want to buy with the real money account
		// 		- If we have reached our maximum number of day trades
		//		- If this is an 'Add'
		//		- If this is a 'Bomb Blow Up'
		if( (tradingAPI.getNumberOfDayTrades() == 0) || (parsedAction.equalsIgnoreCase("Added")) 
				|| (parsedFlagsHashMap.get(parser.BONDBLOWUPS) == true) || (marketOpenFlag == false) 
				|| (super.isBlackListed(parsedSymbol) == true) )
			simulationOnly = true;
		else
			simulationOnly = false;
		
		// Place the order
		if(simulationOnly == false)
			buyOrderStatus = tradingAPI.placeOrder(BUY, parsedSymbol, quantity, isSimulation, null);
		
		if(buyOrderStatus == null && simulationOnly == false)
			return "Unable to connect to TWS...";
		
		// Make the purchase with the Simulator
		isSimulation = true;
		if(simulationOnly == true)
		{
			buyOrderStatus = tradingAPI.placeOrder(BUY, parsedSymbol, quantity, isSimulation, null);  
			
			if(buyOrderStatus == null)
				return "Unable to connect to TWS...";
		}
		else
			tradingAPI.placeOrder(BUY, parsedSymbol, quantity, isSimulation, null);  
		
		// Sleep for 60 seconds, then sell
		try
		{			
			int timeTilSell = 300;  
			boolean cashOnlyOrderFlag = false;
			
			// Check the desired information every second for 60 seconds
			for(int numSeconds = 0; numSeconds < timeTilSell; numSeconds++)
			{
				// Special case - no need to do any checks with the order status
				if(simulationOnly == true)
				{
					Thread.sleep( timeTilSell * SECONDS );
					break;
				}
				
				//System.out.println("orderID = " + buyOrderStatus.orderId + ", buyOrderStatus = " + buyOrderStatus.status);
				
				// If we were unable to buy due to the stock being not marginable
				if( (buyOrderStatus.status.equalsIgnoreCase("Inactive") == true) && (cashOnlyOrderFlag == false) )
				{
					System.out.println("Order was rendered inactive, trying again with our total cash, " + totalCash);
					cashOnlyOrderFlag = true;
					
					// Make the trade using only cash (no leverage)
					isSimulation = false;
					int cash = super.getCash(totalCash, NOLEVERAGE);
					quantity = super.getQuantity(cash, Double.parseDouble(parsedPrice), TRADERPERCENTAGE, parsedQuantity);
					buyOrderStatus = tradingAPI.placeOrder(BUY, parsedSymbol, quantity, isSimulation, null);
					
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
			if( (simulationOnly == false) && ((buyOrderStatus == null) || (buyOrderStatus.status == null) 
					|| (buyOrderStatus.status.equalsIgnoreCase("Inactive") == true) 
					|| buyOrderStatus.status.equalsIgnoreCase("Cancelled") 
					|| buyOrderStatus.status.equalsIgnoreCase("PendingCancel")) )
			{
				return "We were unable to purchase - " + buyOrderStatus.status;
			}
			
			// Cancel the order if we have not purchased any stock
			if(buyOrderStatus.filled == 0)
			{
				if(simulationOnly == true)
					isSimulation = true;
				else
					isSimulation = false;
				
				tradingAPI.cancelOrder(buyOrderStatus, isSimulation);
				return "Order canceled - we did not buy within the time limit";
			}
			// If we have not completed the order, complete it
			else if(buyOrderStatus.status.equalsIgnoreCase("Filled") == false)
			{
				if(simulationOnly == true)
					isSimulation = true;
				else
					isSimulation = false;
				
				tradingAPI.cancelOrder(buyOrderStatus, isSimulation);
				
				// Wait until we have either completed the order or it is cancelled
				while( (buyOrderStatus.status.equalsIgnoreCase("Cancelled") == false) &&
						(buyOrderStatus.status.equalsIgnoreCase("Filled") == false) ){};
						
				// Get the quantity to sell
				quantity = buyOrderStatus.filled;
			}
		}
		catch ( InterruptedException e )
		{
			System.out.println( "awakened prematurely" );
		}
		
		// Sell the stocks
		isSimulation = false;
		if(simulationOnly == false)
			sellOrderStatus = tradingAPI.placeOrder(SELL, parsedSymbol, quantity, isSimulation, null);
		
		if( (simulationOnly == false) && (sellOrderStatus == null) )
			return "Unable to connect to TWS...";
		
		// Sell the stocks over the simulator
		isSimulation = true;
		if(simulationOnly == true)
		{
			sellOrderStatus = tradingAPI.placeOrder(SELL, parsedSymbol, quantity, isSimulation, null); 
			
			if(sellOrderStatus == null)
				return "Unable to connect to TWS...";
		}
		else
			tradingAPI.placeOrder(SELL, parsedSymbol, quantity, isSimulation, null);  
		
		// Save off useful trade information for the purchase
		tradeInfo = new HashMap<String,Object>();
		tradeInfo.put(Database.STOCKSYMBOL, parsedSymbol);
		tradeInfo.put(Database.NUMBEROFSHARES, quantity);
		tradeInfo.put(Database.AVERAGEBUYINGPRICE, buyOrderStatus.avgFillPrice);
		tradeInfo.put(Database.INITIALVOLUME, tradingAPI.getMarketData(tickerID, VOLUME));
		
		// Save off historical market data
		tradeInfo.put(Database.CASHTRADEDYESTERDAY, totalCashTradedYesterday);
		tradeInfo.put(Database.CASHTRADEDLASTWEEK, totalCashTradedLastWeek);
		tradeInfo.put(Database.CASHTRADEDLASTMONTH, totalCashTradedLastMonth);
		
		// Get the updated market data
		if(marketOpenFlag == true)
			marketData = "VOLUME";
		else
			marketData = "CLOSE_PRICE";	// Only for simulation
		
		tickerID = tradingAPI.subscribeToMarketData(parsedSymbol);
		while(tradingAPI.getMarketData(tickerID, marketData) == null){};
		
		// Save the volume now that the purchase is over
		tradeInfo.put(Database.VOLUMEAFTERPURCHASE, tradingAPI.getMarketData(tickerID, VOLUME));
		
		// Wait for the sell to be complete
		while( ((sellOrderStatus.remaining != 0) || (sellOrderStatus.status.equalsIgnoreCase("Filled") == false)) && (marketOpenFlag == true) ){};
		
		if(marketOpenFlag == false)
		{
			isSimulation = false;
			tradingAPI.cancelOrder(sellOrderStatus, isSimulation);
			return "Order canceled - we cannot sell when the market is closed";
		}
		
		// Get the updated market data
		tickerID = tradingAPI.subscribeToMarketData(parsedSymbol);
		while(tradingAPI.getMarketData(tickerID, marketData) == null){};
		
		// Save the last bits of information from the sell
		tradeInfo.put(Database.FINALVOLUME, tradingAPI.getMarketData(tickerID, VOLUME));
		tradeInfo.put(Database.AVERAGESELLINGPRICE, sellOrderStatus.avgFillPrice);
		
		// Save the debug flag
		tradeInfo.put(Database.DEBUGFLAG, simulationOnly);
		
		// If the market is closed, overright some of the values, only used in DebugTrader!
		if(marketOpenFlag == false)
		{
			tradeInfo.put(Database.INITIALVOLUME, 0);
			tradeInfo.put(Database.VOLUMEAFTERPURCHASE, 0);
			tradeInfo.put(Database.FINALVOLUME, 0);
		}

		return null;
		
		
	}
}