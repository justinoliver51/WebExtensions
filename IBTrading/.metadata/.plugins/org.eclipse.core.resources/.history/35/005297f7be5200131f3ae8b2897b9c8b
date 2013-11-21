package com.IBTrading.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	private TradeCenter tradeCenter;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
	
	public RemoteProcedureCallsServlet() 
    {
        super();

        // Initialize the trading API connection
        tradingAPI = new IBTradingAPI();
        tradingAPI.connect();
        
        // Initializes the Trade Center
        tradeCenter = new TradeCenter(tradingAPI);
        
        // Subscribe to updates from my account
        boolean isSimulation = true;
        tradingAPI.initializeAvailableFunds(isSimulation);
        //tradingAPI.getMarketData("DGLY", false);
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		HttpSession session = request.getSession(true);
		PrintWriter out = response.getWriter();
		response.setHeader("Access-Control-Allow-Origin", "*");
        System.out.println("-----------------------\ndoGet() called!  :)");
        out.println("-----------------------\ndoGet() called");
        
        synchronized (session)
        {
        	
        }//end session lock
        
        String startUp = request.getParameter("startUp");
		String traderID = request.getParameter("traderID");
		String newTrade = request.getParameter("newTrade");
		String realTimeSystem = request.getParameter("realTimeSystem");
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss aa");
		Date date = new Date();
		
		System.out.println("traderID: " + traderID + "\ntrade: " + newTrade + "\nrealTimeSystem: " + realTimeSystem + "\non date: " + dateFormat.format(date));
		out.println("traderID: " + traderID + "\ntrade: " + newTrade + "\nrealTimeSystem: " + realTimeSystem + "\non date: " + dateFormat.format(date));
		
		if(startUp != null)
		{
			System.out.println("System started up!");
			out.println("System started up!");
			return;
		}
		
		if( (traderID == null) || (newTrade == null) || (realTimeSystem == null) || ((realTimeSystem.equalsIgnoreCase("email") == false) && (realTimeSystem.equalsIgnoreCase("websiteMonitor") == false)) )
		{
			System.out.println("Invalid parameters...");
			out.println("Invalid parameters: \ntraderID = " + traderID 
					+ "\nnewTrade = " + newTrade);
			return;
		}
		
		// Send the trade to the TradeCenter to be evaluated
		String tradeError = tradeCenter.newTrade(traderID, newTrade, realTimeSystem);
		if(tradeError == null)
		{
			// The new trade has found a trader.  
			// If the trade is successful, send the all clear!
			tradeError = tradeCenter.trade();
			if(tradeError == null)
			{
				System.out.println("Valid trade initiated!");
				out.println("Valid trade!");
			}
			else
			{
				System.out.println("Invalid trade!\nError: " + tradeError);
				out.println("Invalid Trade!\nError: " + tradeError);
			}
		}
		else
		{
			System.out.println("Invalid parameters, " + traderID + ", " + newTrade);
			out.println("Error: " + tradeError);
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