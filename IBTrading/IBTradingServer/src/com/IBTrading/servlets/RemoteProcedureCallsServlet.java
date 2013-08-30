package com.IBTrading.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class RemoteProcedureCallsServlet
 */
@WebServlet("/RemoteProcedureCallsServlet")
//http://localhost:8080/IBTradingServer/RemoteProcedureCallsServlet
public class RemoteProcedureCallsServlet extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	private IBTradingAPI tradingAPI;
	
	
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RemoteProcedureCallsServlet() 
    {
        super();
        
        // Initialize the trading API connection
        tradingAPI = new IBTradingAPI();
        tradingAPI.connect();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		// TODO Auto-generated method stub
		
		HttpSession session = request.getSession(true);
		PrintWriter out = response.getWriter();
        System.out.println("doGet() called!  :)");
        
        synchronized (session)
        {
        	
        }//end session lock
        
        String startUp = request.getParameter("startUp");
		String traderID = request.getParameter("traderID");
		String newTrade = request.getParameter("newTrade");
		
		if(startUp != null)
		{
			System.out.println("System started up!");
			out.println("System started up!");
			return;
		}
		
		// Parse the trade
		TradeParsers tradeParser = new TradeParsers(traderID, newTrade);
		System.out.println("traderID: " + traderID + ", symbol: " + tradeParser.symbol + ", quantity: " + tradeParser.quantity);
		
		if(tradeParser.isValidTrade == true)
		{
			System.out.println("Valid trade!");
			out.println("Valid trade!");
			tradingAPI.placeOrder(tradeParser.symbol, tradeParser.quantity);
		}
		else
		{
			System.out.println("Invalid trade!");
			out.println("Invalid trade!");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doPost(request, response);
	}
	
	

}