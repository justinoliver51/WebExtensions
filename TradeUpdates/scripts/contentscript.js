/*var className = '.comments';
var seconds = 0;
var numItems = $(className).length;
var numMilliseconds = 1000;

function go () {
    // Get the trade array and update the time
		var tradeArray = $(className);
    //console.log(seconds);
		
		// temp
		//console.log(tradeArray[0]);
		//console.log(numItems);
		var i;
		var j;
		
		if(tradeArray.length > numItems)
		{
			var tradeUpdate;
			
			// Update the number of trades
			numItems = tradeArray.length;
			console.log(numItems);
			
			// Access the latest trade text
			splitHTML = tradeArray[0].innerHTML.split("<a")
			tradeUpdate = splitHTML[0];
			console.log(tradeUpdate);
			
			numMilliseconds = 300000;
		}
    seconds++;
    setTimeout(go,numMilliseconds);
}
go();
*/
// https://developer.chrome.com/extensions/npapi.html
// http://stackoverflow.com/questions/17805140/chrome-extension-use-the-same-socket-io-connection-under-background-page-and-con

var socket = io.connect('http://localhost:1337');
socket.on("hello",function(data){
    console.log(data.text);
    chrome.runtime.sendMessage({msg:"socket",text:data.text},function(response){});
});

