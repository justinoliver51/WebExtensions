package com.Trading.servlets;

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

public class GoogleVoice implements Runnable
{
	// Paths to setup Jython
	public String modulesDir = "/Library/Python/2.7/site-packages/";
	public String rootPath = "/Users/justinoliver/jython2.5.3/Lib";
	
	// Variables to log in to Google Voice and who to text
	private final String password = "utredhead51";
	private final String personalEmail = "justin.tradealerts@gmail.com";
	private final String JUSTINS_CELL = "12144762900";
	private final String TYLERS_CELL  = "16302446933";
	private final String DANS_CELL    = "13013999397";
			
	// The message - what we're sending	
	public String message;	
	
	public GoogleVoice(String theMessage)
	{
		message = theMessage;
	}
	
	public void run() 
	{
        sendTextMessage();
    }
	
    public void sendTextMessage()
    {
    	try
    	{
	        // Create an instance of the PythonInterpreter
	        PythonInterpreter interp = new PythonInterpreter();
	        
	        PySystemState sys = Py.getSystemState();
	        sys.path.append(new PyString(rootPath));
	        sys.path.append(new PyString(modulesDir));
	
	        // The exec() method executes strings of code
	        interp.exec("from googlevoice import Voice");
	        interp.exec("import time");
	        
	        interp.set("personalEmail", new PyString(personalEmail));
	        interp.set("password", new PyString(password));
	        interp.set("JUSTINS_CELL", new PyString(JUSTINS_CELL));
	        interp.set("TYLERS_CELL", new PyString(TYLERS_CELL));
	        interp.set("DANS_CELL", new PyString(DANS_CELL));
	        interp.set("message", new PyString(message));
	        
	        interp.set("voice", new PyObject());
	        interp.exec("voice = Voice()");
	        
	        interp.exec("voice.login(personalEmail, password)");
	        interp.exec("voice.send_sms(JUSTINS_CELL, message)");
	        interp.exec("time.sleep(1)");
	        interp.exec("voice.send_sms(TYLERS_CELL, message)");
	        interp.exec("time.sleep(1)");
	        //interp.exec("voice.send_sms(DANS_CELL, message)");
	        interp.exec("voice.logout()");
    	}
    	catch(PyException e)
    	{
    		System.out.println("An exception occurred sending a text message.");
    		e.printStackTrace();
    	}
    }

}