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
	public String trade()
	{
		System.out.println("Should not have arrived here: Trader.trade()");
		return "Should not have arrived here: Trader.trade()";
	}
	
	//
	public int setQuantity(int maxCash, double price, int traderPercentage, int maxQuantity)
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
