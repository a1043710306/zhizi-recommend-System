package tw.qing.util;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.StringTokenizer;
import java.text.NumberFormat;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TextUtil
{
	public static String encode(String s, String encode)
	{
		try
		{
			if (encode!=null)
				return new String(s.getBytes(), encode);
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		return s;
	}
	public static String getString(String[] list)
	{
		return getString(list, new String(","));
	}
	//added by genix@2003/11/29
	public static String getString(String[] list, boolean fCheckNull)
	{
		return getString(list, new String(","), fCheckNull);
	}
	//modified by genix@2003/11/29
	public static String getString(String[] list, String sep)
	{
		return getString(list, sep, false);
	}
	//modified by genix@2003/11/29
	public static String getString(String[] list, String sep, boolean fCheckNull)
	{
		StringBuffer sb = new StringBuffer();
		if (list!=null)
		{
			if (fCheckNull)
			{
				boolean fFirst = true;
				for (int i=0; i<list.length; i++)
				{
					if (list[i]!=null)
					{
						if (!fFirst)
							sb.append(sep);
						//
						sb.append(list[i]);
						//
						if (fFirst)
							fFirst = false;
					}
				}
			}
			else
			{
				for (int i=0; i<list.length; i++)
				{
					if (i!=0)
						sb.append(sep);
					sb.append(list[i]);
				}
			}
		}
		return sb.toString();
	}
	public static String[] getStringList(String str, char sep)
	{
		StringTokenizer st = new StringTokenizer(str, "" + sep);
		int n = st.countTokens();
		if( n <= 0 )
			return null;
		String s[] = new String[n];
		for(int i=0;i<n;i++)
			s[i] = st.nextToken().trim();
		return s;
	}
	public static String[] getStringList(String str)
	{
		return getStringList(str, ',');
	}
	private static void showStringArray(String[] array)
	{
		if (array==null)
			System.out.println("it's null");
		for (int i=0; i<array.length; i++)
			System.out.println(array[i]);
	}
	public static String[] transform(String[] orig)
	{
		if (orig!=null && orig.length>0)
		{
			//showStringArray(orig);
			String[] firstList = getStringList(orig[0]);
			StringBuffer[] sb = new StringBuffer[firstList.length];
			for (int i=0; i<firstList.length; i++)
			{
				sb[i] = new StringBuffer();
				sb[i].append(firstList[i]);
			}
			//
			for (int i=1; i<orig.length; i++)
			{
				String[] theList = getStringList(orig[i]);
				for (int j=0; j<theList.length; j++)
				{
					sb[j].append(',');
					sb[j].append(theList[j]);
				}
			}
			String[] result = new String[sb.length];
			for (int i=0; i<result.length; i++)
				result[i] = sb[i].toString();
			//showStringArray(result);
			return result;
		}
		return null;
	}
	public static int[] getIntList(String str)
	{
		String[] list = getStringList(str);
		if (list!=null && list.length>0)
		{
			int[] intArray = new int[list.length];
			try
			{
				for (int i=0; i<list.length; i++)
					intArray[i] = Integer.parseInt(list[i]);
				return intArray;
			}
			catch (NumberFormatException nfe)
			{
				//ignore
			}
		}
		return null;
	}
	public static String getAdvancePercentage(long present, long past)
	{
		if (past==0)
			return new String("-");
		return new String((present-past)*100/past+"%");
	}
	public static String toPercentage(float numerator, float denominator)
	{
		return toPercentage(numerator, denominator, 5);
	}
	public static String toPercentage(float numerator, float denominator, int len)
	{
		if (denominator==0)
			return new String("-");
		String s = String.valueOf((numerator/denominator)*100);
		if (s.length()>len)
			return s.substring(0, len)+"%";
		else
			return s+"%";
	}
	public static String format(double value, int maxFDigits)
	{
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(maxFDigits);
		//nf.setMinimumFractionDigits(minFDigits);
		return nf.format(value);
	}
	public static String getFileSize(float fileSize)
	{
		float k = 1024;
		float m = 1048576;
		if (fileSize<k)
			return new String((int)fileSize+" bytes");
		else if (fileSize<m)
			return new String(format(fileSize/k, 1)+" Kb");
		else
			return new String(format(fileSize/m, 1)+" Mb");
	}
	public static String getFileSize(Integer fileSize)
	{
		if (fileSize!=null)
			return getFileSize(fileSize.floatValue());
		else
			return new String("0");
	}
	// for JDK1.3
	public static String remove(String source, String s)
	{
		return replaceAll(source, s, "");
	}
	public static String replaceAll(String source, String s, String t)
	{
		int cursor = 0;
		while(true)
		{
			int p = source.indexOf(s, cursor);
			if( p < 0 )
				return source;
			StringBuffer sb = new StringBuffer(source);
			//
			sb = sb.replace(p, p+s.length(), t);
			source = sb.toString();
			cursor = p+t.length();
		}
	}
	public static String textToHTML(String text)
	{
		if( text == null )
			return null;
		return replaceAll(text, "\n", "<br>");
	}
	public static String[] split(String str, String sep)
	{
		StringTokenizer st = new StringTokenizer(str, sep);
		int n = st.countTokens();
		if( n <= 0 )
			return null;
		String s[] = new String[n];
		for(int i=0;i<n;i++)
			s[i] = st.nextToken().trim();
		return s;
	}
	public static String[] split(String str)
	{
		return split(str, ",");
	}
	public static String toHexString(byte[] b)
	{
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<b.length; i++)
		{
			int hex = (b[i]<0)?(b[i]+256):b[i];
			if (hex<16)
				sb.append("0");
			sb.append(Integer.toHexString(hex));
		}
		return sb.toString();
	}
	public static String md5Encode(String s)
	{
		MessageDigest md;
		try
		{
			md = MessageDigest.getInstance("MD5");
			byte buf[] = s.getBytes();
			//
			md.update(buf, 0, buf.length);
			return toHexString(md.digest());
		}
		catch (NoSuchAlgorithmException e)
		{
			return null;
		}
	}
}