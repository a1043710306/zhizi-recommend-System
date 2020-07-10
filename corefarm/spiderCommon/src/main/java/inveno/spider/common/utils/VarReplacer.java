package inveno.spider.common.utils;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.HashMap;
import java.util.Vector;

public class VarReplacer
{
    public static String replace(String s, HashMap props)
    {
        int p = s.indexOf("${");
        if( p < 0 )
            return s;
        int q = s.indexOf("}", p+1);
        if( q < 0 )
            return s;
        String var = s.substring(p+2, q);
        String value = (String) props.get(var);
        if( value == null )
            return s.substring(0, q+1)+replace(s.substring(q+1), props);
        return s.substring(0, p)+value+replace(s.substring(q+1), props);
    }
    private String replace(String s, Properties props)
    {
        int p = s.indexOf("${");
        if( p < 0 )
            return s;
        int q = s.indexOf("}", p+1);
        if( q < 0 )
            return s;
        String var = s.substring(p+2, q);
        String value = props.getProperty(var);
        if( value == null )
            return s.substring(0, q+1)+replace(s.substring(q+1), props);
        return s.substring(0, p)+value+replace(s.substring(q+1), props);
    }
    public VarReplacer(String inputFile, String outputFile, Properties props) throws FileNotFoundException, IOException
    {
        FileReader fr = new FileReader(inputFile);
        BufferedReader br = new BufferedReader(fr);
        //
        Vector v = new Vector();
        while(true)
        {
            String s = br.readLine();
            if( s == null )
                break;
            v.add(s);
        }
        //
        fr.close();
        br.close();
        //
        FileWriter fw = new FileWriter(outputFile);
        PrintWriter pw = new PrintWriter(fw);
        //
        for(int i=0;i<v.size();i++)
        {
            String s = (String) v.get(i);
            pw.println(replace(s, props));
        }
        //
        pw.close();
        fw.close();
    }
    public static void main(String argv[])
    {
        if( argv.length < 3 )
        {
            System.out.println("args: <inputFile> <outputFile> <propertiesFile>");
            System.exit(0);
        }
        try
        {
            FileInputStream fis = new FileInputStream(argv[2]);
            Properties props = new Properties();
            props.load(fis);
            new VarReplacer(argv[0], argv[1], props);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
