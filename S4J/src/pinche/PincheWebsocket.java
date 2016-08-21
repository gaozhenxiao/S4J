package pinche;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
无法给客户端动态发信息的根本原因，客户端是内网，没有唯一的ip地址。
内网路由器根据nat协议将内网ip转化为对外的ip，并记录这个转换表。
一次http通讯结束后，路由器回删除转换表。http1.0升级到1.1后，可以建立长链接，既不结束response。
但运营商为了减轻路由表压力，回隔段时间删除一次。
因此即便是长链接也需要不断发送心跳包。
服务器为了跟所有有必要的客户端发送数据，需要保持一个很大的长链接表。
此外，对每个客户端都开一个线程处理，会压垮服务器，JVM单机线程大概支持1万，但实际上1000多就无法连接上。
因此必须使用异步非阻塞，用几个线程处理所有的链接。
NETTY是个来源包，似乎可以解决这个问题。此外还有h5的websocket协议，还有pushlet。但是搜索服务器推送，大部分使用类似mqtt，搞不清有点。
mina同netty类似，通过javaio提供基于tcpip和udpip协议提供了抽象的事件驱动的异步的api。
考虑到websocket比较新，采用该协议。

链接方法：
在客户端项目中添加java_websocket.jar包，添加引用
新建类，继承WebSocketClient，实现对应接口即可。

 ExampleClient c = new ExampleClient( new URI( "ws://localhost/S4J/websocket" ), new Draft_17() ); 
                c.connectBlocking();
                System.out.println("已打开连接");
                c.send("message from example client");
                c.send("uid:00");
                c.send("to uid:00:hello");
                c.send("broadcast: broadcast from example client");
                c.close();
                System.out.println("已关闭连接");
当司机端链接服务器后，通过上述命令链接websocket，
并通过c.send("uid:00");发送本机的uid。消息格式：  uid:XXXXXX
接受的信息在
public void onMessage( String message ) { }
中实现
 * @author Administrator
 *
 */


//该注解用来指定一个URI，客户端可以通过这个URI来连接到WebSocket。类似Servlet的注解mapping。无需在web.xml中配置。
@ServerEndpoint("/websocket")
public class PincheWebsocket {
	//静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
	private static int onlineCount = 0;

	//concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
	private static CopyOnWriteArraySet<PincheWebsocket> webSocketSet = new CopyOnWriteArraySet<PincheWebsocket>();
	private static CopyOnWriteArraySet<PincheWebsocket> backupServers = new CopyOnWriteArraySet<PincheWebsocket>();
	//使用2个相反的表，是为了方便的取已经发送uid的websocket，并在结束的时候删除。只用一个表，就需要遍历HashMap，以删除map4websockets中uid对应的实例。删除是为了避免表过大
	private static ConcurrentHashMap<PincheWebsocket, String> map4uids = new ConcurrentHashMap<PincheWebsocket, String>();  
	//	private static ConcurrentHashMap<String, PincheWebsocket> map4websockets = new ConcurrentHashMap<String, PincheWebsocket>();  
	private static Map<String, PincheWebsocket> map4websockets = Collections.synchronizedMap(new HashMap<String, PincheWebsocket>());
	//与某个客户端的连接会话，需要通过它来给客户端发送数据
	private Session session;

	/**
	 * 连接建立成功调用的方法
	 * @param session  可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
	 */
	@OnOpen
	public void onOpen(Session session){
		this.session = session;
		webSocketSet.add(this);     //加入set中
		addOnlineCount();           //在线数加1
		System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
	}

	/**
	 * 连接关闭调用的方法
	 */
	@OnClose
	public void onClose(){
		webSocketSet.remove(this);  //从set中删除
		subOnlineCount();           //在线数减1    
		System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
		String uid = map4uids.get(this);
		map4uids.remove(this);
		if( uid!=null && uid.length()>0 )
		{
			map4websockets.remove(uid);
		}
		if( backupServers.contains(this))
		{
			backupServers.remove(this);
			System.out.println("备份服务器关闭");
		}
	}

	/**
	 * 收到客户端消息后调用的方法
	 * @param message 客户端发送过来的消息
	 * @param session 可选的参数
	 */
	@OnMessage
	public void onMessage(String message, Session session) 
	{
		System.out.println("来自客户端的消息:" + message);

		if( message.contains(":") )
		{
			String[] detail = message.split(":");
			if( detail[0].equalsIgnoreCase("uid") )
			{
				if( detail.length>=2 )
				{
					String uid = detail[1];
					map4uids.put(this, uid);
					map4websockets.put(uid, this);
				}
			}
			else if( detail[0].equalsIgnoreCase("to uid") )
			{
				if( detail.length>=3 )
				{
					String uid = detail[1];
					try {
						sentMessage2Uid( "server 转发:" + detail[2], uid);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			else if ( detail[0].equalsIgnoreCase("broadcast")   )
			{
				message = detail[1];
				//群发消息
				for(PincheWebsocket item: webSocketSet){             
					try {
						item.sendMessage("server 转发:" +message);
					} catch (IOException e) {
						e.printStackTrace();
						continue;
					}
				}
			}
			else if( detail[0].equalsIgnoreCase("server") )
			{
				if( detail.length>=2 )
				{
					String uid = detail[1];
					backupServers.add(this);
					System.out.println("有备份服务器加入");
				}
			}
		}

	}

	/**
	 * 发生错误时调用
	 * @param session
	 * @param error
	 */
	@OnError
	public void onError(Session session, Throwable error){
		System.out.println("发生错误");
		error.printStackTrace();
	}

	/**
	 * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
	 * @param message
	 * @throws IOException
	 */
	public void sendMessage(String message) throws IOException{
		this.session.getBasicRemote().sendText(message);
		//this.session.getAsyncRemote().sendText(message);
	}

	public static synchronized int getOnlineCount() {
		return onlineCount;
	}

	public static synchronized void addOnlineCount() {
		PincheWebsocket.onlineCount++;
	}

	public static synchronized void subOnlineCount() {
		PincheWebsocket.onlineCount--;
	}

	public static void sentMessage2Uid(String message, String uid) throws IOException
	{
		if( map4websockets!=null)
		{
			if( map4websockets.get(uid)!=null )
			{
				PincheWebsocket socket =  map4websockets.get(uid);
				socket.sendMessage(message);
			}
		}
	}
	public static void sent2BackupServer(String message) throws IOException
	{
		for(PincheWebsocket item: backupServers)
		{             
			try 
			{
				String[] detail = message.split(":");
				if( detail[0].equalsIgnoreCase("sql") )
				{
					System.out.println("向备份服务器传送sql："+message);
				}
				else if( detail[0].equalsIgnoreCase("image") )
				{
					System.out.println("向备份服务器传送图片名称："+message);
				}
				item.sendMessage( message);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
		}
	}
}