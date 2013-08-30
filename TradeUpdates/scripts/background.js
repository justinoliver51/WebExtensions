var url = 'http://localhost:8080/IBTradingServer/RemoteProcedureCallsServlet?traderID=robertmaxwell&newTrade=';

chrome.runtime.onMessage.addListener(
	function(request,sender,senderResponse)
	{
		// If we've received a trade update, let the server know!
		if(request.msg==="TradeUpdate")
		{
				var newURL = url.concat(request.text);
				console.log(newURL); 

				// Make the call to the server
				$.getJSON( newURL ,function(data){
   		                    console.log(data);
   		                });
		}
	}
);
