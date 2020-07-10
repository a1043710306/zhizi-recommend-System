package inveno.spider.reports.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;
import java.util.HashMap;

public class TemplateParser
{
	File templateFile;
	ArrayList templateList;
	HashMap templateMap;

	public TemplateParser(File _templateFile)
	{
		templateFile = _templateFile;
		templateList = new ArrayList();
		templateMap  = new HashMap();
	}

	private String[] getTemplateContent(int start)
	{
		return getTemplateContent(start, templateList.size());
	}
	private String[] getTemplateContent(int start, int end)
	{
		ArrayList content = new ArrayList();
		for (int i=start; i<end; i++)
			content.add((String)templateList.get(i));
		return (String[])content.toArray(new String[0]);
	}

	public HashMap getMap()
	{
		return templateMap;
	}

	public void doParse()
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(templateFile));
			String prefix = new String();
			//
			while (true)
			{
				String line = br.readLine();
				String token = null;
				int sindex = 0;
				int eindex = 0;
				//
				if (line==null)
					break;
				sindex = line.indexOf("<%", eindex);
				if (sindex < 0)
					prefix += line + "\n";
				else
				{
					while (sindex!=-1)
					{
						if (eindex+2<line.length() && eindex!=0)
							prefix += line.substring(eindex+2, sindex);
						else
							prefix += line.substring(eindex, sindex);
						eindex = line.indexOf("%>", sindex);
						token  = line.substring(sindex+2, eindex);
						templateList.add(prefix);
						templateList.add(token);
						//next iteration
						sindex = line.indexOf("<%", eindex);
						if (sindex==-1)
							prefix = line.substring(eindex+2, line.length());
						else
							prefix = "";
					}
					prefix += "\n";
				}
			}
			templateList.add(prefix);
			//
			assembleParse();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void assembleParse()
	{
		String[] prefix  = null;
		String[] collect = null;
		int sindex=0;
		int eindex=0;
		//
		sindex = templateList.indexOf("HeaderTemplate");
		prefix = getTemplateContent(0, sindex);
		eindex = templateList.indexOf("/HeaderTemplate");
		collect = getTemplateContent(sindex+1, eindex);
		templateMap.put("HeaderPrefix", prefix);
		templateMap.put("HeaderTemplate", collect);
		//
		sindex = templateList.indexOf("DirectoryTemplate");
		prefix = getTemplateContent(eindex+1, sindex);
		eindex = templateList.indexOf("/DirectoryTemplate");
		collect = getTemplateContent(sindex+1, eindex);
		templateMap.put("DirectoryPrefix", prefix);
		templateMap.put("DirectoryTemplate", collect);
		//
		sindex = templateList.indexOf("ContentTemplate");
		prefix = getTemplateContent(eindex+1, sindex);
		eindex = templateList.indexOf("/ContentTemplate");
		collect = getTemplateContent(sindex+1, eindex);
		templateMap.put("ContentPrefix", prefix);
		templateMap.put("ContentTemplate", collect);
		//postfix
		collect = getTemplateContent(eindex+1);
		templateMap.put("ContentPostfix", collect);
	}

	public static void main(String[] args)
	{
		TemplateParser tp = new TemplateParser(new File(args[0]));
		tp.doParse();
		HashMap tm = tp.getMap();
		Set set = tm.keySet();
		Iterator it = set.iterator();
		while (it.hasNext())
		{
			String key = (String)it.next();
			System.out.println("-------" + key + "-------");
			String[] value = (String[])tm.get(key);
			for (int i=0; i<value.length; i++)
				System.out.println(value[i]);
		}
	}
}