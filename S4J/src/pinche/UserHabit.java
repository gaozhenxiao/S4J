package pinche;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class UserHabit extends HttpServlet {
	public UserHabit() {
		super();
	}

	public void init() throws ServletException {  
		System.out.println("init UserHabit ... ");
		new Thread(new Runnable() {
			@Override
			public void run() {
				updateUserBehavior();
			}
		}).start();
	}

	protected void updateUserBehavior() {
		while(true)
		{
			GregorianCalendar now = new GregorianCalendar();
			System.out.println( now.get(GregorianCalendar.HOUR_OF_DAY)+":" + now.get(GregorianCalendar.MINUTE) );
			if( now.get(GregorianCalendar.HOUR_OF_DAY)==1 && now.get(GregorianCalendar.MINUTE)==0 )
			{
				try
				{
					//do update
					int totalUserNo = 0;
					{
						String sql = "select count(uid) from user ";
						MyDatabase database = new MyDatabase(sql);
						ResultSet ret = database.getPreparedStatementExecuteQuery();//执行语句，得到结果集  
						boolean found  = ret.next();
						if( found )
						{
							totalUserNo = ret.getInt(1);
						}
						ret.close();  
						database.close();//关闭连接  
					}
					for( int i=0;i<totalUserNo/100+1;i++)
					{
						String sql = "select uid from user limit " + i*100 + ", 100";
						MyDatabase database = new MyDatabase(sql);
						ResultSet ret = database.getPreparedStatementExecuteQuery();//执行语句，得到结果集  
						while( ret.next() )
						{
							getUserLocationBehavior(ret.getString(1));
						}
						ret.close();  
						database.close();//关闭连接  
					}
				}
				catch( Exception e)
				{
					e.printStackTrace();
				}

			}
			try 
			{
				Thread.sleep(60000);
			}
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}

	private void getUserLocationBehavior(String uid) {
		updateLocations(uid);
		updateContacts(uid);
	}

	private void updateLocations(String uid) {
		try 
		{
			{
				String sql = "delete from user_habit where "+SQLStringUtility.sqlEntryValuePair("uid", uid);
				MyDatabase database = new MyDatabase(sql);
				database.getPreparedStatementExecute();
				database.close();
			}

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			GregorianCalendar now = new GregorianCalendar();
			String endDay = sdf.format( now.getTimeInMillis() );
			now.add(GregorianCalendar.YEAR, -1);
			String startDay = sdf.format( now.getTimeInMillis() );
			String sql = "SELECT t.departureLocation, t.arrivalLocation "
					+ " FROM trip t, trip_taken tt , user u, user v "
					+ "where t.driver=u.uid and tt.userID=v.uid and t.tripID=tt.tripID and (t.driver=" + SQLStringUtility.sqlString4IfNull(uid) + " or tt.userID="+SQLStringUtility.sqlString4IfNull(uid)+") ";
			if( startDay!=null && startDay.length()>0 )
				sql+=" and departureTime>=" + SQLStringUtility.sqlString4IfNull(startDay);
			if( endDay!=null && endDay.length()>0 )
				sql+=" and departureTime<=" + SQLStringUtility.sqlString4IfNull(endDay);

			HashMap<String, Integer> departureFrequency = new HashMap<>();
			HashMap<String, Integer> arriavlFrequency = new HashMap<>();
			{
				MyDatabase database = new MyDatabase();
				ResultSet ret = database.getStatementExecuteQuery(sql);
				while(ret.next())
				{
					String departureLocation = ret.getString(1);
					String arrivalLocation = ret.getString(2);
					int f1=0;
					if( departureFrequency.get(departureLocation)!=null  )
						f1 = departureFrequency.get(departureLocation);
					departureFrequency.put(departureLocation, f1+1);
					int f2=0;
					if( arriavlFrequency.get(arrivalLocation)!=null  )
						f2 = arriavlFrequency.get(arrivalLocation);
					arriavlFrequency.put(arrivalLocation, f2+1);
				}
				ret.close();
				database.close();
			}
			//*********************************************************************************************************************************************
			List<Map.Entry<String, Integer>> infoIds =  new ArrayList<Map.Entry<String, Integer>>(departureFrequency.entrySet());
			Collections.sort(infoIds, new Comparator<Map.Entry<String, Integer>>() {   
				public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {      
					return (o2.getValue() - o1.getValue()); 
				}
			}); 
			List<Map.Entry<String, Integer>> infoIds2 =  new ArrayList<Map.Entry<String, Integer>>(arriavlFrequency.entrySet());
			Collections.sort(infoIds2, new Comparator<Map.Entry<String, Integer>>() {   
				public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {      
					return (o2.getValue() - o1.getValue()); 
				}
			}); 
			//*********************************************************************************************************************************************
			System.out.println( "*********************************************************************************************************************************************" );
			for( int i=0;i<infoIds.size();i++ )
				System.out.println( infoIds.get(i).getKey()  );
			System.out.println( "*********************************************************************************************************************************************" );
			for( int i=0;i<infoIds2.size();i++ )
				System.out.println( infoIds2.get(i).getKey()  );
			System.out.println( "*********************************************************************************************************************************************" );
			//*********************************************************************************************************************************************
			BaiduMap baiduMap = new BaiduMap();
			for( int i=0;i<Math.min(4, infoIds.size() );i++)
			{
				String location = infoIds.get(i).getKey();
				String id = new MyDatabase().createID("user_habit", "id");
				sql = "insert into user_habit set "+SQLStringUtility.sqlEntryValuePair("id", id) 
					+ SQLStringUtility.wrapSemicomma(SQLStringUtility.sqlEntryValuePair("departureLocation", location))
					+ SQLStringUtility.wrapSemicomma(SQLStringUtility.sqlEntryValuePair("uid", uid));
				MyDatabase database= new MyDatabase();
				database.getStatementExecute(sql);
				database.close();
				boolean exist = new MyDatabase().checkExistance("precise_location", "location", location);
				if( exist==false)
				{
					double[] precise_location = baiduMap.getLongitudeLatitude(location);
					if( precise_location!=null && precise_location[0]!=-1)
					{
						sql = "insert into precise_location set "
								+SQLStringUtility.sqlEntryValuePair("location", location) 
								+", longitude="+ precise_location[0]
								+", latitude="+ precise_location[1] ;
						MyDatabase database2= new MyDatabase();
						database2.getStatementExecute(sql);
						database2.close();
					}
				}
			}
			for( int i=0;i<Math.min(4, infoIds2.size() );i++)
			{
				String location = infoIds2.get(i).getKey();
				String id = new MyDatabase().createID("user_habit", "id");
				sql = "insert into user_habit set "+SQLStringUtility.sqlEntryValuePair("id", id) 
					+ SQLStringUtility.wrapSemicomma(SQLStringUtility.sqlEntryValuePair("arrivalLocation", location))
					+ SQLStringUtility.wrapSemicomma(SQLStringUtility.sqlEntryValuePair("uid", uid));
				MyDatabase database= new MyDatabase();
				database.getStatementExecute(sql);
				database.close();
				boolean exist = new MyDatabase().checkExistance("precise_location", "location", location);
				if( exist==false)
				{
					double[] precise_location = baiduMap.getLongitudeLatitude(location);
					if( precise_location!=null && precise_location[0]!=-1)
					{
						sql = "insert into precise_location set "
								+SQLStringUtility.sqlEntryValuePair("location", location) 
								+ ", longitude="+ precise_location[0]
								+", latitude="+ precise_location[1];
						MyDatabase database2= new MyDatabase();
						database2.getStatementExecute(sql);
						database2.close();
					}
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	private void updateContacts(String uid) {
		try 
		{
			{
				String sql = "delete from contact where "+SQLStringUtility.sqlEntryValuePair("uid", uid);
				MyDatabase database = new MyDatabase(sql);
				database.getPreparedStatementExecute();
				database.close();
			}

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			GregorianCalendar now = new GregorianCalendar();
			String endDay = sdf.format( now.getTimeInMillis() );
			now.add(GregorianCalendar.YEAR, -1);
			String startDay = sdf.format( now.getTimeInMillis() );
			String sql = "( select tt.userID from trip t, trip_taken tt "
					+ "where t.driver="+SQLStringUtility.sqlString4IfNull(uid)
					+ " and tt.tripID=t.tripID and departureTime>=" + SQLStringUtility.sqlString4IfNull(startDay) +"  and departureTime<=" + SQLStringUtility.sqlString4IfNull(endDay) + " order by departureTime )"
					+ " union "
					+ "( select t.driver from trip t, trip_taken tt "
					+ " where tt.userID="+SQLStringUtility.sqlString4IfNull(uid)
					+ " and tt.tripID=t.tripID and departureTime>=" + SQLStringUtility.sqlString4IfNull(startDay) +"  and departureTime<=" + SQLStringUtility.sqlString4IfNull(endDay) + " order by departureTime )";
			System.out.println(sql);
			
			MyDatabase database = new MyDatabase();
			ResultSet ret = database.getStatementExecuteQuery(sql);
			while(ret.next())
			{
				String friend = ret.getString(1);
				String id =  new MyDatabase().createID("contact", "id");
				insertNewContact(id, uid, friend);
			}
			ret.close();
			database.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void insertNewContact(String id, String uid, String friend) {
		try 
		{
			String sql = " insert into contact set " + SQLStringUtility.sqlEntryValuePair("id", id)
				+ SQLStringUtility.wrapSemicomma(SQLStringUtility.sqlEntryValuePair("uid", uid))
				+ SQLStringUtility.wrapSemicomma(SQLStringUtility.sqlEntryValuePair("uid_friend", friend));
			System.out.println(sql);
			MyDatabase database = new MyDatabase();
			database.getStatementExecute(sql);
			database.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		UserHabit behavior = new UserHabit();
//		behavior.getUserLocationBehavior("00");
		behavior.updateContacts("uid1");
	}
}
