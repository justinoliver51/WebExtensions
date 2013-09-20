var url = 'http://localhost:8080/IBTradingServer/RemoteProcedureCallsServlet?';

chrome.runtime.onMessage.addListener(
	function(request,sender,senderResponse)
	{
		// If we've somehow disconnected, refresh the page
		if(request.message==="Refresh")
		{
			location.reload(); console.log("Page Updated…");
		}
		// Otherwise, it is a trade
		else
		{
			var encodedURL;
			var params = {
				traderID: request.msg,
				newTrade: request.text
			};
			
			encodedURL = url + $.param(params);
			console.log(encodedURL); 

			// Make the call to the server
			$.getJSON( encodedURL ,function(data){
				console.log(data);
			});
		}
	}
);
