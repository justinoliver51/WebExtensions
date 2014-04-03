package com.Trading.ib;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

public class HistoricalDataCollector
{
	IBTradingAPI tradingAPI;
	
	public HistoricalDataCollector(IBTradingAPI newTradingAPI)
	{	
		tradingAPI = newTradingAPI;
	}
	
	public ArrayList<HistoricalData> collectHistoricalData(String symbol, String timestampString)
	{
		Long timestampLong = Long.parseLong(timestampString);
		Timestamp timestamp = new Timestamp(timestampLong);
		
		String barSizeSetting = IBTradingAPI.ONESECOND;
		String whatToShow = IBTradingAPI.TRADES;
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		String endDateTime = dateFormat.format(timestamp) ; //+ " GMT";
		String durationStr = IBTradingAPI.EIGHTEENHUNDREDSECONDINTEGER + IBTradingAPI.SECONDS;
		
		// Get the historical data
		int tickerID = getHistoricalData(symbol, durationStr, endDateTime, barSizeSetting, whatToShow);
		ArrayList<HistoricalData> historicalDataArray = tradingAPI.getHistoricalData(tickerID);

		
		return historicalDataArray;
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