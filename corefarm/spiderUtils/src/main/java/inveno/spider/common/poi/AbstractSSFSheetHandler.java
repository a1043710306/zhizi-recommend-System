package inveno.spider.common.poi;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Genix.Li on 2016/05/08.
 */
public abstract class AbstractSSFSheetHandler
{
	private static final Logger log = Logger.getLogger(AbstractSSFSheetHandler.class);

	protected boolean fKeepOrder = false;
	protected String[] COLUMN_NAME = null;

	protected AbstractSSFSheetHandler(String[] _COLUMN_NAME, boolean _fKeepOrder)
	{
		COLUMN_NAME = _COLUMN_NAME;
		fKeepOrder  = _fKeepOrder;
	}
	public static int getCellIntValue(Cell cell)
	{
		int value = 0;
		if (cell != null)
		{
			int type = cell.getCellType();
			if (Cell.CELL_TYPE_NUMERIC == type)
			{
				int num = (new Double(cell.getNumericCellValue())).intValue();
				value = num;
			}
		}
		return value;
	}
	public static String getCellDoubleValue(Cell cell)
	{
		String value = null;
		if (cell != null)
		{
			int type = cell.getCellType();
			if (Cell.CELL_TYPE_STRING == type)
			{
				value = cell.getStringCellValue();
			}
			else if (Cell.CELL_TYPE_NUMERIC == type)
			{
				double num = (new Double(cell.getNumericCellValue()));
				value = String.valueOf(num);
			}
		}
		return value;
	}
	public static String getCellValue(Cell cell)
	{
		String value = null;
		if (cell != null)
		{
			int type = cell.getCellType();
			if (Cell.CELL_TYPE_STRING == type)
			{
				value = cell.getStringCellValue();
			}
			else if (Cell.CELL_TYPE_NUMERIC == type)
			{
				value = String.valueOf(getCellIntValue(cell));
			}
		}
		if (value != null)
		{
			value = value.trim();
		}
		return value;
	}
	public ArrayList<HashMap<String, Object>> parseData(String file)
	{
		ArrayList<HashMap<String, Object>> result = null;
		java.io.FileInputStream fis = null;
		try
		{
			fis = new java.io.FileInputStream(file);
			result = parseData(fis);
		}
		catch (Exception e)
		{
			log.error(e);
		}
		finally
		{
			if (fis != null)
			{
				try
				{
					fis.close();
				}
				catch (Exception e)
				{
				}
			}
		}
		return result;
	}
	protected ArrayList<HashMap<String, Object>> parseData(InputStream is)
	{
		ArrayList<HashMap<String, Object>> alData = new ArrayList<HashMap<String, Object>>();
		try
		{
			XSSFWorkbook workbook = new XSSFWorkbook(is);
			XSSFSheet sheet = workbook.getSheetAt(0);
			int rowCount = 0;
			Iterator<Row> itRow = sheet.rowIterator();
			while (itRow.hasNext())
			{
				Row row = (Row)itRow.next();
				//skip first row
				if (rowCount > 0)
				{
					HashMap<String, Object> hmData = new HashMap<String, Object>();
					int total = 0;
					for (int i = 0; i < COLUMN_NAME.length; i++)
					{
						Cell cell = row.getCell(i);
						String name = COLUMN_NAME[i];
						if (name == null)
							continue;
						String value = null;
						if (cell != null)
						{
							int type = cell.getCellType();
							if (Cell.CELL_TYPE_STRING == type)
							{
								value = cell.getStringCellValue();
							}
							else if (Cell.CELL_TYPE_NUMERIC == type)
							{
								value = String.valueOf(cell.getNumericCellValue());
							}
						}
						hmData.put(name, value);
					}
					if (fKeepOrder)
						hmData.put("sequence", rowCount);
					alData.add(hmData);
				}
				rowCount++;
			}
		}
		catch (Exception e)
		{
			log.error("", e);
		}
		return alData;
	}
	protected static String getStackTraceJsonString(Exception e)
	{
		JsonObject obj = new JsonObject();
		obj.add("code", new JsonPrimitive(500));
		obj.add("message", new JsonPrimitive(getStackTraceString(e)));
		return (new GsonBuilder().create()).toJson(obj);
	}
	protected static String getStackTraceString(Exception e)
	{
		java.io.Writer writer = new java.io.StringWriter();
		java.io.PrintWriter printWriter = new java.io.PrintWriter(writer);
		e.printStackTrace(printWriter);
		return writer.toString();
	}
}