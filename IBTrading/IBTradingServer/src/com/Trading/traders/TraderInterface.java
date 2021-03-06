package com.Trading.traders;

interface TraderInterface 
{	   
	// Parses the passed trade into the necessary information for the trade
	public boolean parseTrade(String newTrade);

	// Initiates the trade with TWS
	public boolean trade();
}