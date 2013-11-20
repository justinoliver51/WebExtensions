package com.IBTrading.servlets;

public class OrderStatus 
{
	/*
		orderId	- 	The order ID that was specified previously in the 
					call to placeOrder()
	*/
	public int orderId;
	
	/*
	status	String	
	The order status. Possible values include:

	PendingSubmit - indicates that you have transmitted the order, 
					but have not yet received confirmation that it 
					has been accepted by the order destination.
	PendingCancel - indicates that you have sent a request to cancel
	 				the order but have not yet received cancel 
	 				confirmation from the order destination. At this 
	 				point, your order is not confirmed canceled. You 
	 				may still receive an execution while your 
	 				cancellation request is pending. PendingSubmit 
	 				and PendingCancel order statuses are not sent by 
	 				the system and should be explicitly set by the 
	 				API developer when an order is canceled.
	PreSubmitted - 	indicates that a simulated order type has been 
					accepted by the system and that this order has
					yet to be elected. The order is held in the 
					system until the election criteria are met. At 
					that time the order is transmitted to the order 
					destination as specified.
	Submitted - 	indicates that your order has been accepted at 
					the order destination and is working.
	Cancelled - 	indicates that the balance of your order has 
					been confirmed canceled by the system. This 
					could occur unexpectedly when the destination has 
					rejected your order.
	Filled - 		indicates that he order has been completely filled.
	Inactive - 		indicates that the order has been accepted by the 
					system (simulated orders) or an exchange (native 
					orders) but that currently the order is inactive 
					due to system, exchange or other issues.
	*/
	public String status;
	
	/*
		filled - 	Specifies the number of shares that have been 
					executed.
	*/
	public int filled;
	
	/*
		remaining -	Specifies the number of shares still outstanding.
	*/
	public int remaining;
	
	/*
	avgFillPrice - 	The average price of the shares that have been 
					executed. This parameter is valid only if the 
					filled parameter value is greater than zero. 
					Otherwise, the price parameter will be zero.
	*/
	public double avgFillPrice;
	
	/*
	permId - 		The id used to identify orders. Remains the same 
					over sessions.
	*/
	public int permId;
	
	/*
	parentId - 		The order ID of the parent order, used for 
					bracket and auto trailing stop orders.
	 */
	public int parentId;
	
	/*
	lastFillPrice -	The last price of the shares that have been 
					executed. Valid only if the filled parameter 
					value is greater than zero. Otherwise, the price 
					parameter will be zero.
	*/
	public double lastFillPrice;
	
	/*
	clientId -		The ID of the client who placed the order. Note 
					that application orders have a fixed clientId 
					and orderId of 0 that distinguishes them from 
					API orders.
	*/
	public int clientId;
	
	/*
	whyHeld	- 		Identifies an order held when TWS is trying to 
					locate shares for a short sell.
	*/
	public String whyHeld;
	
	// Default Constructor
	public OrderStatus()
	{
		orderId = 0;
		status = "";
		filled = 0;
		remaining = 0;
		avgFillPrice = 0.0;
		permId = 0;
		parentId = 0;
		lastFillPrice = 0.0;
		clientId = 0;
		whyHeld = "";
		
		return;
	}
	
	public OrderStatus(int newOrderId, String newStatus, int newFilled, int newRemaining,
			 double newAvgFillPrice, int newPermId, int newParentId,
			 double newLastFillPrice, int newClientId, String newWhyHeld)
	{
		orderId = newOrderId;
		status = newStatus;
		filled = newFilled;
		remaining = newRemaining;
		avgFillPrice = newAvgFillPrice;
		permId = newPermId;
		parentId = newParentId;
		lastFillPrice = newLastFillPrice;
		clientId = newClientId;
		whyHeld = newWhyHeld;
		
		return;
	}
	
	public void updateOrder(int newOrderId, String newStatus, int newFilled, int newRemaining,
			 double newAvgFillPrice, int newPermId, int newParentId,
			 double newLastFillPrice, int newClientId, String newWhyHeld)
	{
		orderId = newOrderId;
		status = newStatus;
		filled = newFilled;
		remaining = newRemaining;
		avgFillPrice = newAvgFillPrice;
		permId = newPermId;
		parentId = newParentId;
		lastFillPrice = newLastFillPrice;
		clientId = newClientId;
		whyHeld = newWhyHeld;
		
		return;
	}
}
