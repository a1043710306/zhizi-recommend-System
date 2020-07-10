package inveno.spider.parser.idclass.other;

import inveno.spider.parser.exception.ExtractException;
import inveno.spider.parser.utils.EDCName;

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

import inveno.spider.common.utils.LoggerFactory;
import org.apache.log4j.Logger;

/**
 * thread-safe
 * 
 * @author Jerry Tang
 * @Date 2011-9-26
 * @Purpose 将此类修改成了线程安全的类
 */
public class XPathHelper
{
    private static final Logger LOG = LoggerFactory.make();
    private static final ThreadLocal<DocumentBuilder> mBuilder = new ThreadLocal<DocumentBuilder>()
    {
        protected DocumentBuilder initialValue()
        {
            try
            {
            	DocumentBuilderFactory docBFactory = DocumentBuilderFactory.newInstance();
            	docBFactory.setNamespaceAware(true);
                return docBFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e)
            {
                String msg = "Failed to create DocumentBuilder";
                throw new IllegalStateException(msg, e);
            }
        }
    };
    private static final ThreadLocal<XPath> mXpath = new ThreadLocal<XPath>()
    {
        protected XPath initialValue()
        {
        	XPath xpath = XPathFactory.newInstance().newXPath();
        	xpath.setNamespaceContext(new EDCName()); 
            return xpath;
        }
    };

    private static Pattern invalidUnicode = Pattern.compile("&#[5]\\d{4};");

    public XPathHelper()
    {
        // try {
        // DocumentBuilderFactory factory =
        // DocumentBuilderFactory.newInstance();
        // mBuilder = factory.newDocumentBuilder();
        // XPathFactory xpathFactory = XPathFactory.newInstance();
        // mXpath = xpathFactory.newXPath();
        // } catch (ParserConfigurationException e) {
        // throw new RuntimeException(e);
        // }
    }

    public DocumentBuilder getDocumentBuilder()
    {
        return mBuilder.get();
    }

    public XPath getXPath()
    {
        return mXpath.get();
    }

    public String getString(String expression, String content) throws ExtractException
    {
        try
        {
            Document doc = getDocumentBuilder().parse(
                    IOUtils.toInputStream(content, "UTF-8"));
            XPathExpression expr = getXPath().compile(expression);
            String result = (String) expr.evaluate(doc, XPathConstants.STRING);
            return StringUtils.trim(result);
        } catch (SAXParseException e)
        {
            // delete invalid character
            Matcher matcher = invalidUnicode.matcher(content);
            if (matcher.find())
            {
                content = matcher.replaceAll("");
                return getString(expression, content);
            }
            throw new ExtractException("Fail to find xpath", e);
        } catch (Exception e)
        {
            throw new ExtractException("Fail to find xpath", e);
        } finally
        {

            // mBuilder.reset();
            // mXpath.reset();
        }
    }

    public List<String> getListString(String expression, String content) throws ExtractException
    {
        try
        {
            Document doc = getDocumentBuilder().parse(IOUtils.toInputStream(content, "UTF-8"));
            XPathExpression expr = getXPath().compile(expression);
            NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            List<String> result = new ArrayList<String>(nodeList.getLength());
            for (int i = 0; i < nodeList.getLength(); i++)
            {
                result.add(nodeList.item(i).getTextContent());
            }
            return result;
        }
        catch (SAXParseException e)
        {
            // delete invalid character
            Matcher matcher = invalidUnicode.matcher(content);
            if (matcher.find())
            {
                content = matcher.replaceAll("");
                return getListString(expression, content);
            }
            throw new ExtractException("Fail to find xpath", e);
        }
        catch (Exception e)
        {
            throw new ExtractException("Fail to find xpath", e);
        }
        finally
        {
            // mBuilder.reset();
            // mXpath.reset();
        }
    }
    public List<String> getListString(String expression, String content, String charset) throws ExtractException
    {
        try
        {
            Document doc = getDocumentBuilder().parse(IOUtils.toInputStream(content, charset));
            XPathExpression expr = getXPath().compile(expression);
            NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            List<String> result = new ArrayList<String>(nodeList.getLength());
            for (int i = 0; i < nodeList.getLength(); i++)
            {
                result.add(nodeList.item(i).getTextContent());
            }
            return result;
        }
        catch (SAXParseException e)
        {
            // delete invalid character
            Matcher matcher = invalidUnicode.matcher(content);
            if (matcher.find())
            {
                content = matcher.replaceAll("");
                return getListString(expression, content);
            }
            throw new ExtractException("Fail to find xpath", e);
        }
        catch (Exception e)
        {
            throw new ExtractException("Fail to find xpath", e);
        }
        finally
        {
            // mBuilder.reset();
            // mXpath.reset();
        }
            }

    public NodeList getNodeList(String expression, String content)
            throws ExtractException
    {
        try
        {
            // Document doc = null;
            // synchronized (mBuilder) {
            // doc = mBuilder.parse(IOUtils.toInputStream(content, "UTF-8"));
            // }
            // XPathExpression expr = mXpath.compile(expression);
            Document doc = getDocumentBuilder().parse(
                    IOUtils.toInputStream(content, "UTF-8"));
            XPathExpression expr = getXPath().compile(expression);
            return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        } catch (SAXParseException e)
        {
            // delete invalid character
            Matcher matcher = invalidUnicode.matcher(content);
            if (matcher.find())
            {
                content = matcher.replaceAll("");
                return getNodeList(expression, content);
            }
            throw new ExtractException("Fail to find xpath", e);
        } catch (Exception e)
        {
            throw new ExtractException("Fail to find xpath", e);
        } finally
        {
            // mBuilder.reset();
            // mXpath.reset();
        }
    }

    public Document toNode(String content) throws ExtractException
    {
        try
        {

            // Document doc = null;
            // synchronized (mBuilder) {
            // doc = mBuilder.parse(IOUtils.toInputStream(content, "UTF-8"));
            // }
            Document doc = getDocumentBuilder().parse(
                    IOUtils.toInputStream(content, "UTF-8"));
            return doc;
        } catch (SAXParseException e)
        {
            // delete invalid character
            Matcher matcher = invalidUnicode.matcher(content);
            if (matcher.find())
            {
                content = matcher.replaceAll("");
                return toNode(content);
            }
            throw new ExtractException("Fail to convert to xml", e);
        } catch (Exception e)
        {
            throw new ExtractException("Fail to convert to xml", e);
        } finally
        {
            // mBuilder.reset();
            // mXpath.reset();
        }
    }

    // public List<String> getListStringWithTag(String expression, String
    // content) throws ExtractException {
    // try {
    // Document doc = mBuilder.parse(IOUtils.toInputStream(content, "UTF-8"));
    // XPathExpression expr = mXpath.compile(expression);
    // NodeList nodeList = (NodeList) expr.evaluate(doc,
    // XPathConstants.NODESET);
    //
    // List<String> strings = new ArrayList<String>(nodeList.getLength());
    // for (int i = 0; i < nodeList.getLength(); i++) {
    // Node node = nodeList.item(i);
    // strings.add(getXmlContentWithTags(node));
    // }
    // return strings;
    // } catch (Exception e) {
    // throw new ExtractException("Fail to find xpath",e);
    // }
    // }
    //
    // private String getXmlContentWithTags(Node node) {
    // StringBuilder sb = new StringBuilder();
    // appendText(node, sb);
    // return sb.toString();
    // }
    //
    // private void appendText(Node node, StringBuilder sb) {
    // if("#text".equals(node.getNodeName())) {
    // sb.append(node.getNodeValue());
    // } else {
    // sb.append("<" + node.getNodeName() + ">");
    // NodeList nodeList = node.getChildNodes();
    // for (int i = 0; i < nodeList.getLength(); i++) {
    // Node child = nodeList.item(i);
    // appendText(child, sb);
    // }
    // sb.append("</" + node.getNodeName() + ">");
    // }
    // }
}
