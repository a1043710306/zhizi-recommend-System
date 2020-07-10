package inveno.spider.parser.listener;


import inveno.spider.parser.Constants;
import inveno.spider.parser.ParseServer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Application Lifecycle Listener implementation class DemonListener
 *
 */

public class DemonListener implements ServletContextListener {

    /**
     * Default constructor. 
     */
    public DemonListener() {
        
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0) {
        if (Constants.START_ON_STARTUP)
        {
            start();
        }
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0) {
        stop();
    }
    
    private void start()
    {
        ParseServer.getInstance().start();
    }
    
    private void stop()
    {
        if(ParseServer.getInstance().isStarted())
        {
            ParseServer.getInstance().stop();
        }
    }
    
	
}
