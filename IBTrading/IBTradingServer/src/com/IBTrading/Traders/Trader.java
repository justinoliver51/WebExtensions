package com.IBTrading.Traders;

import com.IBTrading.servlets.IBTradingAPI;

public class Trader 
{
	public static IBTradingAPI tradingAPI;	// TWS API
	
	// Valid trade - true if parsed correctly, false otherwise
	public boolean hasValidTrade = false;
	
	public Trader(IBTradingAPI newTradingAPI)
	{
		tradingAPI = newTradingAPI;
	}
	
	// Parses the passed trade into the necessary information for the trade
	public boolean parseTrade(String newTrade)
	{
		System.out.println("Should not have arrived here: Trader.parseTrade()");
		
		return true;
	}

	// Initiates the trade with TWS
	public boolean trade()
	{
		System.out.println("Should not have arrived here: Trader.trade()");
		
		return true;
	}
}