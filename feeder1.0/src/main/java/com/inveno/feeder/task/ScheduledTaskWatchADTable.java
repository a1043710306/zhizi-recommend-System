package com.inveno.feeder.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.inveno.feeder.ClientJDBCTemplateAD;
import com.inveno.feeder.Feeder;


public class ScheduledTaskWatchADTable
{
	private static ClientJDBCTemplateAD clientJDBCTemplateAD;
	
	public void process() {
		
		//System.out.println("[ScheduledTaskWatchADTable] Processing...");
		
		ApplicationContext context = new FileSystemXmlApplicationContext("beans-config.xml");
		clientJDBCTemplateAD = (ClientJDBCTemplateAD)context.getBean("clientJDBCTemplateAD");
		
		List<Map<String, String>> listADTableEntry = clientJDBCTemplateAD.listADTableEntry();
		if (listADTableEntry.size() > 0)
		{
			for (Map<String, String> infoMap : listADTableEntry)
			{				
				try {
					
					File fHDFSFilename = new File(Feeder.getStrLocalFilePath()+"toAD/"+Feeder.getCurrentHDFSFilename());
					if (!fHDFSFilename.exists())
					{
						fHDFSFilename.createNewFile();
					}
					
					FileWriter fileWritter = new FileWriter(fHDFSFilename.getAbsolutePath(), true);
	    	        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
	    	        bufferWritter.write(new JSONObject(infoMap).toString());
	    	        bufferWritter.write("\n");
	    	        bufferWritter.close();
					
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		
		((ConfigurableApplicationContext)context).close();
	}
	
}