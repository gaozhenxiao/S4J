package pinche;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class UserSetting
 */
@WebServlet("/UserSetting")
public class UserSetting extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UserSetting() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//		response.getWriter().append("Served at: ").append(request.getContextPath());
		//		Set<String> set  = request.getParameterMap().keySet();
		request.setCharacterEncoding("UTF-8");
		String action = request.getParameter("action");
		if( action==null || action.length()==0)
		{
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			out.println("Parameter Action Required!");
			out.println("Parameters acceptable: ");
			out.println("action");
			out.println("home");
			out.println("company");
			out.println("home2companyTime");
			out.println("company2homeTime");
			out.println("vacantSeats");
			out.println("fee");
			out.println("note1");
			out.println("note2");
			out.close();
			return;
		}
		Object callback = null;
		String uid = request.getParameter("uid");
		String home=request.getParameter("home");
		String company=request.getParameter("company");
		String home2companyTime=request.getParameter("home2companyTime");
		String company2homeTime=request.getParameter("company2homeTime");
		String vacantSeats = request.getParameter("vacantSeats");
		String fee = request.getParameter("fee");
		String note1 = request.getParameter("note1");
		String note2 = request.getParameter("note2");
		
		if( action.equalsIgnoreCase("list") )
		{
			if( uid!=null && uid.length()>0 )
			{
				callback =  listUserSettings(uid);
			}
		}
		else if( action.equalsIgnoreCase("update") )
		{
			if( uid!=null && uid.length()>0 )
			{
				callback =  updateUserSettings(uid, home, company, home2companyTime, company2homeTime, vacantSeats, fee, note1,note2);
			}
		}

		response.setContentType("application/x-json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.println( callback );
		out.close();
	}

	private Object updateUserSettings(String uid, String home, String company, String home2companyTime,
			String company2homeTime, String vacantSeats, String fee, String note1, String note2)
	{
		String sql = "update user_setting set "+ SQLStringUtility.sqlEntryValuePair("uid", uid) 
				+ SQLStringUtility.wrapSemicomma(  SQLStringUtility.sqlEntryValuePair("home", home))
				+ SQLStringUtility.wrapSemicomma(  SQLStringUtility.sqlEntryValuePair("company", company))
				+ SQLStringUtility.wrapSemicomma(  SQLStringUtility.sqlEntryValuePair("home2companyTime", home2companyTime))
				+ SQLStringUtility.wrapSemicomma(  SQLStringUtility.sqlEntryValuePair("company2homeTime", company2homeTime))
				+ SQLStringUtility.wrapSemicomma(  SQLStringUtility.sqlEntryValuePair("vacantSeats", vacantSeats))
				+ SQLStringUtility.wrapSemicomma(  SQLStringUtility.sqlEntryValuePair("fee", fee))
				+ SQLStringUtility.wrapSemicomma(  SQLStringUtility.sqlEntryValuePair("note1", note1))
				+ SQLStringUtility.wrapSemicomma(  SQLStringUtility.sqlEntryValuePair("note2", note2))
				+" where "+ SQLStringUtility.sqlEntryValuePair("uid", uid) ;
		System.out.println(sql);
		MyDatabase database = new MyDatabase();
		try {
			database.getStatementExecute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			return e.getMessage();
		}
		database.close();
		return "success";
	}

	private Object listUserSettings(String uid) {
		String sql = "SELECT user_setting.* from user_setting  where "+ SQLStringUtility.sqlEntryValuePair("uid", uid) ;
		MyDatabase database = new MyDatabase(sql);
		return database.getJSONResult();
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
		UserSetting setting = new UserSetting();
		System.out.println( setting.listUserSettings("00"));
		System.out.println( setting.updateUserSettings("00","Äà³Ç","ÕÅ½­","2016-00-00 09:00:00", "2016-00-00 17:00:00","4","15",null,null));
	}
}
