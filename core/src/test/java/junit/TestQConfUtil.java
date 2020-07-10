package junit;

import com.inveno.core.util.QConfUtil;

public class TestQConfUtil {
	
	public static void main(String[] args)
	{
		String strAbtestVersion = args[0];
		String strPath = "/zhizi/abtest/" + strAbtestVersion;
		System.out.println("abtest : " + strAbtestVersion + "\tconfiguration:" + QConfUtil.getBatchConf(strPath, true));
	}
}
