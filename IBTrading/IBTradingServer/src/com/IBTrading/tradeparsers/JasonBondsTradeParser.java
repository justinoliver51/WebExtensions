package com.IBTrading.tradeparsers;

public class JasonBondsTradeParser {
	// Passed parameters
	private String tradeString;
	
	// Parsed trade information
	public String symbol = "";
	public int quantity = 0;
	public String action = "";
	public String price = "";
	
	public JasonBondsTradeParser(String newTrade)
	{
		tradeString = newTrade;
	}
	
	public boolean parseTrade()
	{
		String parsedSymbol = null, parsedQuantity = null, parsedAction = null, parsedPrice = null;
		
		// If we are given an invalid value, return
		if(tradeString == null)
		{
			System.out.println("Null trade string");
			return false;
		}
		
		try
		{
			// Get the useful information
			String[] tokens = tradeString.split("[ ]");
			
			// If this is not a known format, return false
			if((tokens.length != 2) && (tokens.length != 5))
				return false;
			
			// Bought CRRS
			if(tokens.length == 2)
			{
				parsedAction = tokens[0];
				parsedSymbol = tokens[1];
			}
			
			// Bought 10,000 DMD at $5.19
			// Added 10,000 CRRS at $3.50
			if(tokens.length == 5)
			{
				parsedAction   = tokens[0];
				parsedQuantity = tokens[1];
				parsedSymbol   = tokens[2];
				
				if(tokens[4].contains("$") == true)
					parsedPrice = tokens[4].substring(1);
				else
					parsedPrice = tokens[4];
			}
			
			// If everything went well, set up the trade
			if(areParamatersValid(parsedAction, parsedQuantity, parsedSymbol, parsedPrice) == true)
			{
				// Update the globals
				action = parsedAction;
				quantity = Integer.parseInt(parsedQuantity);
				symbol = parsedSymbol;
				price = parsedPrice;
			}
			else
				return false;
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public boolean areParamatersValid(String action, String totalQuant, String symb, String price)
	{	
		try
		{
			// If the action is not 'Bought'
			if( (action.equalsIgnoreCase("Bought") == false) 
					&& (action.equalsIgnoreCase("Added") == false) 
					&& (action.equalsIgnoreCase("Taking") == false))
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
			if(Double.parseDouble(price) >= 100)
			{
				System.out.println("Invalid Price, " + price);
				return false;
			}
		} catch(NumberFormatException e)
		{
			e.printStackTrace();
			return false;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
			
		// The parameters are valid
		return true;
	}
}
