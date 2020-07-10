package inveno.spider.common.mail;

import javax.mail.PasswordAuthentication;

public class SmtpAuthenticator extends javax.mail.Authenticator
{
	private PasswordAuthentication auth;
	//
	public SmtpAuthenticator(String userName, String password)
	{
		auth = new PasswordAuthentication(userName, password); 
	}
	//
	public PasswordAuthentication getPasswordAuthentication() 
	{
		return auth;
	} 
}