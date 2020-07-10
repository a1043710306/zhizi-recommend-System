package inveno.spider.parser.idclass.other;

import inveno.spider.parser.exception.ExtractException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import com.jayway.jsonpath.JsonPath;

public class JsonPathHelper
{
    public JsonPathHelper()
    {

    }

    public String getString(String expression, String content)
            throws ExtractException
    {
        List<String> nodes = JsonPath.read(content, expression);

        try
        {
            if (null == nodes || nodes.size() == 0)
            {
                return "";
            }

            return StringUtils.trim(nodes.get(0));
        } catch (Exception e)
        {
            throw new ExtractException("Fail to find xpath", e);
        }
    }

    public List<String> getListString(String expression, String content)
            throws ExtractException
    {
        try
        {
            List<String> nodes = JsonPath.read(content, expression);
            return nodes;
        } catch (Exception e)
        {
            throw new ExtractException("Fail to find xpath", e);
        }
    }

    public List<String> getListString(String expression, String content,
            String charset) throws ExtractException
    {
        try
        {
            List<String> nodes = JsonPath.read(content, expression);
            return nodes;
        } catch (Exception e)
        {
            throw new ExtractException("Fail to find xpath", e);
        }
    }

}
