package pinche;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.RandomStringUtils;

import com.mysql.jdbc.log.Log;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
@WebServlet("/Login")
public class Login extends HttpServlet 
{
	private static final long serialVersionUID = 2L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 * http://localhost:8080/S4J/Trip?action=SearchCar&departureLocation=longyanglu&arrivalLocation=nicheng
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		String requestURI = request.getRequestURI();  
		String method = request.getMethod();  
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");  
		out.println("<HTML>");  
		out.println("  <HEAD><TITLE>Login Servlet</TITLE></HEAD>");  
		out.println("  <BODY>");  

		Set<String> set  = request.getParameterMap().keySet();
		for (String key : set) 
		{  
			String param = request.getParameter( key );
			out.println("   �� " + method + " ��ʽ���ʸ�ҳ�档ȡ���� param ����Ϊ��" + param + "<br/>");  
		}  

		out.println("   <form action='" + requestURI + "' method='get'><input type='text' name='param' value=''><input type='submit' value='�� GET ��ʽLogin'></form>");  
		out.println("   <form action='" + requestURI + "' method='post'>IMEI:<input type='text' name='imei' value=''>"
				+ "<input type='hidden' name='action' value='login'>"
				+ "<input type='submit' value='�� POST ��ʽLogin'></form>");  
		out.println("   <form action='" + requestURI + "' method='post'>IMEI:<input type='text' name='imei' value=''>"
				+ "<input type='hidden' name='action' value='register'>"
				+ "<input type='submit' value='ע��'></form>");  
		// �ɿͻ����������ȡ���ĵ��ĸ���ʱ��  
		out.println("   <script>document.write('��ҳ��������ʱ�䣺' + document.lastModified + '<br />'); </script>");  
		out.println("   <script>document.write('��ҳ��URL��' + location + '<br/>' ); </script>");  
		out.println("  </BODY>");  
		out.println("</HTML>");  
		out.flush();  
		out.close();  
		out.close();
	}
	/**
	 * ʹ�÷�����
	 * ʹ��httppost��½��ʾ�����룺
	 * try
		{
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("action", "register" ) );
			formparams.add(new BasicNameValuePair("imei", "1234567789012345" ) );
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF8");

			String url = "152r7468s2.imwork.net:25282/S4J/Login";
			System.out.println("������ַ��HttpPost "+url);
			HttpPost httppost = new HttpPost( url );
			httppost.setEntity(entity);
			httppost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.72 Safari/537.36");

			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient.execute(httppost);
			String responseString = EntityUtils.toString(response.getEntity());

			httppost.releaseConnection();
			return responseString;
		}
		catch( UnsupportedEncodingException | 	ClientProtocolException  e)
		{
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		����action����ָ����½��صĶ������ͣ�
		����register��login��update
		action=register��ע�ᣬ��post�����г�����Ҫָ��action�����⣬
		Aʹ���ֻ��ź����롢imeiע���û�����������username,password��imei��ע�⣬password���ʹ��MD5���ܣ�
		ע���֮�󣬷���String true/false��ʾ�Ƿ�ɹ�
		action=login����½��3�з�����A imei��½��B phone��password��imei��½��
		ֻ��Ҫ��Ӷ�Ӧ�Ĳ������ɡ�������ͬʱ����ʱ�����ȼ�A>B��B��½��ʱ����Ҫ����imei�豸��Ϣ
		����JSON��ʽ���û���Ϣ������uidΨһ����
		action=update�������û���Ϣ���û��״ε�½���û�ϣ������û������ֻ��š����롢�Ա��������Ϣ
		��Ҫ�Ĳ����У�uid, phone, birthday, sex, username , password, email
		����isDriverΪ0����1��1��ʾ˾��
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		request.setCharacterEncoding("UTF-8");

		String action = request.getParameter("action");
		if( action==null || action.length()==0)
		{
			response.setContentType("application/x-json");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			out.println("Parameter Action Required!");
			out.close();
		}
		String userName = request.getParameter("username");
		String password = request.getParameter("password");
		String newPassword=request.getParameter("newpassword");
		String imei = request.getParameter("imei");
		String phone = request.getParameter("phone");
		String birthday = request.getParameter("birthday");
		String sex = request.getParameter("sex");
		String uid = request.getParameter("uid");
		String email = request.getParameter("email");
		if( action.equalsIgnoreCase("login") )
		{
			String userInfo ="";

			if( imei !=null && imei.length()>0 && uid !=null && uid.length()>0 )
			{
				userInfo = loginByImeiUid(imei, uid);
			}
			else if (   phone !=null && phone.length()>0   )
			{
				userInfo = loginByPhonePassword(phone, password, imei);
			}
			response.setContentType("application/x-json");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			out.println(userInfo);
			out.close();
		}
		else if( action.equalsIgnoreCase("register"))
		{
			if (  phone!=null && phone.length()>0 && imei!=null && imei.length()>0 )
			{
				String user = registerByPhonePassword(phone, password, imei);
				response.setContentType("application/x-json");
				response.setCharacterEncoding("UTF-8");
				PrintWriter out = response.getWriter();
				out.println(user);
				out.close();
			}
		}
		else if( action.equalsIgnoreCase("update"))
		{
			boolean successSign =false;
			if( uid!=null && uid.length()>0 )
				successSign=updateUserInfoByUid(uid, phone, birthday, sex, userName, email, imei);
			response.setContentType("application/x-json");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			out.println(successSign);
			out.close();
		}
		else if( action.equalsIgnoreCase("changepassword"))
		{
			boolean successSign =false;
			if( uid!=null && uid.length()>0 )
				successSign=updateUserPassword(uid, password, newPassword);
			response.setContentType("application/x-json");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			out.println(successSign);
			out.close();
		}
		//		response.sendRedirect("Login");
	}

	public String loginByImeiUid(String imei, String uid) {
		String sql =  "select uid, phone,birthday,sex, userName, email  from user where uid = '" + uid +"' and imei='" + imei +"'";//SQL���  
		String userInfo= getJSONResult(sql);
		
//		sql = "SELECT uu.uid, uu.userName,uu.phone FROM user uu, contact c, user u where c.uid_friend=uu.uid  and c.uid=u.uid and " 
//				+  SQLStringUtility.sqlEntryValuePair("u.imei", imei)+ " and "+  SQLStringUtility.sqlEntryValuePair("u.uid", uid);
//		String contact=new MyDatabase(sql).getJSONResult();
//
//		sql = "select uh.departureLocation, uh.arrivalLocation"
//				+ "from user_habit uh, user u, user uu"
//				+ "where u.uid=uh.uid and uu.uid=u.uid and " + SQLStringUtility.sqlEntryValuePair("uu.imei", imei)+" and " + SQLStringUtility.sqlEntryValuePair("uu.uid", uid);
//		String locations= new MyDatabase(sql).getJSONResult();
//
//		JSONObject obj =new JSONObject();
//		obj.put("userInfo", userInfo);
//		obj.put("contact", contact);
//		obj.put("locations", locations);
//
//		return obj.toString();
		return userInfo;
	}
	private String loginByPhonePassword(String phone, String password, String imei)
	{
		String sql =  "select uid, phone,birthday,sex, userName, email  from user where phone = '" + phone +"' and password='" + password +"'";//SQL���  
		String userInfo= getJSONResult(sql);
		sql = "update user set imei = " + SQLStringUtility.sqlString4IfNull(imei) + " where phone=" + SQLStringUtility.sqlString4IfNull(phone) + " and password = "+ SQLStringUtility.sqlString4IfNull(password) ;
		System.out.println( sql );
		MyDatabase db1 = new MyDatabase(sql);//����DBHelper����  
		try {
			db1.getPreparedStatementExecute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db1.close();  
//		Contact contact = new Contact();
//		String contactInfo=contact.listContacts(phone, password).toString();
//
//		sql = "select uh.departureLocation, uh.arrivalLocation"
//				+ " from user_habit uh, user u, user uu"
//				+ " where u.uid=uh.uid and uu.uid=u.uid and " + SQLStringUtility.sqlEntryValuePair("uu.phone", phone)+" and " + SQLStringUtility.sqlEntryValuePair("uu.password", password);
//		System.out.println(sql);
//		String locations= new MyDatabase(sql).getJSONResult();
//
//		JSONObject obj =new JSONObject();
//		obj.put("userInfo", userInfo);
//		obj.put("contactInfo", contactInfo);
//		obj.put("locations", locations);
//
//		return obj.toString();
		return userInfo;
	}
	//	private String loginByPhonePassword(String phone, String password)
	//	{
	//		String sql =  "select uid,phone,birthday,sex, userName  from user where phone = '" + phone +"' and password='" + password +"'";//SQL���  
	//		String userInfo= getJSONResult(sql);
	//		return userInfo;
	//	}
	//	private String loginByNamePassword(String name, String password)
	//	{
	//		String sql =  "select uid, phone,birthday,sex, userName  from user where userName = '" + name +"' and password='" + password +"'";//SQL���  
	//		String userInfo= getJSONResult(sql);
	//		return userInfo;
	//	}
	private String getJSONResult(String sql)
	{
		String resultString="";
		try {  
			MyDatabase db1 =  new MyDatabase(sql);//����DBHelper����  
			ResultSet ret = db1.getPreparedStatementExecuteQuery();//ִ����䣬�õ������  
			java.sql.ResultSetMetaData m =ret.getMetaData();
			int columns=m.getColumnCount();
			JSONArray array = new JSONArray();
			ArrayList<String> titles = new ArrayList<>();
			for(int i=1;i<=columns;i++)
				titles.add(m.getColumnName(i));
			while (ret.next()) 
			{
				JSONObject obj =new JSONObject();
				String row = "";
				for(int i=1;i<=columns;i++)
				{
					row+=ret.getString(i)+"\t\t";
					obj.put(titles.get(i-1), ret.getString(i));
				}
				System.out.println( row );
				array.element(obj);
			}//��ʾ����  
			ret.close();  
			db1.close();//�ر�����  
			resultString= array.toString() ;
		} catch (SQLException e) {  
			e.printStackTrace();  
		}
		return resultString;
	}

	private String registerByPhonePassword( String phone, String password, String imei )
	{
		if( checkPhone(phone)==true  )
		{
			return "ERROR:�ֻ����Ѿ�ע�ᣡ";
		}
		String uid  = RandomStringUtils.randomAlphanumeric(32);
		while ( checkUid(uid) ==true  )
			uid  = RandomStringUtils.randomAlphanumeric(32);
		insertUser(uid, phone, password, imei);
		String userInfo=loginByPhonePassword(phone, password, imei);
		return userInfo;
	}
	private boolean insertUser(String uid, String phone, String password, String imei)
	{
		try 
		{  
			Wallet wallet = new Wallet();
			String walletID=wallet.createANewWallet();
			{
				String sql = "insert into user set uid ='" + uid +"' , phone='" + phone + "', password = '"+ password +"', imei='"+imei+"', wallet='"+walletID+"'";//SQL���    
				System.out.println( sql );
				MyDatabase db1 = new MyDatabase(sql);//����DBHelper����  
				db1.getPreparedStatementExecute();//ִ����䣬�õ������
				db1.close();//�ر�����  
			}
			{
				String sql = "insert into user_setting set "+SQLStringUtility.sqlEntryValuePair("uid", uid);
				MyDatabase database = new MyDatabase();
				try {
					database.getStatementExecute(sql);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				database.close();
			}
			boolean hasUser = checkUid(uid);
			return hasUser;
		} catch (SQLException e) {  
			e.printStackTrace();  
		}  
		return false;
	}
	//	private boolean insertUser(String uid, String imei)
	//	{
	//		try 
	//		{  
	//			String sql = "insert into user set uid ='" + uid +"' , imei='" + imei + "'";//SQL���    
	//			System.out.println( sql );
	//			MyDatabase db1 = new MyDatabase(sql);//����DBHelper����  
	//			db1.pst.execute();//ִ����䣬�õ������
	//			db1.close();//�ر�����  
	//			boolean hasUser = checkUid(uid);
	//			return hasUser;
	//		} catch (SQLException e) {  
	//			e.printStackTrace();  
	//		}  
	//		return false;
	//	}
	private boolean updateUserInfoByUid(String uid, String phone, String birthday, String sex,  String userName, String email, String imei)
	{
		if( uid==null || uid.length()==0)
			return false;
		if( phone==null && birthday==null && sex==null && userName==null && email==null && imei==null)
			return false;
		try 
		{
			//			String sql = "update pinche.user set phone='" + phone + "', birthday='" + birthday +"', sex='" + sex 
			//					+"', userName='" +userName + "', password='"+ password +"', email=" + Trip.sqlString4IfNull(email) + " where uid ='" + uid +"'";//SQL���
			String sql = "update pinche.user set "+ SQLStringUtility.sqlEntryValuePair("phone", phone)
			+ SQLStringUtility.wrapSemicomma(SQLStringUtility.sqlEntryValuePair("birthday", birthday)) 
			+  SQLStringUtility.wrapSemicomma(SQLStringUtility.sqlEntryValuePair("sex", sex))  
			+  SQLStringUtility.wrapSemicomma(SQLStringUtility.sqlEntryValuePair("userName", userName))   
			+	 SQLStringUtility.wrapSemicomma(SQLStringUtility.sqlEntryValuePair("email", email)) 
			+ SQLStringUtility.wrapSemicomma(SQLStringUtility.sqlEntryValuePair("imei", imei)) 
			+ " where " + SQLStringUtility.sqlEntryValuePair("uid", uid);
			System.out.println( sql );
			MyDatabase db1 = new MyDatabase(sql);//����DBHelper����  
			db1.getPreparedStatementExecute();//ִ����䣬�õ������
			db1.close();//�ر�����  
			return true;
		} catch (SQLException e) {  
			e.printStackTrace();
		}  
		return false;
	}
	private boolean updateUserPassword(String uid, String password, String newPassword) {
		if( uid==null || uid.length()==0)
			return false;
		if( newPassword==null || newPassword.length()==0)
			return false;
		try 
		{
			boolean found  = false;
			{
				String sql =  "select 1 from pinche.user where " +SQLStringUtility.sqlEntryValuePair("uid", uid) + " and " + SQLStringUtility.sqlEntryValuePair("password", password);
				System.out.println( sql );
				MyDatabase db1 =  new MyDatabase(sql);//����DBHelper����  
				ResultSet ret = db1.getPreparedStatementExecuteQuery();//ִ����䣬�õ������  
				found  = ret.next();
				ret.close();  
				db1.close();//�ر�����  
				if( found==false )
					return false;
			}
			{
				String sql = "update pinche.user set "+ SQLStringUtility.sqlEntryValuePair("password", newPassword)+
						" where " + SQLStringUtility.sqlEntryValuePair("uid", uid);
				System.out.println( sql );
				MyDatabase db1 = new MyDatabase(sql);//����DBHelper����  
				db1.getPreparedStatementExecute();//ִ����䣬�õ������
				db1.close();//�ر�����  
				return true;
			}
		} catch (SQLException e) {  
			e.printStackTrace();
		}  
		return false;
	}

	/**
	 * �ҵ��Ļ�����true
	 * @param phone
	 * @return
	 */
	private boolean checkPhone(String phone)
	{
		try {  
			String sql =  "select 1 from pinche.user where phone = '" + phone +"' limit 1;";//SQL���  
			MyDatabase db1 =  new MyDatabase(sql);//����DBHelper����  
			//			java.sql.ResultSetMetaData m=null;
			//			m=ret.getMetaData();
			ResultSet ret = db1.getPreparedStatementExecuteQuery();//ִ����䣬�õ������  
			boolean found  = ret.next();
			ret.close();  
			db1.close();//�ر�����  
			return found;
		} catch (SQLException e) {  
			e.printStackTrace();  
		}  
		return false;
	}

	private boolean checkUid(String uid)
	{
		try {  
			String sql =  "select 1 from pinche.user where uid = '" + uid +"' limit 1;";//SQL���  
			MyDatabase db1 =  new MyDatabase(sql);//����DBHelper����  
			//			java.sql.ResultSetMetaData m=null;
			//			m=ret.getMetaData();
			ResultSet ret = db1.getPreparedStatementExecuteQuery();//ִ����䣬�õ������  
			boolean found  = ret.next();
			ret.close();  
			db1.close();//�ر�����  
			return found;
		} catch (SQLException e) {  
			e.printStackTrace();  
		}  
		return false;
	}
	public static void main(String[]  args)
	{
		//		String uid  = RandomStringUtils.randomAlphanumeric(32);
		//		System.out.println( uid );
		Login myLogin = new Login();
		//		System.out.println(  myLogin.checkUid("11") );
		//		System.out.println(  "register:" );
		//		String imei = RandomStringUtils.randomAlphanumeric(32);
		//		myLogin.updateUserInfoByUid("5e4p6i4lt8WCuoKBSspfe7CDPGZxlSkx", "13822221111", "20000101", null, "test user",  "test@A.com", null);
		//		myLogin.updateUserPassword("00", "1", "newPassword");
		//		myLogin.registerByPhonePassword("testPhone", "testPass", "testIMEI");
		//		System.out.println( myLogin.login("11111111111111")  );
		System.out.println( myLogin.loginByPhonePassword("18521511571","newPassword", "11")  );
		//		System.out.println( Login.sqlBlankIfNull("password", "")    );
	}
}
