package tw.qing.lwdba;

public class SQLString
{
	private String str;
	//
	public String getString()
	{
		return str;
	}
	public SQLString(String _str)
	{
		str = _str;
	}
	public String toString()
	{
		return getString();
	}
}
