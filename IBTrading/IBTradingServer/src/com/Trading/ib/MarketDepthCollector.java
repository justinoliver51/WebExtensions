package com.Trading.ib;

import java.util.ArrayList;
import java.util.HashMap;

public class MarketDepthCollector 
{
	IBTradingAPI tradingAPI;
	
	public MarketDepthCollector(IBTradingAPI newTradingAPI)
	{	
		tradingAPI = newTradingAPI;
	}
	
	public HashMap<String, Object> collectMarketDepthForTenMin(String symbol)
	{
		HashMap<String, Object> marketDepthMap = new HashMap<String, Object>();
		int numRows = 10;
		int tickerID = tradingAPI.subscribeToMarketDepth(symbol, numRows);

		ArrayList<MarketDepthRow> marketDepthArray = tradingAPI.getMarketDepth(tickerID);
		
		
		
		return marketDepthMap;
	}
}