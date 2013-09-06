var url = 'http://localhost:8080/IBTradingServer/RemoteProcedureCallsServlet?traderID=';

chrome.runtime.onMessage.addListener(
	function(request,sender,senderResponse)
	{
		// If we've received a trade update, let the server know!
		if(request.msg==="superman")
		{
			var newURL = url.concat('superman&newTrade=');
			newURL = newURL.concat(request.text);
			console.log(newURL); 

			// Make the call to the server
			$.getJSON( newURL ,function(data){
				console.log(data);
			});
		}
		else if(request.msg==="sykes")
		{
			var newURL = url.concat('sykes&newTrade=');
			newURL = newURL.concat(request.text);
			console.log(newURL); 

			// Make the call to the server
			$.getJSON( newURL ,function(data){
				console.log(data);
			});
		}
		// If we've somehow disconnected, refresh the page
		if(request.message==="Refresh")
		{
			location.reload(); console.log("Page Updated…");
		}
	}
);
