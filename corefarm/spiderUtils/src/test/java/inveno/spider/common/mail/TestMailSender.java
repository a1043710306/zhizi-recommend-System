package inveno.spider.common.mail;

import junit.framework.TestCase;

/**
 * Created by dell on 2016/4/16.
 */
public class TestMailSender extends TestCase
{
	public void testSendMail()
	{
		try
		{
			String smtp_username = "report.crawler@inveno.cn";
			String smtp_password = "Spider@2016";
			String smtp_host     = "smtp.exmail.qq.com";
			int    smtp_port     = 465;
			String mailSender    = "report.crawler@inveno.cn";
			String mailReceiver  = "report.crawler@inveno.cn";
			boolean fSSLConnection = true;
			MailSender ms = new MailSender(smtp_host, smtp_port, smtp_username, smtp_password, fSSLConnection);
			ms.setMailFrom(mailSender);
			ms.setSubject("testing email");
			ms.setMailTo(mailReceiver);
			ms.setContent("test content from crawler");
			//ms.setAttachedFile(new String[0]);
			//ms.send();
			assertTrue(true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
