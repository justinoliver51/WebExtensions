
package com.Trading.ib;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;

public class HistoricalDataCollector
{
	IBTradingAPI tradingAPI;
	
	public HistoricalDataCollector(IBTradingAPI newTradingAPI)
	{	
		tradingAPI = newTradingAPI;
	}
	
	public HashMap<String, Object> collectHistoricalData(String symbol, String timestampString)
	{
		Long timestampLong = Long.parseLong(timestampString);
		Timestamp timestamp = new Timestamp(timestampLong);
		
		String barSizeSetting = IBTradingAPI.ONEMINUTE;
		String whatToShow = IBTradingAPI.TRADES;
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("CST"));
		String endDateTime = dateFormat.format(timestamp) + " CST";
		String durationStr = IBTradingAPI.EIGHTEENHUNDREDSECONDINTEGER + IBTradingAPI.SECONDS;
		
		// Get the historical data
		int tickerID = getHistoricalData(symbol, durationStr, endDateTime, barSizeSetting, whatToShow);
		ArrayList<HistoricalData> historicalDataArray = tradingAPI.getHistoricalData(tickerID);
		
		// Covert the historical data into Google Finance format and Yahoo Finance format
		String googleFormattedData = "";
		
		int index = 0;
		for(HistoricalData data : historicalDataArray)
		{
			if(index > 0)
				googleFormattedData += "\n";
			
			if(data.WAP == -1)
				break;
			
			googleFormattedData += ((timestampLong / 1000) - IBTradingAPI.EIGHTEENHUNDREDSECONDINTEGER + (index * 60)) + ",";
			googleFormattedData += data.open + ",";
			googleFormattedData += data.high + ",";
			googleFormattedData += data.low + ",";
			googleFormattedData += data.close + ",";
			googleFormattedData += data.volume;
			
			index++;
		}
		
		HashMap<String, Object> historicalDataMap = new HashMap<String, Object>();
		historicalDataMap.put("HistoricalData", historicalDataArray);
		historicalDataMap.put("GoogleFormattedData", googleFormattedData);
		
		return historicalDataMap;
	}
	
	private int getHistoricalData(String symbol, String durationStr, String endDateTime, String barSizeSetting, String whatToShow)
	{
		int tickerID = tradingAPI.subscribeToHistoricalData(symbol, endDateTime, durationStr, barSizeSetting, whatToShow);
		
		if(tickerID == -1)
			return tickerID;
		
		// Wait until we have received the market data
		while(tradingAPI.getHistoricalData(tickerID) == null){};
		
		return tickerID;
	}
}