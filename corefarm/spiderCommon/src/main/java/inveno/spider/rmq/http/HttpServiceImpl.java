package inveno.spider.rmq.http;


import inveno.spider.common.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 
 * 
 * @version 1.0 2014-1-10
 * @author jerrytang@wisers.com
 */
public class HttpServiceImpl implements HttpService
{


    @Override
    public String get(String user,String pass,String apiUrl) throws AuthorizationException
    {

        // (Request-Line) GET /api/queues HTTP/1.1
        // Host ngp06.wisers.com:15672
        // User-Agent Mozilla/5.0 (Windows NT 5.1; rv:26.0) Gecko/20100101
        // Firefox/26.0
        // Accept
        // text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
        // Accept-Language zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3
        // Accept-Encoding gzip, deflate
        // Cookie auth=Y3Jhd2xlcjMxOmNyYXdsZXIzMQ%3D%3D; m=34e2:|796a:t
        // Authorization Basic Y3Jhd2xlcjMxOmNyYXdsZXIzMQ==
        // Connection keep-alive
        final String API_URL = Constants.RABBIT_HOST_URL + apiUrl;
        URL postUrl = null;
        HttpURLConnection connection = null;
        BufferedReader reader =null;
        String authorizationEncodeString=null;
        

        if(null==user || null==pass)
        {
            throw new AuthorizationException("RabbitMQ connect user or pass is blank.");
        }
        String temp = user+":"+pass;
        authorizationEncodeString = base64Encode(temp.getBytes());

        
        StringBuilder sb = new StringBuilder();
        try
        {
            postUrl = new URL(API_URL);
            connection = (HttpURLConnection) postUrl
                    .openConnection();
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 5.1; rv:26.0) Gecko/20100101 Firefox/26.0");
            connection.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            connection.setRequestProperty("Authorization","Basic "+authorizationEncodeString);

            connection.connect();
            reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null)
            {
                sb.append(line);
            }

        } catch (MalformedURLException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            throw new AuthorizationException();
        }finally
        {

            if (null != reader)
            {
                try
                {
                    reader.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            connection.disconnect();
            
        }

        return sb.toString();
    }
    
    public String base64Encode(byte[] bstr)
    {
        return (new sun.misc.BASE64Encoder()).encode(bstr);
    }

}
