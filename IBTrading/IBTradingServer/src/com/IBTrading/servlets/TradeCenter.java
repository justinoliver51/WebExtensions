package com.IBTrading.servlets;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import com.IBTrading.Traders.AwesomePennyStocksTrader;
import com.IBTrading.Traders.DebugTrader;
import com.IBTrading.Traders.JasonBondsTrader;
import com.IBTrading.Traders.SupermanTrader;
import com.IBTrading.Traders.SykesTrader;
import com.IBTrading.Traders.Trader;

public class TradeCenter {
	// Passed parameters
	private String traderID;
	
	// TWS API
	private static IBTradingAPI tradingAPI;
	
	// Valid trade - true if parsed correctly, false otherwise
	public boolean isValidTrade = false;
	
	// The current trader
	private Trader trader = null;  
	
	// True if market is open
	private boolean marketOpenFlag = false;
	
	// CONSTANTS
	private final int SECONDS = 1000;
	private final String BUY = "BUY";
	private final String SELL = "SELL";
	
	// Traders
	private String SUPERMAN = "SuperAlerts";  // Superman
	private String SUPERMANCHAT = "super_trades";  // Superman
	private String SYKES = "TimAlerts";  // Tim Sykes
	private String SYKESCHAT = "timothysykes";  // Tim Sykes
	private String AWESOMEPENNYSTOCKS = "AwesomePennyStocks"; // AwesomePennyStocks.com
	private String JASONBONDS = "Jason"; // Jason Bond
	private String JASONBONDSEMAIL = "Jason Bond"; // Jason Bond
	private String DEBUGEMAIL = "Justin Oliver"; // My debug trader

	public TradeCenter(IBTradingAPI newTradingAPI)
	{
		tradingAPI = newTradingAPI;
	}
	
	public String newTrade(String newTraderID, String newTrade, String realTimeSystem)
	{
		// If either of the passed values are null, exit
		if( (newTraderID == null) || (newTrade == null) )
		{
			System.out.println("Null arguments...");
			return "Null arguments...";
		}
		
		// If this is an invalid time of day, exit
		long MILLIS_AT_8_30_AM = (long) (8.5 * 60 * 60 * 1000);
		long MILLIS_AT_3_00_PM = (long) (15 * 60 * 60 * 1000);
		long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;
		long MILLIS_TIMEZONE_DIFF = 6 * 60 * 60 * 1000;
		
		//Date now = Calendar.getInstance(TimeZone.getTimeZone("US/Central")).getTime();
		long timePortion = System.currentTimeMillis() % MILLIS_PER_DAY;
		timePortion = timePortion < MILLIS_TIMEZONE_DIFF ? (MILLIS_PER_DAY - (MILLIS_TIMEZONE_DIFF - timePortion)) : (timePortion - MILLIS_TIMEZONE_DIFF); 
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		
		if( (timePortion < MILLIS_AT_8_30_AM) || (timePortion > MILLIS_AT_3_00_PM) || (dayOfWeek == 7) || (dayOfWeek == 1) )
		{
			System.out.println("Market is closed!");
			marketOpenFlag = false;
		}
		else
			marketOpenFlag = true;
		
		traderID = newTraderID;
		boolean websiteMonitorFlag = realTimeSystem.equalsIgnoreCase("websiteMonitor") ? 
										true : false;
		
		// Parse the new trade string
		if(traderID.equalsIgnoreCase(SUPERMAN) || traderID.equalsIgnoreCase(SUPERMANCHAT))
		{
			SupermanTrader currentTrader = new SupermanTrader(newTrade, tradingAPI, websiteMonitorFlag);
			trader = (Trader) currentTrader;
		}
		else if(traderID.equalsIgnoreCase(SYKES) || traderID.equalsIgnoreCase(SYKESCHAT))
		{
			SykesTrader currentTrader = new SykesTrader(newTrade, tradingAPI, websiteMonitorFlag);
			trader = (Trader) currentTrader;
		}
		else if(traderID.equalsIgnoreCase(AWESOMEPENNYSTOCKS))
		{
			AwesomePennyStocksTrader currentTrader = new AwesomePennyStocksTrader(newTrade, tradingAPI, websiteMonitorFlag);
			trader = (Trader) currentTrader;
		}
		else if(traderID.equalsIgnoreCase(JASONBONDS) || traderID.equalsIgnoreCase(JASONBONDSEMAIL))
		{
			JasonBondsTrader currentTrader = new JasonBondsTrader(newTrade, tradingAPI, websiteMonitorFlag);
			trader = (Trader) currentTrader;
		}
		else if(traderID.equalsIgnoreCase(DEBUGEMAIL))
		{
			DebugTrader currentTrader = new DebugTrader(newTrade, tradingAPI, websiteMonitorFlag, marketOpenFlag);
			trader = (Trader) currentTrader;
		}
		else
		{
			System.out.println(traderID + " is not a valid trader.");
			return traderID + " is not a valid trader.";
		}
		
		// If the market is closed, return an error
		if( (traderID.equalsIgnoreCase(DEBUGEMAIL) == false) && (marketOpenFlag == false) )
			return "Market is closed!";  //FIXME: We may not want this commented out!
		
		return null;
	}
	
	public String trade()
	{
		// Determine if passed parameters are correct
		if(trader.hasValidTrade == false)
		{
			System.out.println("Trader has an invalid trade.");
			return "Trader has an invalid trade.";
		}
		else
		{
			System.out.println("Trader has a valid trade.");
		}
		
		// Determine if this trade could make a profit
		if(trader.isTradeable() == false)
		{
			System.out.println("Trade could not be validated...");
			return "Trade could not be validated...";
		}
		else
		{
			System.out.println("The stock may be tradeable.");
		}
		
		return trader.trade();
	}
	
	public HashMap<String,Object> getTradeInfo()
	{	
		return trader.tradeInfo;
	}
}