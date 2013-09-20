package com.IBTrading.tradeparsers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProfitlyTradeParser 
{
	// Passed parameters
	private String tradeString;
	
	// Parsed trade information
	public String symbol = null;
	public int quantity = 0;
	
	
	// PROFIT.LY

	public String date;
	public String action;
	public String totalQuant;
	public String symb;
	public String price;
	
	public ProfitlyTradeParser(String newTrade)
	{
		tradeString = newTrade;
	}
	
	public boolean parseTrade()
	{
		// If we are given an invalid value, return
		if(tradeString == null)
		{
			System.out.println("Null trade string");
			return false;
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
		
		// If everything went well, set up the trade
		if(areParamatersValid(date, action, totalQuant, symb, price) == true)
		{
			// Update the globals
			symbol = symb;
			quantity = Integer.parseInt(totalQuant);
		}
		else
			return false;
		
		return true;
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
