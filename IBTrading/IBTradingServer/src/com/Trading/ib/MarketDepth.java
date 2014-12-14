package com.Trading.ib;

public class MarketDepthRow 
{
	public int tickerId;	// The ticker Id that was specified previously in the call to reqMktDepth()
	public int position;	// Specifies the row Id of this market depth entry.
	public int operation;	// Identifies how this order should be applied to the market depth. Valid values are:
							// 0 = insert (insert this new order into the row identified by 'position')·
							// 1 = update (update the existing order in the row identified by 'position')·
							// 2 = delete (delete the existing order at the row identified by 'position').
	public int side;		// Identifies the side of the book that this order belongs to. Valid values are:
	public double price;	// The order price.
	public int size;		// The order size.
	
	public MarketDepthRow(int newTickerId, int newPosition, int newOperation, 
			int newSide, double newPrice, int newSize)
	{
		tickerId 	= newTickerId;
		position 	= newPosition;
		operation 	= newOperation;
		side		= newSide;
		price		= newPrice;
		size		= newSize;
	}
}