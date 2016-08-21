package pinche;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Contact
 * 示例访问方法：
 * http://host/S4J/Contact?action=GetUserInfo&uid=uid1
 * action:
 * 1、ListContacts，需要参数uid，返回JSON列表，包含uid的所有联系人（该联系人是系统通过历史乘坐记录生成的）
 * 2、GetUserInfo，需要参数uid，返回JSON列表，包含uid这个人的基本信息
 */
@WebServlet("/Contact")
public class Contact extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Contact() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		if( action==null || action.length()==0)
		{
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			out.println("Parameter Action Required!");
			out.println("Parameters acceptable: ");
			out.println("action");
			out.println("uid");
			out.close();
			return ;
		}
		Object callback = null;
		if( action.equalsIgnoreCase("ListContacts") )
		{
			String uid = request.getParameter("uid");
			if( uid!=null && uid.length()>0 )
			{
				callback = listContacts(uid);
			}
		}
		else if( action.equalsIgnoreCase("GetUserInfo") )
		{
			String uid = request.getParameter("uid");
			if( uid!=null && uid.length()>0 )
			{
				callback = getUserInfo(uid);
			}
		}
		response.setContentType("application/x-json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.println( callback );
		out.close();
	}

	private Object getUserInfo(String uid) {
		String sql =  "select uid, phone,birthday,sex, userName , email from user where "+ SQLStringUtility.sqlEntryValuePair("uid", uid);
		MyDatabase db1 =  new MyDatabase(sql);
		return db1.getJSONResult();
	}

	public Object listContacts(String uid) {
		String json="";
		String sql = "SELECT u.uid, u.userName,u.phone, u.birthday, u.sex, u.email FROM contact c, user u where c.uid_friend=u.uid and " +  SQLStringUtility.sqlEntryValuePair("c.uid", uid);
		System.out.println( sql );
		MyDatabase db1 =  new MyDatabase(sql);
		json = db1.getJSONResult();
		return json;
	}
	public Object listContacts(String phone, String password) {
		String json="";
		String sql = "SELECT uu.uid, uu.userName,uu.phone FROM user uu, contact c, user u where c.uid_friend=uu.uid  and c.uid=u.uid and " 
				+  SQLStringUtility.sqlEntryValuePair("u.phone", phone)+ " and "+  SQLStringUtility.sqlEntryValuePair("u.phone", phone);
		System.out.println( sql );
		MyDatabase db1 =  new MyDatabase(sql);
		json = db1.getJSONResult();
		return json;
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	public static void main(String[]  args)
	{
		Contact contact = new Contact();
//		System.out.println(contact.listContacts("00"));
//		System.out.println(contact.getUserInfo("00"));
		System.out.println(contact.listContacts("18521511571", "newPassword"));
	}
}
