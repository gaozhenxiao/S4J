package pinche;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

/**
 * @author GaoZX
 * ISelfDescriptiveΪ�������ӿ�
 * SelfDescriptiveServletΪ��������ĸ��࣬�޷�ʵ����������̳�ʵ��
 * �½��̳������дSelfDescriptiveServlet���������󷽷����ֱ��Ӧget��post��action����
 * ����д�����ClientServerInterfaces��ע��һ�£������û��Ϳ���ͨ��ClientServerInterfaces��ȡ���еĽӿ�
 * ͨ��ֱ�ӷ��ʸ�servletҲ���Ի�ȡ��������
 * ����ķ�����������get_action_XXX
 * �������ڼ���ԭ�������getInterfaceDesctription()û�а취�����б���˱���ͨ����д�������˵��
 * ���忴�����ַ��������ã���ȻҲ�����ر��ࡣҲû���뵽�ر�õķ���
 * ����ķ����ǣ��½������ʱ��ֻ��Ҫʵ�ַ��������ˡ�
 * 2������1����Ҫʵ�ֽӿڵ��඼����ĳ���ط��������Ϳ���ʵ��һ���ط����ʣ��õ����еĽӿڣ�
 * ����tomcat��������̬���أ�ĳ�ಢ��֪���������Ƿ���أ��Զ�������һ���Ѷ�
 * 2��servlet���б�ķ�����Ӧ��ֻ������Ҫ�û������ķ�����Ҳ��������ķ�����
 * �������ͨ�������������ƺ�Ҳû�п�����İ취
 * 
 * Servlet implementation class SelfDescriptiveServlet
 */
//@WebServlet("/SelfDescriptiveServlet")
public abstract class SelfDescriptiveServlet extends HttpServlet implements ISelfDescriptive{
	public static ArrayList<SelfDescriptiveServlet> descriptiveClasses=new ArrayList<>();
	private static final long serialVersionUID = 1L;
	protected HttpServletRequest request =null;
	protected HttpServletResponse response=null;
//	Map<String, ArrayList<String>> parameters=new HashMap<String, ArrayList<String>>();
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SelfDescriptiveServlet() {
        super();
//        descriptiveClasses.add(this);
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request1, HttpServletResponse response1) throws ServletException, IOException {
		this.request=request1;
		this.response=response1;
		String action = request.getParameter("action");
		Object callback =null;
		if( action==null || action.length()==0 )
		{
			callback="Error:action needed";
			callback+="\n";
			callback+=getInterfaceDesctription();
		}
		else
		{
			callback=get_action(action);
		}
		response.setContentType("application/x-json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.println( callback );
		out.close();
	}

	public abstract Object get_action(String action);

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request1, HttpServletResponse response1) throws ServletException, IOException {
		this.request=request1;
		this.response=response1;
		String action = request.getParameter("action");
		Object callback =null;
		if( action==null || action.length()==0 )
		{
			callback="Error:action needed";
		}
		else
		{
			callback=post_action(action);
		}
		response.setContentType("application/x-json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.println( callback );
		out.close();
	}
	public abstract Object post_action(String action);
	
	public String actionTrim(String action)
	{
		if( action.startsWith("get") 	)
			action=action.substring("get_action".length(), action.length());
		else if( action.startsWith("post") 	)
			action=action.substring("post_action".length(), action.length());
		if( action.startsWith("_"))
			action=action.substring(1,action.length());
		return action;
	}
	@Override
	public String getInterfaceDesctription() 
	{
		Class cla = this.getClass();
		Method[] method=cla.getDeclaredMethods();
		JSONObject root =new JSONObject();
        for(Method m:method)
        { 
        	boolean condition=!m.getName().contains("main");
        	condition=condition&& (m.toString().contains("action_")  || m.toString().contains("get_") ||  m.toString().contains("post_") );
        	if( condition==true )
        	{
        		JSONObject obj =new JSONObject();
        		String url = "http://"+BackUp.MainServerAddress+"/"+m.getDeclaringClass().getSimpleName() ;
        		obj.put("address", url );

        		if( m.getName().startsWith("get"))
        			obj.put("method", "GET");
        		else if( m.getName().startsWith("post"))
        			obj.put( "method", "POST");

        		String shortMethodName=m.getName();
        		if( shortMethodName.startsWith("get") 	)
        			shortMethodName=shortMethodName.substring("get_action".length(), shortMethodName.length());
        		else if( shortMethodName.startsWith("post") 	)
        			shortMethodName=shortMethodName.substring("post_action".length(), shortMethodName.length());
        		if( shortMethodName.startsWith("_"))
        			shortMethodName=shortMethodName.substring(1,shortMethodName.length());
        		obj.put("parameter action=", shortMethodName);
        		
        		System.out.println( m.toString()); 
        		System.out.println( m.getName() ); 
        		
//        		Class[] parameterTypes = m.getParameterTypes();
//        		for( int i=0;i<parameterTypes.length;i++)
//        		{
//        			System.out.println(parameterTypes[i]);
//        			obj.put("parameter"+i, parameterTypes[i]);
//        		}
        		
        		Type type=  m.getGenericReturnType();
        		System.out.println(type);
        		obj.put("return", type);
        		
        		root.put(m.getName() , obj);
        	}
        }
        return root.toString();
	}
}
