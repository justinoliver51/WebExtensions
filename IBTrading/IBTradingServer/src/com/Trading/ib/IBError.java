package com.Trading.ib;

public class IBError
{
	public int id;
	public int errorCode;
	public String errorMsg;
	
	public IBError(int newId, int newErrorCode, String newErrorMsg)
	{
		id = newId;
		errorCode = newErrorCode;
		errorMsg = newErrorMsg;
	}
	
	public IBError clone()
	{
		IBError error = new IBError(id, errorCode, errorMsg);
        return error;
    }
}
