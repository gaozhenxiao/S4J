package pinche;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.RandomStringUtils;

/**
 * Servlet implementation class Password
 * 流程：
 * http://localhost:8080/S4J/Password?action=reset&phone=18521511571
 * Get方法访问，3个参数
 * action：动作类型，reset
 * 前者为重置密码，需要唯一参数 手机号
 * 如果手机号存在而且email存在，那么发送到email中一封重置密码的邮件
 * 用户点击后，在网页端继续访问本网址，本servlet自动生成4位数字的密码，并且通过md5加密后存储到数据库
 * 加密代码：
 * import java.security.MessageDigest;
public class Md5Encrypt 
{
	public String Encrypt(String strSrc) {
		MessageDigest md = null;
		Md5Encrypt mept = new Md5Encrypt();
		String strDes = null;
		byte[] bt = strSrc.getBytes();
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(bt);
			strDes = mept.bytes2Hex(md.digest()); //to HexString
		} catch (Exception e) {
			System.out.println("Invalid algorithm.\n" + e.getMessage());
			return null;
		}
		return strDes;
	}

	private String bytes2Hex(byte[] bts) {
		String des = "";
		String tmp = null;

		for (int i = 0; i < bts.length; i++) {
			tmp = (Integer.toHexString(bts[i] & 0xFF));
			if (tmp.length() == 1) {
				des += "0";
			}
			des += tmp;
		}
		return des;
	}
	public static void main(String[]  args)
	{
		Md5Encrypt md = new Md5Encrypt();
		String password = md.Encrypt("this is a password before encryption");
		System.out.println( password );

	}
} 
	用户在客户端就可以直接使用新生成的随机密码登陆了，登陆后修改新密码
 */
@WebServlet("/Password")
public class Password extends HttpServlet {
	private static final long serialVersionUID = 5L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Password() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		//		response.getWriter().append("Served at: ").append(request.getContextPath());
		//		sentResetEmail(request, response);
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		String action = request.getParameter("action");
		if( action==null || action.length()==0)
		{
			//			response.setContentType("application/x-json");
			PrintWriter out = response.getWriter();
			out.println("Parameter Action Required!");
			out.println("需要参数");
			out.close();
			return;
		}
		if( action.equalsIgnoreCase("reset") )
		{
			sentResetEmail(request, response);
		}
		else if( action.equalsIgnoreCase("checklink") )
		{
			checkResetLink(request, response);
		}

	}

	private void sentResetEmail(HttpServletRequest request, HttpServletResponse response) throws IOException 
	{
		String phone = request.getParameter("phone");
		String emailAdress  = "";
		{
			String sql = "select email from user where phone = "+ SQLStringUtility.sqlString4IfNull(phone);
			try {
				MyDatabase database =  new MyDatabase(sql);
				ResultSet ret = database.getPreparedStatementExecuteQuery();
				boolean found  = ret.next();
				if( found )
				{
					emailAdress = ret.getString(1);
					if( emailAdress==null || emailAdress.length()==0 )
					{
						response.getWriter().append("Error: Email has not been registered:"+phone);
						return;
					}
				}
				else
				{
					response.getWriter().append("Error: User does not exist:"+phone);
					return;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		String privateKey = UUID.randomUUID().toString(); // 密钥
		Timestamp outDate = new Timestamp(System.currentTimeMillis() + 30 * 60 * 1000);// 30分钟后过期
		long date = outDate.getTime() / 1000 * 1000;// 忽略毫秒数  mySql 取出时间是忽略毫秒数的
		System.out.println("   phone>>>> "+phone  );
		String key =phone + "$" + date + "$" + privateKey;
		System.out.println(" key>>>"+key);
		Md5Encrypt md = new Md5Encrypt();
		String valiCode = md.Encrypt(key);// 数字签名

		String sql="update user set expirationTime="+SQLStringUtility.sqlString4IfNull(outDate.toString())+", privateKey = " + SQLStringUtility.sqlString4IfNull(privateKey) +",validataCode=" 
				+ SQLStringUtility.sqlString4IfNull(valiCode)  +" where phone=" + SQLStringUtility.sqlString4IfNull(phone) +";";
		MyDatabase database =  new MyDatabase(sql);
		try {
			database.getPreparedStatementExecute();

			String path =request.getContextPath();
			String basePath = request.getScheme() + "://"
					+ request.getServerName() + ":"
					+ request.getServerPort() + path + "/";
			String resetPassHref = basePath + "Password?action=checklink&valiCode="+ valiCode +"&phone="+phone;
			String emailContent = "请勿回复本邮件.点击下面的链接,重设密码<br/>"
					+ "<a href="+ resetPassHref + " target='_BLANK'>" + resetPassHref
					+ "</a> <br/>tips:本邮件超过30分钟,链接将会失效，需要重新申请'找回密码'" + key
					+ "\t" + valiCode;

			EmailClient email = new EmailClient();
			email.sentEmail(emailContent, "找回您的账户密码", emailAdress);
			response.getWriter().append("重置密码邮件已经发送，请登陆邮箱进行重置："+ emailAdress);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String checkResetLink(HttpServletRequest request, HttpServletResponse response) throws IOException 
	{
		String phone = request.getParameter("phone");
		String valiCode = request.getParameter("valiCode");
		System.out.println("valiCode>>>" + valiCode);
		if (valiCode==null||phone==null ||valiCode.equals("")  || phone.equals("")) 
		{
			response.getWriter().append("ERROR:链接不完整,请重新生成");
			System.out.println(">>>>> null");
			return "error";
		}

		String code="";
		try {  
			String sql =  "select validataCode, expirationTime from user where phone =" + SQLStringUtility.sqlString4IfNull(phone) +" limit 1;";//SQL语句  
			//			MyDatabase db1 =  new MyDatabase();
			//			ResultSet ret = db1.executQuery(sql);
			MyDatabase db1 =  new MyDatabase(sql);
			ResultSet ret = db1.getPreparedStatementExecuteQuery();

			boolean found  = ret.next();
			if( found  )
			{
				code = ret.getString(1);
				long timeStamp =ret.getTimestamp(2).getTime(); 
				ret.close();  
				db1.close();//关闭连接

				if(  timeStamp<=System.currentTimeMillis() )
				{
					response.getWriter().append("链接已经过期,请重新申请找回密码.");
					System.out.println("时间 超时");
					return "error";
				}
				else
				{
					System.out.println("validataCode>>>>"+code);
					if( valiCode.equalsIgnoreCase(code)  )
					{
						String password  = RandomStringUtils.randomNumeric(4);
						response.getWriter().append("新密码如下，请登陆后即刻修改：" + password );
						Md5Encrypt md5 = new Md5Encrypt();
						String encryptedPassword=md5.Encrypt(password);
						sql = "update user set password ="+ SQLStringUtility.sqlString4IfNull(encryptedPassword) + " where phone="+SQLStringUtility.sqlString4IfNull(phone);
						MyDatabase db2 =  new MyDatabase(sql);
						db2.getPreparedStatementExecute();
						db2.close();  
					}
					else
					{
						response.getWriter().append("ERROR:验证码不正确");
						System.out.println("验证码不正确");
					}
				}
			}
			else
			{
				response.getWriter().append("ERROR:链接错误,无法找到匹配用户,请重新申请找回密码");
				System.out.println("用户不存在");
				ret.close();  
				db1.close();//关闭连接
				return "error";
			}

		} catch (SQLException e) {  
			e.printStackTrace();  
		}  
		return "OK";
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		doGet(request, response);
	}

}
