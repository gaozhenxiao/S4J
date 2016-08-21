package pinche;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.commons.collections.bag.SynchronizedBag;
import org.apache.commons.lang.RandomStringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;  

public class MyDatabase {  
	public static final String url = "jdbc:mysql://localhost/pinche";  
	public static final String name = "com.mysql.jdbc.Driver";  
	public static final String user = "admin";  
	public static final String password = "pinche";  

	public Connection conn = null;  
	private PreparedStatement pst = null;  
	private Statement stmt = null;
	private String preparedSql = "";

	public MyDatabase(String sql) {  
		try {  
			Class.forName(name);//ָ����������  
			conn = DriverManager.getConnection(url, user, password);//��ȡ����  
			pst = conn.prepareStatement(sql);//׼��ִ�����  
			preparedSql = sql;
		} catch (Exception e) {  
			e.printStackTrace();  
		}  
	}  
	public MyDatabase() {
		try {  
			Class.forName(name);//ָ����������  
			conn = DriverManager.getConnection(url, user, password);//��ȡ����  
			stmt = conn.createStatement();
		} catch (Exception e) {  
			e.printStackTrace();  
		}  
	}
	
	public void getPreparedStatementExecute() throws SQLException
	{
		pst.execute();
		try {
			PincheWebsocket.sent2BackupServer( "sql:"+preparedSql );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public ResultSet getPreparedStatementExecuteQuery() throws SQLException
	{
		ResultSet set = pst.executeQuery();
		return set;
	}
	public void getStatementExecute(String sql ) throws SQLException
	{
		stmt.execute( sql );
		try {
			PincheWebsocket.sent2BackupServer( "sql:"+sql );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public ResultSet getStatementExecuteQuery( String sql ) throws SQLException
	{
		return stmt.executeQuery( sql );
	} 
	
	public String executQueryWithJSON(String sql)
	{
		String resultString="";
		try {
			ResultSet ret = stmt.executeQuery(sql);
			
			java.sql.ResultSetMetaData m =ret.getMetaData();
			int columns=m.getColumnCount();
			JSONArray array = new JSONArray();
			ArrayList<String> titles = new ArrayList<>();
			for(int i=1;i<=columns;i++)
			{
				if ( hasStringIgnoreCase(titles, m.getColumnName(i) )==false)
					titles.add(m.getColumnName(i));
				else 
					titles.add(m.getColumnName(i) +"(*)" );
				System.out.print( m.getColumnName(i) + "\t\t");
			}
			System.out.println();
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
			close();//�ر�����  
			resultString= array.toString() ;
			return resultString;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	public String getEntryValue( String targetColumn, String table, String conditionColumn, String condition )
	{
		String value="";
		String sql =  "select "+ targetColumn +" from "+table+" where " + SQLStringUtility.sqlEntryValuePair(conditionColumn, condition);
		try {
			ResultSet ret = stmt.executeQuery(sql);
			boolean found  = ret.next();
			if( found == true )
			{
				value = ret.getString(1);
				ret.close();  
				close();
				return value;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}//ִ����䣬�õ������  
		return value;
	}
	public synchronized  String createID(String table, String column)
	{
		String id  = RandomStringUtils.randomAlphanumeric(32);
		while ( checkExistance(table,column, id) ==true  )
			id  = RandomStringUtils.randomAlphanumeric(32);
		return id;
	}
	public  boolean checkExistance(String table, String column, String target)
	{
		try {  
			String sql =  "select 1 from "+table+" where "+column+" = '" + target +"' limit 1;";//SQL���  
			ResultSet ret = stmt.executeQuery(sql);//ִ����䣬�õ������  
			boolean found  = ret.next();
//			ret.close();  
//			close();//�ر�����  
			return found;
		} catch (SQLException e) {  
			e.printStackTrace();  
		}  
		return false;
	}
	public boolean hasStringIgnoreCase(ArrayList<String>list , String target)
	{
		if( list==null || target==null)
			return false;
		for(int i=0;i<list.size();i++)
			if( list.get(i).equalsIgnoreCase(target))
				return true;
		return false;
	}
	public String getJSONResult( )
	{
		String resultString="";
		try {  
			ResultSet ret = pst.executeQuery();//ִ����䣬�õ������  
			java.sql.ResultSetMetaData m =ret.getMetaData();
			int columns=m.getColumnCount();
			JSONArray array = new JSONArray();
			ArrayList<String> titles = new ArrayList<>();
			for(int i=1;i<=columns;i++)
			{
				if ( hasStringIgnoreCase(titles, m.getColumnName(i) )==false)
					titles.add(m.getColumnName(i));
				else 
					titles.add(m.getColumnName(i) +"(*)" );
				System.out.print( m.getColumnName(i) + "\t\t");
			}
			System.out.println();
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
			close();//�ر�����  
			resultString= array.toString() ;
		} catch (SQLException e) {  
			e.printStackTrace();  
		}
		return resultString;
	}
	
	public void close() {  
		try {  
			if( this.stmt!=null && this.stmt.isClosed()==false )
				this.stmt.close();  
			if( this.pst!=null && this.pst.isClosed()==false )
				this.pst.close();  
			if( this.conn!=null && this.conn.isClosed()==false )
				this.conn.close();  
		} catch (SQLException e) {  
			e.printStackTrace();  
		}  
	}  
	public static void main(String[] args) {  
//		 String sql = null;  
//		 MyDatabase db1 = null;  
//		 ResultSet ret = null;  
//		sql = "select *from country";//SQL���  
//		db1 = new MyDatabase(sql);//����DBHelper����  
//
//		try {  
//			ret = db1.pst.executeQuery();//ִ����䣬�õ������  
//			while (ret.next()) {  
//				String uid = ret.getString(1);  
//				String ufname = ret.getString(2);  
//				String ulname = ret.getString(3);  
//				String udate = ret.getString(4);  
//				System.out.println(uid + "\t" + ufname + "\t" + ulname + "\t" + udate );  
//			}//��ʾ����  
//			ret.close();  
//			db1.close();//�ر�����  
//		} catch (SQLException e) {  
//			e.printStackTrace();  
//		}  
		MyDatabase database = new MyDatabase();
		String result=database.executQueryWithJSON("select * from pinche.trip");
		System.out.println(result);
	}  
}  