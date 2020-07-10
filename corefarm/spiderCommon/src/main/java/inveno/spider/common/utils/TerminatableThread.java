package inveno.spider.common.utils;

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
