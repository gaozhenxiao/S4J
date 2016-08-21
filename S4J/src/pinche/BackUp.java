package pinche;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
/**
 * ���ӷ������������д����ݵ�sql����Լ������д����µ��û�ͷ��ͼƬ
 * �������������ӷ������������������Ƿ����������ⷢ�ͱ��ݵ����ݣ�����Ǳ��ݷ������������websocket���ݣ���������
 * SQL�����д�����ص�sql�б��ļ��У�Ȼ�����ִ��
 * ͼƬ��ֱ�����ڵ����ط�����Ŀ¼��
 * @author Administrator
 *
 */
public class BackUp extends HttpServlet {

	private static final long serialVersionUID = -2934633394493198910L;
	public static boolean mainServerSign=false;
	public static final String MainServerAddress="120.26.76.25/S4J";
	ExampleClient c=null;
	boolean initiated=false;

	public BackUp() {
		super();
	}

	public void init() throws ServletException {  
		if( initiated==true )
			return;
		System.out.println("init BackUp ... ");
		initiated=true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				while( true )
				{
					//connect to server;
					//������ˣ���������
					checkMainServer();
					if( mainServerSign==false )
					{
						if( c==null || c.getReadyState()!=1  )  //0����ʼ״̬��1��������2���ر�
							connect2MainServer();
					}
					try 
					{
						Thread.sleep(60*60*1000);
					}
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}

				}
			}
		}).start();
	}
	protected void connect2MainServer() {
		try {
			c = new ExampleClient( new URI( "ws://"+MainServerAddress+"/websocket" ), new Draft_17() ); 
			c.connectBlocking();
			System.out.println("�Ѵ�����");
			c.send("server: "+Signature.serverSigniture);
			//			c.close();
			//			System.out.println("�ѹر�����");
		} catch (InterruptedException | URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ���˸����������
	 * @return
	 */
	public boolean isMainServer()
	{
		try
		{
			String strUrl = "http://"+ MainServerAddress+"/Signature";
			URL url = new URL(strUrl); 
			URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);
			HttpGet httpGet = new HttpGet( uri );
			System.out.println( "������ַΪ��"+ uri );
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpResponse response = httpclient.execute(httpGet);
			String responseString =  EntityUtils.toString( response.getEntity() , "UTF-8");
			responseString=responseString.trim();
			httpGet.releaseConnection();
			System.out.println( "��������signiture��"+ responseString );
			System.out.println( "���ط�����signiture��"+ Signature.serverSigniture );
			if( responseString.equalsIgnoreCase( Double.toString(Signature.serverSigniture ) ) )
				return true;
			else
				return false;
		}
		catch( Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	protected void checkMainServer() 
	{
		try 
		{
			Thread.sleep(6*1000);
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		mainServerSign=isMainServer();
	}


	public static void main(String[] args)
	{
		BackUp backup = new BackUp();
		System.out.println(  backup.isMainServer() );
	}
}
