package com.IBTrading.Traders;

import com.IBTrading.servlets.IBTradingAPI;

public class Trader 
{
	public static IBTradingAPI tradingAPI;	// TWS API
	private final int MAXLEVERAGE = 4;
	private final int NOLEVERAGE = 1;
	private final int MAXCASH = 24000;
	
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
	public String trade()
	{
		System.out.println("Should not have arrived here: Trader.trade()");
		return "Should not have arrived here: Trader.trade()";
	}
	
	//
	public int getCash(int cash, int leverage)
	{
		// Ensure cash is realistic
		if(cash < 0)
			return 0;
		else if(cash > MAXCASH)
			cash = MAXCASH;
		
		// Ensure leverage is realistic
		if(leverage > MAXLEVERAGE)
			leverage = MAXLEVERAGE;
		else if(leverage < 0)
			leverage = NOLEVERAGE;
		
		// 2% wiggle room
		cash = (cash * leverage) - ((cash * leverage) / 50);
		if(cash > MAXCASH)
			cash = MAXCASH;
		
		return cash;
	}
	
	//
	public int getQuantity(int maxCash, double price, int traderPercentage, int maxQuantity)
	{
		// If we do not have a maximum amount of cash to spend, buy the maximum number of shares
		if(maxCash == 0)
			return ((maxQuantity * traderPercentage) / 100);
		
		// If we do not know the price of the stock, buy the maximum number of shares
		else if(price == 0.0)
			return ((maxQuantity * traderPercentage) / 100);
		
		// If we do not have a maximum number of shares to purchase, spend the maximum amount of cash
		else if (maxQuantity == 0)
			return ((int) (maxCash/price));
		
		// Ensure the TraderPercentage is within the bounds of 0 - 100
		else if (traderPercentage < 0)
			traderPercentage = 0;
		else if (traderPercentage > 100)
			traderPercentage = 100;
		
		// Return the minimum of either the maximum number of shares or the maximum amount of 
		// shares we can purchase with cash
		return ((int) (maxCash/price) <= ((maxQuantity * traderPercentage) / 100) ) ? 
					((int) (maxCash/price)) : ((maxQuantity * traderPercentage) / 100);
	}
}
