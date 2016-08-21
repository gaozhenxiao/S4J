package pinche;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
/**这个@符号可以自动添加本Servlet到web.xml中
 */
@WebServlet("/JSONServlet")
public class JSONServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public JSONServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("application/x-json");
		//		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		//		response.getWriter().append("Served at: ").append(request.getContextPath());
		PrintWriter out = response.getWriter();
		//		test1(out);
		test2(out);
		out.close();
	}

	private void test3(PrintWriter out)
	{
		int columns = 5;
		JSONArray array = new JSONArray();
		ArrayList<String> titles = new ArrayList<>();
		for(int i=1;i<=columns;i++)
			titles.add( "title"+columns);

		for( int i=0;i<10;i++)
		{
			JSONObject obj =new JSONObject();
			String row = "row";
			for(int j=1;j<=columns;j++)
			{
				row = "row";
				row+=(int) (Math.random()*100);
				obj.put(titles.get( j-1), row );
			}
			array.element(obj);
		}
		out.println(array.toString());
	}
	
	private void test2(PrintWriter out)
	{
		String sql = null;  
		MyDatabase db1 = null;  
		ResultSet ret = null;  
		java.sql.ResultSetMetaData m=null;
		sql = "select uid, phone from user";//SQL语句  
		db1 = new MyDatabase(sql);//创建DBHelper对象  

		try {  
			ret = db1.getPreparedStatementExecuteQuery();//执行语句，得到结果集  
			m=ret.getMetaData();
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
			}//显示数据  
			ret.close();  
			db1.close();//关闭连接  

			out.println(array.toString());
		} catch (SQLException e) {  
			e.printStackTrace();  
		}  

	}

	private void test1(PrintWriter out) {
//		out.println("Hello, Brave new World!");

		JSONArray array = new JSONArray();
		for( int no=0; no<10;no++)
		{
			JSONObject obj ;
			obj=new JSONObject();
			String[] monthNames =new String[]{"ID","Time", "location", "destination"};
			for(int i=0; i<monthNames.length; i++)
			{
				int num = (int) (Math.random()*100);
				obj.put(monthNames[i], num);
			}
			array.element(obj);
		}
		out.println(array.toString());
		System.out.println(array.toString());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		doGet(request, response);
	}
	
}
