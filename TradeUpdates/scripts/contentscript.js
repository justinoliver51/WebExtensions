var className = '.comments';
var seconds = 0;
var numItems = $(className).length;
var numMilliseconds = 1000;

chrome.runtime.onMessage.addListener(
	function(request,sender,senderResponse)
	{
		// If we've received a trade update, let the server know!
		if(request.msg==="Update Received")
		{
			console.log(request.text);
		}
	}
);

function go () {
		// Get the trade array and update the time
		var tradeArray = $(className);
		console.log(seconds);
	
/*	
		if(numItems == 0)
		{
			numItems = tradeArray.length;
		}
*/
	
		if(tradeArray.length > numItems)
		{
			var tradeUpdate;
			
			// Update the number of trades
			numItems = tradeArray.length;
			console.log("New trade alert!");
			
			// Access the latest trade text
			splitHTML = tradeArray[0].innerHTML.split("<a")
			tradeUpdate = splitHTML[0];
			console.log(tradeUpdate);
			
			// Send message to the background with the data
			chrome.runtime.sendMessage({msg:"TradeUpdate",text:tradeUpdate},function(response){});
		}
    seconds++;
    setTimeout(go,numMilliseconds);
}
go();
