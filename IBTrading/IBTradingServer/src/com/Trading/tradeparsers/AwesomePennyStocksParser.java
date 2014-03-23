package com.Trading.tradeparsers;


public class AwesomePennyStocksParser 
{
	// Passed parameters
	private String tradeString;
	
	// Parsed trade information
	public String symbol = null;
	public int quantity = 1000;
	
	public AwesomePennyStocksParser(String newTrade)
	{
		tradeString = newTrade;
	}
	
	public boolean parseTrade()
	{
		String symb;
		
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
			
			// If the string is valid, grab the ticker symbol
			// Our Brand New Pick is <TICKER_SYMBOL> 
			if(tokens[0].equalsIgnoreCase("Our") &&
					tokens[1].equalsIgnoreCase("Brand") &&
					tokens[2].equalsIgnoreCase("New") &&
					tokens[3].equalsIgnoreCase("Pick") &&
					tokens[4].equalsIgnoreCase("is"))
			{
				symb = tokens[5];
			}
			else
				return false;
			
			// If everything went well, set up the trade
			if(areParamatersValid(symb) == true)
			{
				// Update the globals
				symbol = symb;
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
	
	public boolean areParamatersValid(String symb)
	{
		try
		{
			// If the symbol is not valid
			if( (symb != null) && (symb.length() > 10) )
			{
				System.out.println("Invalid symbol, " + symb);
				return false;
			}
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
}