var s = document.createElement('script');
s.src = chrome.extension.getURL("script.js");
s.onload = function() {
    this.parentNode.removeChild(this);
};
(document.head||document.documentElement).appendChild(s);

/*
var regex = /abelski/;

if(regex.test(document.body.innerText))
{
	alert("Got here!");
	//chrome.runtime.sendMessage("request message", function(response_str) {alert(response_str)});
}*/