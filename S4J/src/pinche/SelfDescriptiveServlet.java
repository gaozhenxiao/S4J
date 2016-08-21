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
 * ISelfDescriptive为自描述接口
 * SelfDescriptiveServlet为自描述类的父类，无法实例化，必须继承实现
 * 新建继承类后，重写SelfDescriptiveServlet的两个抽象方法，分别对应get和post的action处理
 * 子类写完后，在ClientServerInterfaces中注册一下，这样用户就可以通过ClientServerInterfaces获取所有的接口
 * 通过直接访问该servlet也可以获取方法帮助
 * 子类的方法命名规则：get_action_XXX
 * 此外由于技术原因，子类的getInterfaceDesctription()没有办法参数列表，因此必须通过重写方法添加说明
 * 整体看，这种方法可以用，虽然也不是特别简洁。也没有想到特别好的方法
 * 理想的方法是，新建子类的时候只需要实现方法就行了。
 * 2个需求：1、需要实现接口的类都加入某个地方，这样就可以实现一个地方访问，得到所有的接口；
 * 由于tomcat是容器动态加载，某类并不知道别的类的是否加载；自动加载有一定难度
 * 2、servlet中有别的方法，应该只返回需要用户看到的方法，也就有特殊的方法。
 * 这个除了通过函数名区别，似乎也没有看到别的办法
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
