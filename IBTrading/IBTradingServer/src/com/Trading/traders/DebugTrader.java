package com.Trading.traders;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import com.Trading.ib.HistoricalData;
import com.Trading.ib.IBTradingAPI;
import com.Trading.ib.OrderStatus;
import com.Trading.servlets.Database;
import com.Trading.tradeparsers.JasonBondsTradeParser;

public class DebugTrader extends Trader
{
	// Passed parameters
	private String tradeString;
	private boolean websiteMonitorFlag;
	private boolean marketOpenFlag = false;
	
	// Parsed trade information
	JasonBondsTradeParser parser;
	
	// Public variables
	
	// PRIVATE VARIABLES
	private static ArrayList<String> lastTradeStrings = new ArrayList<String>();
	
	// Timers
	Timestamp startTime = null;
	
	// CONSTANTS
	private final int SECONDS = 1000;
	private final String BUY = "BUY";
	private final String SELL = "SELL";
	private final int TRADERPERCENTAGE = 100;
	private final int MAXLEVERAGE = 4;
	private final int NOLEVERAGE = 1;
	private final String VOLUME = "VOLUME";
	
	public DebugTrader(String newTrade, IBTradingAPI newTradingAPI, boolean newRealTimeSystem, boolean newMarketOpenFlag)
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
		
		websiteMonitorFlag = newRealTimeSystem;
		marketOpenFlag = newMarketOpenFlag;
		parser = new JasonBondsTradeParser(newTrade);
		hasValidTrade = parser.parseTrade();
		
		if(hasValidTrade == true)
		{
			lastTradeStrings.add(newTrade);
		}
	}
	
	private int getHistoricalData(String durationStr, String endDateTime, String barSizeSetting, String whatToShow)
	{
		int tickerID = tradingAPI.subscribeToHistoricalData(parser.symbol, endDateTime, durationStr, barSizeSetting, whatToShow);
		
		if(tickerID == -1)
			return tickerID;
		
		// Wait until we have received the market data
		while(tradingAPI.getHistoricalData(tickerID) == null){};
		
		return tickerID;
	}
	
	// Determines if this trade may be profitable
	public boolean isTradeable()
	{
		startTime = new Timestamp(System.currentTimeMillis());
		
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
		int tickerID = getHistoricalData(durationStr, endDateTime, barSizeSetting, whatToShow);
		ArrayList<HistoricalData> historicalDataArray = tradingAPI.getHistoricalData(tickerID);
		totalCashTradedYesterday = historicalDataArray.get(0).totalCashTradedInInterval;
		
		if(tickerID == -1)
			return false;
		
		durationStr = IBTradingAPI.ONEWEEKINTEGER + IBTradingAPI.DAYS;
		tickerID = getHistoricalData(durationStr, endDateTime, barSizeSetting, whatToShow);
		historicalDataArray = tradingAPI.getHistoricalData(tickerID);
		for(int i = 0; i < historicalDataArray.size(); i++)
		{
			totalCashTradedLastWeek += historicalDataArray.get(i).totalCashTradedInInterval;
		}
		
		if(tickerID == -1)
			return false;
		
		durationStr = IBTradingAPI.ONEMONTHINTEGER + IBTradingAPI.DAYS;
		tickerID = getHistoricalData(durationStr, endDateTime, barSizeSetting, whatToShow);
		historicalDataArray = tradingAPI.getHistoricalData(tickerID);
		for(int i = 0; i < historicalDataArray.size(); i++)
		{
			totalCashTradedLastMonth += historicalDataArray.get(i).totalCashTradedInInterval;
		}
		
		if(tickerID == -1)
			return false;
		
		// Get the information about the volume of the stock over the last 30 minutes
        // If it is a Sunday or Monday, use Friday's date
        //if(dayOfWeek != 1 && dayOfWeek != 2)
        cal = Calendar.getInstance();
		
		barSizeSetting = IBTradingAPI.ONEMINUTE;
		dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		endDateTime = dateFormat.format(cal.getTime()) + " CST";
		endDateTime = "20140331 2:10:00 CST"; // DEBUG TIME

		// Get the average/medium volume from the last 30 minutes
		ArrayList<Integer> volumeList = new ArrayList<Integer>();
		durationStr = IBTradingAPI.EIGHTEENHUNDREDSECONDINTEGER + IBTradingAPI.SECONDS;
		tickerID = getHistoricalData(durationStr, endDateTime, barSizeSetting, whatToShow);
		historicalDataArray = tradingAPI.getHistoricalData(tickerID);
		for(int i = 0; i < historicalDataArray.size(); i++)
		{
			volumeList.add(historicalDataArray.get(i).volume);
			averageVolumeInLast30Minutes += historicalDataArray.get(i).volume;
		}

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
			return "Cannot read market data from simulation account.";
		else
			isSimulation = false;
		
		// Give a little wiggle room of ~2% for the maxCash
		maxCash = super.getCash(totalCash, MAXLEVERAGE);
		
		// Wait until we have received the market data
		String marketData = null;
		if(marketOpenFlag == true)
			marketData = "LAST_PRICE";
		else
			marketData = "CLOSE_PRICE";	// Should only be using this in debug
		
		int tickerID = tradingAPI.subscribeToMarketData(parser.symbol);
		while(tradingAPI.getMarketData(tickerID, marketData) == null){};
		
		// We have finished waiting on all data - time to start the trade!
		long timeInterval = System.currentTimeMillis() - startTime.getTime();
		
		// If the price/share of the stock was not supplied, get this information from TWS
		if(parser.price.equalsIgnoreCase("0.00"))
		{
			// Get the market price
			parser.price = tradingAPI.getMarketData(tickerID, marketData) + "";
		}
		
		try
		{
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
				|| (parser.flagsHashMap.get(parser.BONDBLOWUPS) == true) 
				|| (super.isBlackListed(parser.symbol) == true) )
			simulationOnly = true;
		else
			simulationOnly = true;
		
		/*
		buyOrderStatus = tradingAPI.placeOrder(BUY, parser.symbol, quantity, isSimulation);
		
		if(buyOrderStatus == null)
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
				//return "Order canceled - we did not buy within the time limit";
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
		
		if(sellOrderStatus == null)
			return "Unable to connect to TWS...";
		*/
		
		// Sell the stocks over the simulator
		isSimulation = true;
		sellOrderStatus = tradingAPI.placeOrder(SELL, parser.symbol, simulationQuantity, isSimulation, null);
		
		// Save off useful trade information for the purchase
		tradeInfo = new HashMap<String,Object>();
		tradeInfo.put(Database.STOCKSYMBOL, parser.symbol);
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
		
		tickerID = tradingAPI.subscribeToMarketData(parser.symbol);
		while(tradingAPI.getMarketData(tickerID, marketData) == null){};
		
		// Save the volume now that the purchase is over
		tradeInfo.put(Database.VOLUMEAFTERPURCHASE, tradingAPI.getMarketData(tickerID, VOLUME));
		
		// Wait for the sell to be complete
		while( (sellOrderStatus.remaining != 0) && (marketOpenFlag == true) ){};
		
		// Cancel the order if we have not purchased any stock
		if(sellOrderStatus.filled == 0)
		{
			isSimulation = true;
			tradingAPI.cancelOrder(sellOrderStatus, isSimulation);
			//return "Order canceled - we cannot sell when the market is closed";
		}
		
		// Get the updated market data
		tickerID = tradingAPI.subscribeToMarketData(parser.symbol);
		while(tradingAPI.getMarketData(tickerID, marketData) == null){};
		
		// Save the last bits of information from the sell
		tradeInfo.put(Database.FINALVOLUME, tradingAPI.getMarketData(tickerID, VOLUME));
		tradeInfo.put(Database.AVERAGESELLINGPRICE, sellOrderStatus.avgFillPrice);
		
		// Save the debug flag
		tradeInfo.put(Database.DEBUGFLAG, true);
		
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