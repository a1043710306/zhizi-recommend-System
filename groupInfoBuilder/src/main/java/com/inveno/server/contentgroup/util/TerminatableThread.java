package com.inveno.server.contentgroup.util;

public class TerminatableThread extends Thread
{
	private boolean fTerminated = false;

	protected boolean isTerminated()
	{
		return fTerminated;
	}
	public void terminated()
	{
		fTerminated = true;
	}
}
