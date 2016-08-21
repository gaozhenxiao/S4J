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
 * 连接服务器，接收有待备份的sql命令，以及下载有待更新的用户头像图片
 * 启动后，主动连接服务器，如果本程序就是服务器，对外发送备份的内容；如果是备份服务器，则接收websocket内容，并且下载
 * SQL命令：先写到本地的sql列表文件中，然后逐个执行
 * 图片：直接现在到本地服务器目录中
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
					//如果断了，重新连接
					checkMainServer();
					if( mainServerSign==false )
					{
						if( c==null || c.getReadyState()!=1  )  //0：初始状态；1：正常；2：关闭
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
			System.out.println("已打开连接");
			c.send("server: "+Signature.serverSigniture);
			//			c.close();
			//			System.out.println("已关闭连接");
		} catch (InterruptedException | URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 用了个绝妙的主意
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
			System.out.println( "请求网址为："+ uri );
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpResponse response = httpclient.execute(httpGet);
			String responseString =  EntityUtils.toString( response.getEntity() , "UTF-8");
			responseString=responseString.trim();
			httpGet.releaseConnection();
			System.out.println( "主服务器signiture："+ responseString );
			System.out.println( "本地服务器signiture："+ Signature.serverSigniture );
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
