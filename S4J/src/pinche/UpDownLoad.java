package pinche;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Servlet implementation class UploadServlet
 * 接口规范：
 * 本Servlet处理2类任务，
 * 
 * http://localhost/S4J/UpDownLoad?phone=18521511571&fileName=00.jpg
 * 
 */
@WebServlet("/UpDownLoad")
public class UpDownLoad extends HttpServlet {

	private static final long serialVersionUID = 1L;  
	private ServletFileUpload upload;  
	private final long MAXSize = 4194304*2L;//4*2MB  
	private String filedir=null;  

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UpDownLoad() {
		super();
		// TODO Auto-generated constructor stub
	}
	/** 
	 * 设置文件上传的初始化信息 
	 * @see Servlet#init(ServletConfig) 
	 */  
	public void init(ServletConfig config) throws ServletException {  
		FileItemFactory factory = new DiskFileItemFactory();// Create a factory for disk-based file items  
		this.upload = new ServletFileUpload(factory);// Create a new file upload handler  
		this.upload.setSizeMax(this.MAXSize);// Set overall request size constraint 4194304  
		String osName = System.getProperties().getProperty("os.name");
		System.out.println("OS name:" + osName);
		System.out.println("OS name in lower case:" + osName.toLowerCase());
		if( osName.toLowerCase().contains("win")  )
			filedir=config.getServletContext().getRealPath("/")+"../../server_images";  
		else if( osName.toLowerCase().contains("linux") )
//			filedir="/usr/server_images";  
			filedir=config.getServletContext().getRealPath("/")+"../../server_images";
		System.out.println("filedir="+filedir);  
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 * http://localhost/S4J/UpDownLoad?phone=18521511571&fileName=00.jpg
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String phone=request.getParameter("phone");
		String fileName = request.getParameter("fileName");
		String action = request.getParameter("action");
		boolean backup=false;
		if( action!=null && action.equalsIgnoreCase("backup"))
			backup=true;
		boolean isUser=verify(phone,fileName);
		if(isUser || backup )
		{
			String fullFilePath = filedir +File.separator+ fileName;
			/*读取文件*/
			File file = new File(fullFilePath);
			//      System.out.println("file="+file.getAbsolutePath());  
			/*如果文件存在*/
			if (file.exists()) {
				String filename = URLEncoder.encode(file.getName(), "utf-8");
				response.reset();
//				response.setContentType("application/x-msdownload");
				response.setContentType("text/html");
				response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
				int fileLength = (int) file.length();
				response.setContentLength(fileLength);
				/*如果文件长度大于0*/
				if (fileLength != 0) {
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
				}
			}
			try {
				PincheWebsocket.sent2BackupServer( "image:"+fileName );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else
		{
			response.setContentType("application/x-json");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			out.println("Error:Invalid User!");
			out.close();
		}
	}

	private boolean verify(String phone, String fileName)
	{
		if(phone==null || phone.length()==0 || fileName==null ||fileName.length()==0 )
			return false;
		if(fileName.contains("."))
			fileName=fileName.substring(0, fileName.indexOf("."));
		String uid = fileName;
		boolean found  = false;
		try
		{
			String sql =  "select 1 from pinche.user where " +SQLStringUtility.sqlEntryValuePair("phone", phone) + " and " + SQLStringUtility.sqlEntryValuePair("uid", uid);
			System.out.println( sql );
			MyDatabase db1 =  new MyDatabase(sql);//创建DBHelper对象  
			ResultSet ret = db1.getPreparedStatementExecuteQuery();//执行语句，得到结果集  
			found  = ret.next();
			ret.close();  
			db1.close();//关闭连接  
			return found;
		}
		catch (SQLException e) 
		{  
			e.printStackTrace();
		}  
		return false;
	}
	private boolean verify( String fileName)
	{
		if( fileName==null ||fileName.length()==0 )
			return false;
		if(fileName.contains("."))
			fileName=fileName.substring(0, fileName.indexOf("."));
		String uid = fileName;
		boolean found  = false;
		try
		{
			String sql =  "select 1 from pinche.user where " + SQLStringUtility.sqlEntryValuePair("uid", uid);
			System.out.println( sql );
			MyDatabase db1 =  new MyDatabase(sql);//创建DBHelper对象  
			ResultSet ret = db1.getPreparedStatementExecuteQuery();//执行语句，得到结果集  
			found  = ret.next();
			ret.close();  
			db1.close();//关闭连接  
			return found;
		}
		catch (SQLException e) 
		{  
			e.printStackTrace();
		}  
		return false;
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out=response.getWriter();  
		try {
			List<FileItem> items = this.upload.parseRequest(request);  
			if(items!=null  && !items.isEmpty())
			{  
				for (FileItem fileItem : items) 
				{  
					if( fileItem!=null && fileItem.getName()!=null  )
					{
						String filename=fileItem.getName();  
						boolean isUser=verify(filename);
						if(isUser)
						{
							String filepath=filedir+File.separator+filename;  
							System.out.println("文件保存路径为:"+filepath);  
							File file=new File(filepath);  
							InputStream inputSteam=fileItem.getInputStream();  
							BufferedInputStream fis=new BufferedInputStream(inputSteam);  
							FileOutputStream fos=new FileOutputStream(file);  
							int f;  
							while((f=fis.read())!=-1)  
							{  
								fos.write(f);  
							}  
							fos.flush();  
							fos.close();  
							fis.close();  
							inputSteam.close();  
							System.out.println("文件："+filename+"上传成功!");  
						}
						else
						{
							response.setContentType("application/x-json");
							response.setCharacterEncoding("UTF-8");
							out.println("Error:Invalid User!");
							out.close();
						}
					}
				}  
			}  
			System.out.println("上传文件成功!");  
			out.write("上传文件成功!");  
		} catch (FileUploadException e) {  
			e.printStackTrace();  
			out.write("上传文件失败:"+e.getMessage());  
		}  
	}
	public static void main(String[] args) throws Exception 
	{
		System.out.println("===========os.name:"+System.getProperties().getProperty("os.name"));  
		System.out.println("===========file.separator:"+System.getProperties().getProperty("file.separator"));  
		System.out.println(System.getProperty("user.dir"));//user.dir指定了当前的路径 
		File directory = new File("");//设定为当前文件夹 
		try{ 
			System.out.println(directory.getCanonicalPath());//获取标准的路径 
			System.out.println(directory.getAbsolutePath());//获取绝对路径 
		}catch(Exception e){} 

		File file = new File("../test.jpg");
		System.out.println(file.getAbsolutePath());
		System.out.println(file.exists());

		System.out.println("windows 7".contains("win"));
	}
}
