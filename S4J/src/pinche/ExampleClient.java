package pinche;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class ExampleClient extends WebSocketClient {

	public ExampleClient( URI serverUri , Draft draft ) {
		super( serverUri, draft );
	}

	public ExampleClient( URI serverURI ) {
		super( serverURI );
	}

	@Override
	public void onOpen( ServerHandshake handshakedata ) {
		System.out.println( "opened connection" );
		// if you plan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient
	}

	@Override
	public void onMessage( String message ) {
		System.out.println( "received: " + message );
		if( message.contains(":") )
		{
			String[] detail = message.split(":");
			if( detail[0].equalsIgnoreCase("sql") )
			{
				try
				{
					MyDatabase database = new MyDatabase();
					database.getStatementExecute( detail[1] );
					database.close();
				}
				catch (SQLException e) {
					e.printStackTrace();
				}
				save2File( detail[1] );
			}
			if( detail[0].equalsIgnoreCase("image") )
			{
				String imageFile=detail[1];
			}
		}
	}

	public void downloadImage2Local(String remoteImage)
	{
		String strUrl = "http://"+BackUp.MainServerAddress+"/UpDownLoad?action=backup&fileName="+remoteImage;
		try
		{
			URL url = new URL(strUrl); 
			URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);
			HttpGet httpGet = new HttpGet( uri );
			System.out.println( "请求网址为："+ uri );
			CloseableHttpClient httpclient = HttpClients.createDefault();
			CloseableHttpResponse  response = httpclient.execute(httpGet);
			HttpEntity entity = response.getEntity();

			if (response.getStatusLine().getStatusCode() >= 400) 
			{
				throw new IOException("Got bad response, error code = " + response.getStatusLine().getStatusCode() + " imageUrl: " + strUrl);
			}
			if (entity != null) 
			{
				InputStream input = entity.getContent();
				
				String localFileDir= System.getProperty("user.dir")+File.separator+".."+File.separator+".."+ File.separator+"server_backup"+File.separator+remoteImage ;
				System.out.println("文件保存路径为:"+localFileDir);  
				OutputStream output = new FileOutputStream(new File( localFileDir ));
//				byte b[] = new byte[1024];
//				int j = 0;
//				while( (j = input.read(b))!=-1){
//					output.write(b,0,j);
//				}
				IOUtils.copy(input, output);
				output.flush();
				output.close();
			}
			httpGet.releaseConnection();
			response.close();
		}
		catch( Exception e)
		{
			e.printStackTrace();
		}
	}

	private void save2File(String sql) 
	{
		GregorianCalendar today = new GregorianCalendar();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String absolutePath = System.getProperty("user.dir")+File.separator+".."+File.separator+".."+ File.separator+"server_backup"+File.separator+sdf.format(today.getTime())+"_backup.sql" ;
		System.out.println( absolutePath );
		File sqlFile = new File(absolutePath);
		if( sqlFile.exists() )
		{
			System.out.println( "back up file exists!" );
		}
		else
		{
			try {
				sqlFile.createNewFile();
				System.out.println( "back up file created!" );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter(sqlFile, true));
			out.println(sql);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//        @Override
	public void onFragment( Framedata fragment ) {
		System.out.println( "received fragment: " + new String( fragment.getPayloadData().array() ) );
	}

	@Override
	public void onClose( int code, String reason, boolean remote ) {
		// The codecodes are documented in class org.java_websocket.framing.CloseFrame
		System.out.println( "Connection closed by " + ( remote ? "us" : "remote peer" )  + " because " +reason );
	}

	@Override
	public void onError( Exception ex ) {
		ex.printStackTrace();
		// if the error is fatal then onClose will be called additionally
	}

	public static void main( String[] args ) throws URISyntaxException, InterruptedException 
	{
		//                ExampleClient c = new ExampleClient( new URI( "ws://localhost/S4J/websocket" ), new Draft_17() ); 
		ExampleClient c = new ExampleClient( new URI( "ws://120.26.76.25/S4J/websocket" ), new Draft_17() );
//		System.out.println( c.getReadyState()  );
//		c.connectBlocking();
//		//                c.connect();
//		System.out.println("已打开连接");
//		System.out.println( c.getReadyState()  );
//		c.send("message from example client");
//		c.send("uid:00");
//		c.send("to uid:00:hello");
//		c.send("broadcast: broadcast from example client");
//		System.out.println( c.getReadyState()  );
//		c.close();
//		System.out.println( c.getReadyState()  );
//		//                c.closeBlocking();
//		System.out.println("已关闭连接");
		//		c.save2File("content test");
		c.downloadImage2Local( "00.jpg" );
	}
}