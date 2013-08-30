package com.IBTrading.servlets;

public class TradeParsers {
	// Passed parameters
	private String traderID;
	private String tradeString;
	
	// Parsed trade information
	public String symbol = null;
	public int quantity = 0;
	
	// Valid trade - true if parsed correctly, false otherwise
	public boolean isValidTrade = false;
	
	// List of trader identifiers and their strings
	private String ROBERTMAXWELL = "robertmaxwell";
	private static String robertMaxwellLastTraderString;
	private static int robertMaxwellTradePercentage = 20;
	
	
	public TradeParsers(String newTraderID, String newTrade)
	{
		// If either of the passed values are null, exit
		if( (newTraderID == null) || (newTrade == null) )
			return;
		
		traderID = newTraderID;
		tradeString = newTrade;
		
		// Parse the new trade string
		if(traderID.equalsIgnoreCase(ROBERTMAXWELL))
		{
			RobertMaxwellParser parser = new RobertMaxwellParser();
			parser.parseTrade(newTrade);
		}
	}
	
	public class RobertMaxwellParser
	{
		// Example trade string from RobertMaxwell:
		// 08/28 Bought 15000 shares DGLY at 9.6667 - SWING DONT CHASE  downside 8.25 target 10  if works.....- DGLY only 2.1m shares o/s and same product as TASR camera and patent link
		
		public String date;
		public String action;
		public String totalQuant;
		public String symb;
		public String price;
		
		public void parseTrade(String newTrade)
		{
			// If we are given an invalid value, return
			if(tradeString == null)
			{
				isValidTrade = false;
				return;
			}
			
			// If we have already parsed this string, return
			if(newTrade.equalsIgnoreCase(robertMaxwellLastTraderString))
			{
				isValidTrade = false;
				return;
			}
			
			// Get the useful information
			String[] tokens = tradeString.split("[ ]");
			date = tokens[0];
			action = tokens[1];
			totalQuant = tokens[2];
			symb = tokens[4];
			price = tokens[6];
			
			isValidTrade = areParamatersValid(date, action, totalQuant, symb, price);
			
			// Set the globals
			if(isValidTrade == true)
			{
				symbol = symb;
				quantity = (Integer.parseInt(totalQuant) * robertMaxwellTradePercentage) / 100;
			}
		}
		
		public boolean areParamatersValid(String date, String action, String totalQuant, String symb, String price)
		{
			// If the date is not the current date
			if(false)
				return false;
			
			// If the action is not 'Bought'
			if(false)
				return false;
			
			// If the totalQuant is not a valid number > 0
			if(false)
				return false;
			
			// If the symbol is not valid
			if(false)
				return false;
			
			// If the price is unreasonable
			if(false)
				return false;
				
			// The parameters are valid
			return true;
		}
	}
}