package inveno.spider.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;

public class Util
{
    public static byte[] serialize(Object obj) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public static <T> T deserialize(byte[] data) throws IOException,
            ClassNotFoundException
    {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return (T)is.readObject();
    }
    
    public static String isoToUTF8(String str)
    {
        if(null==str || str.trim().length()==0)
        {
            return "";
        }
        
        try
        {
            return new String(str.getBytes("ISO-8859-1"),"utf-8");
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return str;
    }

}
