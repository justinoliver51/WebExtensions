package com.IBTrading.servlets;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TradeParsers {
	// Passed parameters
	private String traderID;
	private String tradeString;
	
	// Parsed trade information
	public String symbol = null;
	public int quantity = 0;
	
	// Valid trade - true if parsed correctly, false otherwise
	public boolean isValidTrade = false;
	
	// PROFIT.LY
	// List of trader identifiers and their strings
	private String trader = null;  // Superman
	private static String lastTraderString;
	private static int tradePercentage = 0;
	
	private String SUPERMAN = "SuperAlerts";  // Superman
	private static String supermanLastTraderString;
	private static int supermanTradePercentage = 25;
	
	private String SYKES = "TimAlerts";  // Tim Sykes
	private static String sykesLastTraderString;
	private static int sykesTradePercentage = 25;
	
	
	public TradeParsers(String newTraderID, String newTrade)
	{
		// If either of the passed values are null, exit
		if( (newTraderID == null) || (newTrade == null) )
		{
			System.out.println("Null arguments...");
			return;
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
		if(traderID.equalsIgnoreCase(SUPERMAN))
		{
			trader = SUPERMAN;  // Superman
			lastTraderString = supermanLastTraderString;
			tradePercentage = supermanTradePercentage;
			
			ProfitLYParser parser = new ProfitLYParser();
			String temp = parser.parseTrade(newTrade);
			if(temp != null)
			{
				supermanLastTraderString = temp;
			}
		}
		else if(traderID.equalsIgnoreCase(SYKES))
		{
			trader = SYKES;  // Superman
			lastTraderString = sykesLastTraderString;
			tradePercentage = sykesTradePercentage;
			
			ProfitLYParser parser = new ProfitLYParser();
			String temp = parser.parseTrade(newTrade);
			if(temp != null)
			{
				sykesLastTraderString = temp;
			}
		}
		else
		{
			System.out.println(traderID + " is not a valid trader.");
			return;
		}
	}
	
	public class ProfitLYParser
	{
		// Example trade string from RobertMaxwell:
		// 08/28 Bought 15000 shares DGLY at 9.6667 - SWING DONT CHASE  downside 8.25 target 10  if works.....- DGLY only 2.1m shares o/s and same product as TASR camera and patent link
		
		public String date;
		public String action;
		public String totalQuant;
		public String symb;
		public String price;
		
		public String parseTrade(String newTrade)
		{
			// If we are given an invalid value, return
			if(tradeString == null)
			{
				System.out.println("Null trade string");
				isValidTrade = false;
				return null;
			}
			
			// If we have already parsed this string, return
			if(newTrade.equalsIgnoreCase(lastTraderString))
			{
				System.out.println("Duplicate trade, " + newTrade);
				isValidTrade = false;
				return null;
			}
			
			// Get the useful information
			String[] tokens = tradeString.split("[ ]");
			int tokensIndex = 0;
			
			// Get the date
			date = tokens[tokensIndex++];
			
			// Ignore the time if it exists
			if( (tokens[tokensIndex].contains("AM")) || (tokens[tokensIndex].contains("PM")))
			{
				tokensIndex++;
			}
			else if( (tokens[tokensIndex + 1].contains("AM")) || (tokens[tokensIndex + 1].contains("PM")) )
			{
				tokensIndex = tokensIndex + 2;
			}
			
			 // Skipping '-'
			if(tokens[tokensIndex].equalsIgnoreCase("-"))
			{
				tokensIndex++;
			}
			
			// Get the action
			action = tokens[tokensIndex++];
			
			// Get the quantity
			totalQuant = tokens[tokensIndex];
			tokensIndex = tokensIndex + 2;
			
			// Get the symbol
			symb = tokens[tokensIndex];
			tokensIndex = tokensIndex + 2;
			
			// Get the price
			price = tokens[tokensIndex];
			
			isValidTrade = areParamatersValid(date, action, totalQuant, symb, price);
			
			// If everything went well, set up the trade
			if(isValidTrade == true)
			{
				// Update the globals
				symbol = symb;
				quantity = (Integer.parseInt(totalQuant) * tradePercentage) / 100;
				
				// Save this for next time
				supermanLastTraderString = newTrade;
			}
			else
				return null;
			
			return newTrade;
		}
		
		public boolean areParamatersValid(String date, String action, String totalQuant, String symb, String price)
		{
			// Get the current date
			DateFormat dateFormatBought = new SimpleDateFormat("MM/dd");
			Date todaysDate = new Date();
			
			// If the date is not the current date
			if(date.equalsIgnoreCase(dateFormatBought.format(todaysDate)) == false)
			{
				System.out.println("Invalid date, " + date);
				return false;
			}
			
			// If the action is not 'Bought'
			if( (action.equalsIgnoreCase("Bought") == false) && (action.equalsIgnoreCase("Added") == false) )
			{
				System.out.println("Invalid action, " + action + " does not match 'Bought' or 'Added'...");
				return false;
			}
			
			// If the totalQuant is not a valid number > 0
			if(Integer.parseInt(totalQuant) <= 0)
			{
				System.out.println("Invalid quantity, " + quantity);
				return false;
			}
			
			// If the symbol is not valid
			if(symb.length() > 10)
			{
				System.out.println("Invalid symbol, " + symb);
				return false;
			}
			
			// If the price is unreasonable
			if(false)
			{
				return false;
			}
				
			// The parameters are valid
			return true;
		}
	}
}