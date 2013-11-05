package com.IBTrading.servlets;

public class OrderStatus 
{
	public int orderId;
	public String status;
	public int filled;
	public int remaining;
	public double avgFillPrice;
	public int permId;
	public int parentId;
	public double lastFillPrice;
	public int clientId;
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
