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
			boolean validParameters = false;
			
			// If this is not a known format, return false
			if(tokens.length <= 1)
				return false;
			
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
				
				validParameters = areParamatersValid(parsedAction, parsedQuantity, parsedSymbol, parsedPrice);
				
				// If we successfully parsed the parameters, return true
				if(validParameters == true)
					return true;
			}
			
			// Bought DMD
			if(tokens.length > 1)
			{
				parsedAction = tokens[0];
				parsedSymbol = tokens[1];
				
				parsedQuantity = "0";
				parsedPrice = "0.00";
				
				validParameters = areParamatersValid(parsedAction, parsedQuantity, parsedSymbol, parsedPrice);

				// If we successfully parsed the parameters, return true
				if(validParameters == true)
					return true;
			}
			
			// If everything went well, set up the trade
			System.out.println("Invalid Parameters");
			return false;
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean areParamatersValid(String parsedAction, String parsedQuantity, String parsedSymbol, String parsedPrice)
	{	
		try
		{
			// If the action is not 'Bought'
			if( (parsedAction.equalsIgnoreCase("Bought") == false) 
					&& (parsedAction.equalsIgnoreCase("Added") == false) 
					&& (parsedAction.equalsIgnoreCase("Taking") == false))
			{
				System.out.println("Invalid action, " + parsedAction + " does not match 'Bought' or 'Added'...");
				return false;
			}
			
			// If the totalQuant is not a valid number > 0
			if(Integer.parseInt(parsedQuantity) < 0)
			{
				System.out.println("Invalid quantity, " + parsedQuantity);
				return false;
			}
			
			// If the symbol is not valid
			if(parsedSymbol.length() > 10)
			{
				System.out.println("Invalid symbol, " + parsedSymbol);
				return false;
			}
			
			// If the price is unreasonable
			if(Double.parseDouble(parsedPrice) >= 100)
			{
				System.out.println("Invalid Price, " + parsedPrice);
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
		
		// Update the globals
		action = parsedAction;
		quantity = Integer.parseInt(parsedQuantity);
		symbol = parsedSymbol;
		price = parsedPrice;
			
		// The parameters are valid
		return true;
	}
}
