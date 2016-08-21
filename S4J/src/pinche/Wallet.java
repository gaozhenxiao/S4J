package pinche;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.RandomStringUtils;
@WebServlet("/Wallet")
public class Wallet extends HttpServlet 
{
	private static final long serialVersionUID = 3L;

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
		out.println("   <form action='" + requestURI + "' method='post'>uid:<input type='text' name='uid' value=''>"
				+ "<input type='hidden' name='action' value='queryBalance'>"
				+ "<input type='submit' value='查看余额'></form>");  

		out.println("   <form action='" + requestURI + "' method='post'>uid:<input type='text' name='uid' value=''>"
				+ "<input type='hidden' name='action' value='queryHistory'>"
				+ "<input type='submit' value='交易历史'></form>");  

		// 由客户端浏览器读取该文档的更新时间  
		out.println("   <script>document.write('本页面最后更新时间：' + document.lastModified + '<br />'); </script>");  
		out.println("   <script>document.write('本页面URL：' + location + '<br/>' ); </script>");  
		out.println("  </BODY>");  
		out.println("</HTML>");  
		out.flush();  
		out.close();  
		out.close();
	}
	/**
	 * 使用方法：
	 * 使用httppost登陆，示范代码：
	 * try
		{
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("action", register ) );
			formparams.add(new BasicNameValuePair("uid", "1234567789012345" ) );
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF8");

			String url = "152r7468s2.imwork.net:25282/S4J/Wallet";
			System.out.println("请求网址：HttpPost "+url);
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
		其中action用来指定登陆相关的动作类型：
		1、action=queryBalance，查看钱包余额，返回JSON，只有一个数字；入口uid
		2、action=queryHistory，查看钱包交易历史，返回JSON；
		需要参数：String uid, String startDay, String endDay；其中uid为必填字段，后2者可选。startDay和endDay格式相同，为2016-06-26 09:00:00
		3、action=transaction，交易，返回成功与否标志；入口payerUid，beneficiaryUid，transactionTime, password, amount
		注：每个新用户注册的时候默认生成一个新钱包，余额为0
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
		String uid = request.getParameter("uid");
		String payerUid = request.getParameter("payerUid");
		String beneficiaryUid = request.getParameter("beneficiaryUid");
		String transactionTime = request.getParameter("transactionTime");
		String password = request.getParameter("password");
		String amount = request.getParameter("amount");
		String startDay = request.getParameter("startDay");
		String endDay = request.getParameter("endDay");
		
		String isCashFlow = request.getParameter("isCashFlow");

		if( action.equalsIgnoreCase("queryBalance") )
		{
			String info ="";
			if( uid !=null && uid.length()>0 )
			{
				String sql = "SELECT wallet.money FROM wallet,user where user.uid=" + SQLStringUtility.sqlString4IfNull(uid) +" and user.wallet=wallet.walletID";
				MyDatabase database = new MyDatabase(sql);
				info = database.getJSONResult();
			}
			response.setContentType("application/x-json");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			out.println(info);
			out.close();
		}
		else 	if( action.equalsIgnoreCase("queryHistory") )
		{
			String info ="";
			if( uid !=null && uid.length()>0 )
			{
				info = queryHistory(uid, startDay, endDay);
			}
			response.setContentType("application/x-json");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			out.println(info);
			out.close();
		}
		else 	if( action.equalsIgnoreCase("transaction") )
		{
			Object callback;
			
			boolean valid =  verifyUser( beneficiaryUid, password  );
			if (valid==false)
				callback= "Error: 非法用户";
			else
			{
				double amountValue=Double.parseDouble(amount);
				//需要加密机制，单一用户密码的加密程度不够
				if( payerUid !=null && payerUid.length()>0 && beneficiaryUid !=null && beneficiaryUid.length()>0 && transactionTime !=null && transactionTime.length()>0  )
				{
					String id  = RandomStringUtils.randomAlphanumeric(32);
					while ( checkTransactionID(id) ==true  )
						id  = RandomStringUtils.randomAlphanumeric(32);
					boolean cashflow=false;
					if( isCashFlow!=null && isCashFlow.length()>0)
						cashflow=Boolean.parseBoolean(isCashFlow);
					callback = markTransaction(id, payerUid, beneficiaryUid, transactionTime, amountValue, cashflow);
				}
				else
				{
					callback = "Error:交易信息不完整";
				}
			}
			response.setContentType("application/x-json");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			out.println(callback);
			out.close();
		}
	}
	private String queryHistory(String uid, String startDay, String endDay) {
		String info;
		String sql = "select t.time, u.uid, v.uid, t.amount, t.cashflow from `transaction` t, `user` u, `user` v where t.payerWallet=u.wallet and t.beneficiaryWallet=v.wallet "
				+ "and( u.uid="+ SQLStringUtility.sqlString4IfNull(uid) + " or v.uid="+ SQLStringUtility.sqlString4IfNull(uid) + ") ";
		if( startDay!=null && startDay.length()>0 )
			sql+=" and t.time>=" + SQLStringUtility.sqlString4IfNull(startDay);
		if( endDay!=null && endDay.length()>0 )
			sql+=" and t.time<=" + SQLStringUtility.sqlString4IfNull(endDay);
		MyDatabase database = new MyDatabase(sql);
		info = database.getJSONResult();
		return info;
	}
	private boolean verifyUser(String uid, String password) {
		try {  
			String sql =  "select 1  from user where "+ SQLStringUtility.sqlEntryValuePair("uid", uid) + " and "+ SQLStringUtility.sqlEntryValuePair("password", password);
			MyDatabase db1 =  new MyDatabase(sql);//创建DBHelper对象  
			ResultSet ret = db1.getPreparedStatementExecuteQuery();//执行语句，得到结果集  
			boolean user  = ret.next();
			ret.close();  
			db1.close();//关闭连接  
			return user;
		} catch (SQLException e) {  
			e.printStackTrace();  
		}  
		return false;
	}
	private Object markTransaction(String id, String payerUid, String beneficiaryUid, String transactionTime,	double amount, boolean cashflow) 
	{
		String payerWallet = getWalletID(payerUid);
		String beneficiaryWallet=getWalletID(beneficiaryUid);
		if( payerWallet==null || payerWallet.length()==0 || beneficiaryWallet==null || beneficiaryWallet.length()==0 )
			return "Error: 交易信息错误";
		Connection conn =null;
		String sql1 = "";
		try {
			 sql1 =  "insert into transaction set " + SQLStringUtility.sqlEntryValuePair("id", id) +SQLStringUtility.wrapSemicomma(SQLStringUtility.sqlEntryValuePair("payerWallet", payerWallet))  
			+ SQLStringUtility.wrapSemicomma(SQLStringUtility.sqlEntryValuePair("beneficiaryWallet", beneficiaryWallet))  + SQLStringUtility.wrapSemicomma(SQLStringUtility.sqlEntryValuePair("time", transactionTime)) 
			+ ", amount="+amount + ", cashflow="+cashflow;
//			String sql2 = "update wallet "
//					+ "set money=(select m.money from (select money from user u left join wallet w on u.wallet=w.walletID where " + SQLStringUtility.sqlEntryValuePair("uid", payerUid)+" )m  )- "+amount
//							+ " where "+SQLStringUtility.sqlEntryValuePair("walletID", payerWallet);
//			String sql3 = "update wallet "
//					+ "set money=(select m.money from (select money from user u left join wallet w on u.wallet=w.walletID where " + SQLStringUtility.sqlEntryValuePair("uid", beneficiaryUid)+" )m  )+ "+amount
//							+ " where "+SQLStringUtility.sqlEntryValuePair("walletID", beneficiaryWallet);
			System.out.println(sql1);
//			System.out.println(sql2);
//			System.out.println(sql3);
			
			Class.forName(MyDatabase.name);//指定连接类型  
			conn = DriverManager.getConnection( MyDatabase.url, MyDatabase.user, MyDatabase.password);//获取连接  
			conn.setAutoCommit(false);
			Statement stmt = conn.createStatement(); 
			stmt.executeUpdate(sql1);
//			stmt.executeUpdate(sql2);
//			stmt.executeUpdate(sql3);
			conn.commit();
			conn.setAutoCommit(true);
			conn.close();
			stmt.close();
		}
		catch (SQLException | ClassNotFoundException e) 
		{
			try {
				if( conn!=null)
					conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
				return e1.getMessage();
			} 
			e.printStackTrace();
			return e.getMessage();
		}  
		String callback = "transaction success! ";
		
		try {
			PincheWebsocket.sent2BackupServer("sql" + sql1 ); 
			
			String message = " transaction occured: "+ amount +"元 payed to " + beneficiaryUid +" at time "+transactionTime +"from user "+payerUid;
			if( cashflow==true)
				message+=" with real money";
			else
				message+=" by book keeping";
			PincheWebsocket.sentMessage2Uid( message, payerUid);
			PincheWebsocket.sentMessage2Uid(message, beneficiaryUid);
			return  callback + " payer has been informed. ";
		} catch (IOException e) {
			e.printStackTrace();
			return callback + " But payer has not been informed"+e.getMessage();
		}
	}
	public String createANewWallet()
	{
		String id  = RandomStringUtils.randomAlphanumeric(32);
		while ( checkWalletID(id) ==true  )
			id  = RandomStringUtils.randomAlphanumeric(32);
		try {  
			String sql =  "insert into wallet values("+SQLStringUtility.sqlString4IfNull(id)+", 0 )";
			MyDatabase db1 =  new MyDatabase(sql);//创建DBHelper对象  
			db1.getPreparedStatementExecute();//执行语句，得到结果集  
			db1.close();//关闭连接  
			return id;
		} catch (SQLException e) {  
			e.printStackTrace();
			return null;
		}
	}
	private boolean checkWalletID(String id) {
		try {  
			String sql =  "select 1 from pinche.wallet where walletID = '" + id +"' limit 1;";//SQL语句  
			MyDatabase db1 =  new MyDatabase(sql);//创建DBHelper对象  
			ResultSet ret = db1.getPreparedStatementExecuteQuery();//执行语句，得到结果集  
			boolean found  = ret.next();
			ret.close();  
			db1.close();//关闭连接  
			return found;
		} catch (SQLException e) {  
			e.printStackTrace();  
		}  
		return false;
	}
	private String getWalletID(String uid)
	{
		String walletID="";
		String sql =  "select user.wallet from user where user.uid="+SQLStringUtility.sqlString4IfNull(uid);
		MyDatabase db1 =  new MyDatabase(sql);//创建DBHelper对象  
		ResultSet ret;
		try {
			ret = db1.getPreparedStatementExecuteQuery();
			boolean found  = ret.next();
			if( found == true )
			{
				walletID = ret.getString(1);
				ret.close();  
				db1.close();//关闭连接
				return walletID;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}//执行语句，得到结果集  
		return walletID;
	}
	private boolean checkTransactionID(String id) {
		try {  
			String sql =  "select 1 from pinche.transaction where id = '" + id +"' limit 1;";//SQL语句  
			MyDatabase db1 =  new MyDatabase(sql);//创建DBHelper对象  
			//			java.sql.ResultSetMetaData m=null;
			//			m=ret.getMetaData();
			ResultSet ret = db1.getPreparedStatementExecuteQuery();//执行语句，得到结果集  
			boolean found  = ret.next();
			ret.close();  
			db1.close();//关闭连接  
			return found;
		} catch (SQLException e) {  
			e.printStackTrace();  
		}  
		return false;
	}
	public static void main(String[]  args)
	{
		Wallet wallet1=new Wallet();
		String id = RandomStringUtils.randomAlphanumeric(32);
		System.out.println(wallet1.markTransaction(id, "00", "uid1", "2016-7-24 00:00:00", 5, true) );
		System.out.println(wallet1.verifyUser("00", "newPassword") );
		System.out.println(wallet1.queryHistory("00", "2016-7-5 00:00:00", null) );
	}
}
