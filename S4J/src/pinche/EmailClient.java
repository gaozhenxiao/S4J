package pinche;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

public class EmailClient {

	public void  sentEmail(String content,String subject, String emailAddress)
	{
		sent163Email(content,subject, emailAddress);
	}
	private void sent163Email(String content, String subject, String emailAdress)
	{
		if( emailAdress.contains("@")==false)
			return ;
		HtmlEmail email = new HtmlEmail();
		email.setHostName("smtp.163.com");
		email.setSSLOnConnect(true);
		email.setAuthentication("18521511571@163.com","pi3.14");
		email.setCharset("UTF-8");
		try {
			email.addTo(emailAdress);//要发送的地址
			email.setFrom("18521511571@163.com");//必须和Authentication使用的用户相同，否则失败
			email.setSubject(subject);//要发送的主题
			//            email.setMsg(content);//要发送的内容
			email.setHtmlMsg(content);
			email.send();
		} catch (EmailException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args)throws Exception
	{
		GregorianCalendar now  =  new GregorianCalendar();
		now.add(GregorianCalendar.DAY_OF_YEAR, -1);
		String detail = "今日大宗交易为：\n";
		System.out.println(detail);
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");

		EmailClient emailclient = new EmailClient();
//		emailclient.sentEmail("test", sdf.format(now.getTime()) + "大宗交易");
		//这里我测试了一下，用163的是的确可以的，OK~
	}

}
