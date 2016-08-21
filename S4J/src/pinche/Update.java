package pinche;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URLEncoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Servlet implementation class Update
 */
@WebServlet("/Update")
public class Update extends HttpServlet implements ISelfDescriptive{
	private static final long serialVersionUID = 1L;
	private String filedir=null;  
	private String latestClientVersion = null;
	private HttpServletRequest request =null;
	private HttpServletResponse response=null;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Update() {
        super();
    }
    
    public void init(ServletConfig config) throws ServletException {  
		filedir=config.getServletContext().getRealPath("/")+"../../client_update";  
		System.out.println("filedir="+filedir);  
		File versionFile = new File( filedir+"/version.xml");
		if( versionFile.exists() == false)
			return ;
		
		SAXBuilder builder = new SAXBuilder();
		org.jdom.Document jDomDoc;
		try 
		{
			jDomDoc = builder.build( versionFile  );
			Element rootNode = jDomDoc.getRootElement();
			latestClientVersion = rootNode.getChild("version").getText();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 * 没有参数，action=null，直接下载
	 * action=Download，下载，一言不合就下载
	 * action=VersionCheck，返回json格式的最新版本信息
	 */
	protected void doGet(HttpServletRequest request1, HttpServletResponse response1) throws ServletException, IOException {
		this.request=request1;
		this.response=response1;
		String action = request.getParameter("action");
		Object callback =null;
		if( action==null || action.length()==0 || action.equalsIgnoreCase("download") )
		{
			callback = get_action_download();
		}
		else if ( action.equalsIgnoreCase("VersionCheck") )
		{
			callback = this.get_action_versionCheck();
		}
		response.setContentType("application/x-json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.println( callback );
		out.close();
	}

	private Object get_action_download() throws UnsupportedEncodingException, FileNotFoundException, IOException {
		Object callback=null;
		String fullFilePath = filedir +File.separator+ "qingpinche.apk";
		/*读取文件*/
		File file = new File(fullFilePath);
		//      System.out.println("file="+file.getAbsolutePath());  
		/*如果文件存在*/
		if (file.exists()) 
		{
			String filename = URLEncoder.encode(file.getName(), "utf-8");
			response.reset();
			response.setContentType("application/x-msdownload");
			response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
			int fileLength = (int) file.length();
			response.setContentLength(fileLength);
			/*如果文件长度大于0*/
			if (fileLength != 0) 
			{
				/*创建输入流*/
				InputStream inStream = new FileInputStream(file);
				byte[] buf = new byte[4096];
				/*创建输出流*/
				ServletOutputStream servletOS = response.getOutputStream();
				int readLength;
				while (((readLength = inStream.read(buf)) != -1)) {
					servletOS.write(buf, 0, readLength);
				}
				inStream.close();
				servletOS.flush();
				servletOS.close();
				return callback;
			}
		}
		else
		{
			callback="Error:File does not exist on the server";
		}
		return callback;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 * 这个方法本是想为了方便上传文件用，但似乎用处不大，孙晓东可以直接通过WinSCP上传，文件目录应该在项目S4J之外，方便我更新服务器程序的时候不影响最新的版本文件
	 * windows中放在..apache-tomcat/client_update中，linux放在/usr/apache-tomcat/client_update中
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	/**
	 * 检查版本
	 * @return
	 */
	public String get_action_versionCheck()
	{
		return latestClientVersion;
	}
	
	@Override
	public String getInterfaceDesctription() {
		Class cla = this.getClass();
		Method[] method=cla.getDeclaredMethods(); 
        for(Method m:method){ 
        	if( m.toString().contains("action_"))
        	{
        		System.out.println("获取全部公共方法的方法:"+m.toString()); 
        		Type type=  m.getGenericReturnType();
        		System.out.println(type);
        		Class[] parameterTypes = m.getParameterTypes();
        		for( int i=0;i<parameterTypes.length;i++)
        			System.out.println(parameterTypes[i]);
        		Annotation[] notes=m.getAnnotations();
        		for( int i=0;i<notes.length;i++)
        			System.out.println(notes[i]);
        	}
        }
//        try { 
//            //获取指定无参方法 
//            //第二个参数表示参数类型，参数NUll表示无参数方法 
//            Method method1=cla.getMethod("doPost", null); 
//            System.out.println("获取无参公共方法的方法:"+method1.toString()); 
//        } catch (SecurityException e) { 
//            // TODO Auto-generated catch block 
//            e.printStackTrace(); 
//        } catch (NoSuchMethodException e) { 
//            // TODO Auto-generated catch block 
//            e.printStackTrace(); 
//        } 
        
		return null;
	}
	public static void main(String[]  args)
	{
		Update update = new Update();
		update.getInterfaceDesctription();
	}

}
