package tw.qing.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

public class PrimitiveTypeUtil
{
	private static Class classInteger;
	private static Class classLong;
	private static Class classDouble;
	private static Class classFloat;
	private static Class classBigDecimal;
	private static Class classBigInteger;
	static
	{
		try
		{
			classInteger = Class.forName("java.lang.Integer");
			classLong = Class.forName("java.lang.Long");
			classDouble = Class.forName("java.lang.Double");
			classFloat = Class.forName("java.lang.Float");
			classBigDecimal = Class.forName("java.math.BigDecimal");
			classBigInteger = Class.forName("java.math.BigInteger");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public static boolean isInt(Object obj)
	{
		return classInteger.isInstance(obj) || classLong.isInstance(obj) || classBigDecimal.isInstance(obj);
	}
	public static int getInt(Object obj)
	{
		if (classLong.isInstance(obj))
			return ((Long) obj).intValue();
		if (classBigDecimal.isInstance(obj))
			return ((BigDecimal) obj).intValue();
		if (classBigInteger.isInstance(obj))
			return ((BigInteger) obj).intValue();
		return ((Integer) obj).intValue();
	}
	public static Long getLong(Object obj)
	{
		if (classInteger.isInstance(obj))
			return new Long(((Integer) obj).intValue());
		if (classBigDecimal.isInstance(obj))
			return new Long(((BigDecimal) obj).longValue());
		if (classBigInteger.isInstance(obj))
			return new Long(((BigInteger) obj).longValue());
		return (Long) obj;
	}
	public static Integer getInteger(Object obj)
	{
		if (classLong.isInstance(obj))
			return new Integer(((Long) obj).intValue());
		if (classBigDecimal.isInstance(obj))
			return new Integer(((BigDecimal) obj).intValue());
		if (classBigInteger.isInstance(obj))
			return new Integer(((BigInteger) obj).intValue());
		return (Integer) obj;
	}
	public static Float getFloat(Object obj)
	{
		if (classDouble.isInstance(obj))
			return new Float(((Double) obj).floatValue());
		if (classBigDecimal.isInstance(obj))
			return new Float(((BigDecimal) obj).floatValue());
		return (Float) obj;
	}
	public static Double getDouble(Object obj)
	{
		if (classFloat.isInstance(obj))
			return new Double(((Float) obj).doubleValue());
		if (classBigDecimal.isInstance(obj))
			return new Double(((BigDecimal) obj).doubleValue());
		return (Double) obj;
	}
	public static Class getWrapClass(Class c)
	{
		if (c == Character.TYPE)
			return Character.class;
		else if (c == Integer.TYPE)
			return Integer.class;
		else if (c == Long.TYPE)
			return Long.class;
		else if (c == Float.TYPE)
			return Float.class;
		else if (c == Double.TYPE)
			return Double.class;
		else if (c == Boolean.TYPE)
			return Boolean.class;
		return c;
	}
	public static Object castValue(Class valueClass, Object value)
	{
		if (value==null)
			return null;
		if (value.getClass()==valueClass)
			return value;
		else
			return castValue(valueClass, value.toString());
	}
	public static Object castValue(Class valueClass, String value)
	{
		if (value==null)
			return null;
		if (valueClass == String.class)
			return value;
		if (valueClass == Character.TYPE)
		{
			if (value.trim().length()==0)
				return new Character(Character.MIN_VALUE);
			return new Character(value.trim().charAt(0));
		}
		//
		if (valueClass.isPrimitive())
		{
			if (value.trim().length()==0)
				value = new String("0");
			if (valueClass == Boolean.TYPE)
				return new Boolean(value);
			else if (valueClass == Byte.TYPE)
				return new Byte(value);
			else
			{
				if( value.indexOf(",")!=-1)
					value = value.replaceAll(",", "");
				if (valueClass == Double.TYPE)
					return new Double(value);
				else if (valueClass == Float.TYPE)
					return new Float(value);
				else if (valueClass == Integer.TYPE)
					return new Integer(value);
				else if (valueClass == Long.TYPE)
					return new Long(value);
				else if (valueClass == Short.TYPE)
					return new Short(value);
			}
		}
		//
		if (valueClass == Date.class)
			return DateUtil.stringToDate(value);
		if( valueClass == classDouble )
			return Double.valueOf(value);
		if( valueClass == classFloat )
			return Float.valueOf(value);
		if( valueClass == classLong )
			return Long.valueOf(value);
		if( valueClass == classInteger )
			return Integer.valueOf(value);
		return value;
	}
}