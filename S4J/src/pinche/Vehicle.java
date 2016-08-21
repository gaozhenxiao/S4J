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
@WebServlet("/Vehicle")
public class Vehicle extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Vehicle() {
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
			out.println("license");
			out.println("type");
			out.println("seats");
			out.println("company2homeTime");
			out.println("color");
			out.close();
			return;
		}
		Object callback = null;
		String uid = request.getParameter("uid");
		String license=request.getParameter("license");
		String type=request.getParameter("type");
		String seats=request.getParameter("seats");
		String color=request.getParameter("color");

		if( action.equalsIgnoreCase("list") )
		{
			if( uid!=null && uid.length()>0 )
			{
				callback =  listUserVehicles(uid);
			}
		}
		else if( action.equalsIgnoreCase("register") )
		{
			if( uid!=null && uid.length()>0 && license!=null && license.length()>0 )
			{
				callback =  register(uid, license, type, seats, color);
			}
		}
		else if( action.equalsIgnoreCase("update") )
		{
			if( uid!=null && uid.length()>0 && license!=null && license.length()>0 )
			{
				callback =  update(uid, license, type, seats, color);
			}
		}
		response.setContentType("application/x-json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.println( callback );
		out.close();
	}
	public Object update(String uid, String license, String type, String seats, String color)
	{
		String sql = "update vehicle set "+ SQLStringUtility.sqlEntryValuePair("license", license) 
		+ SQLStringUtility.wrapSemicomma(  SQLStringUtility.sqlEntryValuePair("owner", uid))
		+ SQLStringUtility.wrapSemicomma(  SQLStringUtility.sqlEntryValuePair("type", type))
		+ SQLStringUtility.wrapSemicomma(  SQLStringUtility.sqlEntryValuePair("seats", seats))
		+ SQLStringUtility.wrapSemicomma(  SQLStringUtility.sqlEntryValuePair("color", color)) 
		+ " where "+ SQLStringUtility.sqlEntryValuePair("license", license);
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
	public Object register(String uid, String license, String type, String seats, String color)
	{
		String sql = "insert into vehicle set "+ SQLStringUtility.sqlEntryValuePair("license", license) 
		+ SQLStringUtility.wrapSemicomma(  SQLStringUtility.sqlEntryValuePair("owner", uid))
		+ SQLStringUtility.wrapSemicomma(  SQLStringUtility.sqlEntryValuePair("type", type))
		+ SQLStringUtility.wrapSemicomma(  SQLStringUtility.sqlEntryValuePair("seats", seats))
		+ SQLStringUtility.wrapSemicomma(  SQLStringUtility.sqlEntryValuePair("color", color)); 
//		 + " where " + SQLStringUtility.sqlEntryValuePair("license", license);
		System.out.println(sql);
		MyDatabase database = new MyDatabase();
		try {
			database.getStatementExecute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			return e.getMessage();
		}
		database.close();
		return "true";
	}


	private Object listUserVehicles(String uid) {
		String sql = "SELECT vehicle.* from vehicle  where "+ SQLStringUtility.sqlEntryValuePair("owner", uid) ;
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
		Vehicle vehicle = new Vehicle();
		System.out.println( vehicle.register("00", "0000", "Benz", "4", "red") );
		//		System.out.println( setting.updateUserSettings("00","Äà³Ç","ÕÅ½­","2016-00-00 09:00:00", "2016-00-00 17:00:00","4","15",null,null));
	}
}
