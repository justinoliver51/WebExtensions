package com.IBTrading.servlets;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.IBTrading.Traders.*;

public class TradeCenter {
	// Passed parameters
	private String traderID;
	private String tradeString;
	
	// TWS API
	private static IBTradingAPI tradingAPI;
	
	// Valid trade - true if parsed correctly, false otherwise
	public boolean isValidTrade = false;
	
	// The current trader
	private Trader trader = null;  
	
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

	public TradeCenter(IBTradingAPI newTradingAPI)
	{
		tradingAPI = newTradingAPI;
	}
	
	public boolean newTrade(String newTraderID, String newTrade)
	{
		// If either of the passed values are null, exit
		if( (newTraderID == null) || (newTrade == null) )
		{
			System.out.println("Null arguments...");
			return false;
		}
		
		// If this is an invalid time of day, exit
		long MILLIS_AT_8_30_AM = (long) (8.5 * 60 * 60 * 1000);
		long MILLIS_AT_3_00_PM = (long) (15 * 60 * 60 * 1000);
		long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;
		long MILLIS_TIMEZONE_DIFF = 5 * 60 * 60 * 1000;
		
		Date now = Calendar.getInstance(TimeZone.getTimeZone("US/Central")).getTime();
		long timePortion = System.currentTimeMillis() % MILLIS_PER_DAY;
		timePortion = timePortion < MILLIS_TIMEZONE_DIFF ? (MILLIS_PER_DAY - (MILLIS_TIMEZONE_DIFF - timePortion)) : (timePortion - MILLIS_TIMEZONE_DIFF); 
		
		if( (timePortion < MILLIS_AT_8_30_AM) || (timePortion > MILLIS_AT_3_00_PM) )
		{
			System.out.println("Market is closed!");
			//return;  FIXME: We may not want this commented out!
		}
		
		traderID = newTraderID;
		tradeString = newTrade;
		
		// Parse the new trade string
		if(traderID.equalsIgnoreCase(SUPERMAN) || traderID.equalsIgnoreCase(SUPERMANCHAT))
		{
			SupermanTrader currentTrader = new SupermanTrader(newTrade, tradingAPI);
			trader = (Trader) currentTrader;
		}
		else if(traderID.equalsIgnoreCase(SYKES) || traderID.equalsIgnoreCase(SYKESCHAT))
		{
			SykesTrader currentTrader = new SykesTrader(newTrade, tradingAPI);
			trader = (Trader) currentTrader;
		}
		else if(traderID.equalsIgnoreCase(AWESOMEPENNYSTOCKS))
		{
			AwesomePennyStocksTrader currentTrader = new AwesomePennyStocksTrader(newTrade, tradingAPI);
			trader = (Trader) currentTrader;
		}
		else
		{
			System.out.println(traderID + " is not a valid trader.");
			return false;
		}
		
		return true;
	}
	
	public boolean trade()
	{
		//SupermanTrader currentTrader = (SupermanTrader) trader;
		
		if(trader.hasValidTrade == false)
		{
			System.out.println("Trader has an invalid trade.");
			return false;
		}
		
		trader.trade();
		
		return true;
	}
}