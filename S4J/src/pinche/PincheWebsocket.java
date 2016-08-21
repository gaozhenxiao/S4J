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
�޷����ͻ��˶�̬����Ϣ�ĸ���ԭ�򣬿ͻ�����������û��Ψһ��ip��ַ��
����·��������natЭ�齫����ipת��Ϊ�����ip������¼���ת����
һ��httpͨѶ������·������ɾ��ת����http1.0������1.1�󣬿��Խ��������ӣ��Ȳ�����response��
����Ӫ��Ϊ�˼���·�ɱ�ѹ�����ظ���ʱ��ɾ��һ�Ρ�
��˼����ǳ�����Ҳ��Ҫ���Ϸ�����������
������Ϊ�˸������б�Ҫ�Ŀͻ��˷������ݣ���Ҫ����һ���ܴ�ĳ����ӱ�
���⣬��ÿ���ͻ��˶���һ���̴߳�����ѹ���������JVM�����̴߳��֧��1�򣬵�ʵ����1000����޷������ϡ�
��˱���ʹ���첽���������ü����̴߳������е����ӡ�
NETTY�Ǹ���Դ�����ƺ����Խ��������⡣���⻹��h5��websocketЭ�飬����pushlet�������������������ͣ��󲿷�ʹ������mqtt���㲻���е㡣
minaͬnetty���ƣ�ͨ��javaio�ṩ����tcpip��udpipЭ���ṩ�˳�����¼��������첽��api��
���ǵ�websocket�Ƚ��£����ø�Э�顣

���ӷ�����
�ڿͻ�����Ŀ�����java_websocket.jar�����������
�½��࣬�̳�WebSocketClient��ʵ�ֶ�Ӧ�ӿڼ��ɡ�

 ExampleClient c = new ExampleClient( new URI( "ws://localhost/S4J/websocket" ), new Draft_17() ); 
                c.connectBlocking();
                System.out.println("�Ѵ�����");
                c.send("message from example client");
                c.send("uid:00");
                c.send("to uid:00:hello");
                c.send("broadcast: broadcast from example client");
                c.close();
                System.out.println("�ѹر�����");
��˾�������ӷ�������ͨ��������������websocket��
��ͨ��c.send("uid:00");���ͱ�����uid����Ϣ��ʽ��  uid:XXXXXX
���ܵ���Ϣ��
public void onMessage( String message ) { }
��ʵ��
 * @author Administrator
 *
 */


//��ע������ָ��һ��URI���ͻ��˿���ͨ�����URI�����ӵ�WebSocket������Servlet��ע��mapping��������web.xml�����á�
@ServerEndpoint("/websocket")
public class PincheWebsocket {
	//��̬������������¼��ǰ������������Ӧ�ð�����Ƴ��̰߳�ȫ�ġ�
	private static int onlineCount = 0;

	//concurrent�����̰߳�ȫSet���������ÿ���ͻ��˶�Ӧ��MyWebSocket������Ҫʵ�ַ�����뵥һ�ͻ���ͨ�ŵĻ�������ʹ��Map����ţ�����Key����Ϊ�û���ʶ
	private static CopyOnWriteArraySet<PincheWebsocket> webSocketSet = new CopyOnWriteArraySet<PincheWebsocket>();
	private static CopyOnWriteArraySet<PincheWebsocket> backupServers = new CopyOnWriteArraySet<PincheWebsocket>();
	//ʹ��2���෴�ı���Ϊ�˷����ȡ�Ѿ�����uid��websocket�����ڽ�����ʱ��ɾ����ֻ��һ��������Ҫ����HashMap����ɾ��map4websockets��uid��Ӧ��ʵ����ɾ����Ϊ�˱�������
	private static ConcurrentHashMap<PincheWebsocket, String> map4uids = new ConcurrentHashMap<PincheWebsocket, String>();  
	//	private static ConcurrentHashMap<String, PincheWebsocket> map4websockets = new ConcurrentHashMap<String, PincheWebsocket>();  
	private static Map<String, PincheWebsocket> map4websockets = Collections.synchronizedMap(new HashMap<String, PincheWebsocket>());
	//��ĳ���ͻ��˵����ӻỰ����Ҫͨ���������ͻ��˷�������
	private Session session;

	/**
	 * ���ӽ����ɹ����õķ���
	 * @param session  ��ѡ�Ĳ�����sessionΪ��ĳ���ͻ��˵����ӻỰ����Ҫͨ���������ͻ��˷�������
	 */
	@OnOpen
	public void onOpen(Session session){
		this.session = session;
		webSocketSet.add(this);     //����set��
		addOnlineCount();           //��������1
		System.out.println("�������Ӽ��룡��ǰ��������Ϊ" + getOnlineCount());
	}

	/**
	 * ���ӹرյ��õķ���
	 */
	@OnClose
	public void onClose(){
		webSocketSet.remove(this);  //��set��ɾ��
		subOnlineCount();           //��������1    
		System.out.println("��һ���ӹرգ���ǰ��������Ϊ" + getOnlineCount());
		String uid = map4uids.get(this);
		map4uids.remove(this);
		if( uid!=null && uid.length()>0 )
		{
			map4websockets.remove(uid);
		}
		if( backupServers.contains(this))
		{
			backupServers.remove(this);
			System.out.println("���ݷ������ر�");
		}
	}

	/**
	 * �յ��ͻ�����Ϣ����õķ���
	 * @param message �ͻ��˷��͹�������Ϣ
	 * @param session ��ѡ�Ĳ���
	 */
	@OnMessage
	public void onMessage(String message, Session session) 
	{
		System.out.println("���Կͻ��˵���Ϣ:" + message);

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
						sentMessage2Uid( "server ת��:" + detail[2], uid);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			else if ( detail[0].equalsIgnoreCase("broadcast")   )
			{
				message = detail[1];
				//Ⱥ����Ϣ
				for(PincheWebsocket item: webSocketSet){             
					try {
						item.sendMessage("server ת��:" +message);
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
					System.out.println("�б��ݷ���������");
				}
			}
		}

	}

	/**
	 * ��������ʱ����
	 * @param session
	 * @param error
	 */
	@OnError
	public void onError(Session session, Throwable error){
		System.out.println("��������");
		error.printStackTrace();
	}

	/**
	 * ������������漸��������һ����û����ע�⣬�Ǹ����Լ���Ҫ��ӵķ�����
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
					System.out.println("�򱸷ݷ���������sql��"+message);
				}
				else if( detail[0].equalsIgnoreCase("image") )
				{
					System.out.println("�򱸷ݷ���������ͼƬ���ƣ�"+message);
				}
				item.sendMessage( message);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
		}
	}
}