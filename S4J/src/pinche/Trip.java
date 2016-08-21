package pinche;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.RandomStringUtils;

/**
 * Servlet implementation class Trip
 */
@WebServlet("/Trip")
public class Trip extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Trip() 
	{
		super();
	}

	/** http://localhost:8080/S4J/Trip?action=SearchCar&departureLocation=longyanglu&arrivalLocation=nicheng
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 * ��������get�����У���ʽ��Trip?action=SearchCar&departureLocation=�Ž�&arrivalLocation=���
	 * �����г���ص����󣬰�����
	 * 1���ҳ�     action=SearchCar
	 * 2��ȷ��ƴ��   action=CarFound
	 * 3�����ˣ��������г���Ϣ action=publishTrip
	 * 4�����ݷ�����ʷ��������ʷ������Ϣ�����û�ѡ��action=searchCarsDriven
	 * 5���˿ͽ��ƴ����ϵ,action=deShareCarTrip
	 * 6����ѯĳ˾������˿ͣ������������г̣�action=ListSharedCars
	 * 
	 * 1���ҳ����õ���departureLocation��arrivalLocation, departureTime��seatsRequired������,seatsRequired��дĬ��Ϊ1
	 * ����JSON�б��������з����������г���Ϣ������tripID���������г���Ϣ��˾��uid
	 * 2��ȷ��ƴ��������������uid,   tripID��seatsRequired, ȷ��ƴ�������ݿ��vacantSeats���Զ���seatsRequired��
	 * ����JSON�б������˱����г���λȷ��������3���ֶ�
	 * 3�����ˣ���������String uid, String departureTime, String departureLocation, String arrivalTime, String arriavalLocation, int vacantSeats, double fee, String note
	 *       ��ӳ�����Ϣ��String type, int seats, String color, String vehicleLicense   ����Ϊ��
	 *       note:1000�ַ�����
	 *       departureTime��arrivalTime������String�ĸ�ʽ��2016-06-28 09:00:00
	 *       ����true/false
	 * 4��������ʷ������Ϣ��String uid������JSON��ʽ�ĳ���
	 * 5��deShareCarTrip����ҪString uid, String tripID
	 * 6����Ҫ������String uid, String startDay, String endDay������uidΪ�����ֶΣ���2�߿�ѡ��startDay��endDay��ʽ��ͬ��Ϊ2016-06-26 09:00:00
	 * 
	 * seats��vacantSeats����ǰ��ָ���ǳ���������λ��������ָʣ��ɹ�ƴ������λ��
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//		response.getWriter().append("Served at: ").append(request.getContextPath());
		request.setCharacterEncoding("UTF-8");

		Set<String> set  = request.getParameterMap().keySet();
		//		for (String key : set) 
		//		{  
		//			String param = request.getParameter( key );
		//			System.out.println("����"+param);
		//		}  

		String action = request.getParameter("action");
		if( action==null || action.length()==0)
		{
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			out.println("Parameter Action Required!");
			out.println("Parameters acceptable: ");
			out.println("action");
			out.println("departureLocation");
			out.println("arrivalLocation");
			out.println("uid");
			out.println("tripID");
			out.println("departureTime");
			out.println("arrivalTime");
			out.println("vacantSeats");
			out.println("note");
			out.println("type");
			out.println("seats");
			out.println("vehicleLicense");
			out.println("seatsRequired");
			out.println("startDay");
			out.println("endDay");
			out.close();
			return; 
		}
		if( action.equalsIgnoreCase("test") )
		{
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			for (String key : set) 
			{  
				String param = request.getParameter( key );
				out.println("to client: ����"+param);
				//				String str = new String(request.getParameter(key).getBytes("iso-8859-1"), "utf-8");
				//				out.println("����"+str);
			}  
			out.close();
		}
		String departureLocation = request.getParameter("departureLocation");
		String arrivalLocation = request.getParameter("arrivalLocation");
		String uid = request.getParameter("uid");
		String tripID = request.getParameter("tripID");
		String departureTime = request.getParameter("departureTime");
		String arrivalTime = request.getParameter("arrivalTime");
		String vacantSeats = request.getParameter("vacantSeats");
		String seatsRequired = request.getParameter("seatsRequired");
		String fee = request.getParameter("fee");

		String type = request.getParameter("type");
		String seats = request.getParameter("seats");
		String color = request.getParameter("color");
		String vehicleLicense = request.getParameter("vehicleLicense");
		String note = request.getParameter("note");

		String startDay = request.getParameter("startDay");
		String endDay = request.getParameter("endDay");

		Object callback = null;
		if( action.equalsIgnoreCase("SearchCar") )
		{
			int seatsCount=1;
			if( seatsRequired!=null && seatsRequired.length()>0 )
				seatsCount = Integer.parseInt(seatsRequired);
			//			callback =  searchVehicle(departureLocation, arrivalLocation, departureTime, seatsCount);
			callback =  searchTrips(departureLocation, arrivalLocation, departureTime, seatsCount);
		}
		else if ( action.equalsIgnoreCase("CarFound") )
		{
			int seatsCount=1;
			if( seatsRequired!=null && seatsRequired.length()>0 )
				seatsCount = Integer.parseInt(seatsRequired);
			callback = shareVehicle(uid, tripID, seatsCount);
		}
		else if ( action.equalsIgnoreCase("publishTrip") )
		{
			if( vehicleLicense!=null && vehicleLicense.length()>0 )
			{
				//				publishVehicle(type, seatsCount, color, vehicleLicense);
				MyDatabase database=new MyDatabase();
				boolean exist = database.checkExistance("vehicle", "license", vehicleLicense);
				Vehicle v = new Vehicle();
				if( exist )
					v.update(uid, vehicleLicense, type, seats, color);
				else
					v.register(uid, vehicleLicense, type, seats, color);
			}

			int vacantSeats2 = Integer.parseInt(vacantSeats);
			double cost=0;
			if( fee!=null && fee.length()>0 )
				cost= Double.parseDouble(fee);
			callback = publishTrip(uid, departureTime, departureLocation, arrivalTime, arrivalLocation, vacantSeats2, cost, vehicleLicense, note);
		}
		else if ( action.equalsIgnoreCase("searchCarsDriven") )
		{
			callback = searchVehicleDriven(uid);
		}
		else if ( action.equalsIgnoreCase("deShareCarTrip") )
		{
			callback = deShareVehicle(uid, tripID);
		}
		else if ( action.equalsIgnoreCase("ListSharedCars4Driver") )
		{
			if( uid!=null && uid.length()>0 )
			{
				callback = listSharedCars4Driver(uid, startDay, endDay);
			}
		}
		else if ( action.equalsIgnoreCase("ListTripsInvolved") )
		{
			if( uid!=null && uid.length()>0 )
			{
				callback = listTripsInvolved(uid, startDay, endDay);
			}
		}
		else if ( action.equalsIgnoreCase("EditTrip") )
		{
			if( tripID==null || tripID.length()==0 )
			{
				callback = "Error: tripID����Ϊ��";
			}
			else
			{
				int vacantSeatsNo = Integer.parseInt(vacantSeats);
				double cost= Double.parseDouble(fee);
				callback = editTrip(tripID, departureTime, departureLocation, arrivalTime, arrivalLocation, vacantSeatsNo, cost, vehicleLicense, note);
			}
		}
		else if ( action.equalsIgnoreCase("CeaseTrip") )
		{
			if( tripID==null || tripID.length()==0 )
			{
				callback = "Error: tripID����Ϊ��";
			}
			else
			{
				callback = ceaseTrip(tripID);
			}
		}
		else if ( action.equalsIgnoreCase("RemoveTrip") )
		{
			if( tripID==null || tripID.length()==0 )
			{
				callback = "Error: tripID����Ϊ��";
			}
			else
			{
				callback = removeTrip(tripID);
			}
		}
		else if ( action.equalsIgnoreCase("FuzzySearchTrip") )
		{
			if( uid==null || uid.length()==0 )
			{
				callback = "Error: tripID����Ϊ��";
			}
			else
			{
				callback = searchFavoriteTrips(uid);
			}
		}
		response.setContentType("application/x-json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.println( callback );
		out.close();
	}

	private Object searchFavoriteTrips(String uid) {
		HashSet<String> departureLocations = new HashSet<>();
		HashSet<String> arrivalLocations = new HashSet<>();
		try 
		{
			String sql = "select uh.departureLocation,uh.arrivalLocation  from user_habit uh where " + SQLStringUtility.sqlEntryValuePair("uh.uid", uid);    // +" and uh.departureLocation is not null limit 2";
			System.out.println( sql );
			MyDatabase db1 =  new MyDatabase(sql);
			ResultSet ret = db1.getPreparedStatementExecuteQuery();
			while ( ret.next() )
			{
				System.out.println( ret.getString(1)+"\t" + ret.getString(2) );
				if( departureLocations.size()<=2 &&  ret.getString(1)!=null )
					departureLocations.add( ret.getString(1) );
				if( arrivalLocations.size()<=2  && ret.getString(2)!=null)
					arrivalLocations.add( ret.getString(2) );
			}
			String trips ="";
			for( String  departure: departureLocations)
			{
				for( String arrival: arrivalLocations )
				{
					String trip = searchTrips(departure,arrival, null,1);
					if( trip!=null && trip.length()>=2)
					{
						trip=trip.substring(1, trip.length()-1);
						trip=trip.trim();
						System.out.println( departure+"\t"+arrival +"\t"+trip );
						if( trips.length()>0 && trip.length()>0)
							trips+=",";
						trips+=trip;
					}
				}
			}
			trips="["+trips+"]";
			ret.close();
			db1.close();
			return trips;
		}
		catch (SQLException e) {  
			e.printStackTrace();  
		}
		return null;
	}

	private Object ceaseTrip(String tripID) {
		try {  
			String sql =  "update trip set ceased=true where "+SQLStringUtility.sqlEntryValuePair("tripID", tripID) ;
			MyDatabase db1 =  new MyDatabase(sql);//����DBHelper����  
			db1.getPreparedStatementExecute();//ִ����䣬�õ������  
			db1.close();//�ر�����  
			return true;
		} catch (SQLException e) {  
			e.printStackTrace();  
			return "Error: "+e.getMessage();
		}  
	}

	private Object editTrip(String tripID, String departureTime, String departureLocation, String arrivalTime,
			String arrivalLocation, int vacantSeatsNo, double cost, String vehicleLicense, String note) {
		try {  
			String sql =  "update trip set "+ SQLStringUtility.sqlEntryValuePair("departureTime", departureTime) 
			+ SQLStringUtility.wrapSemicomma(SQLStringUtility.sqlEntryValuePair("departureLocation", departureLocation))
			+ SQLStringUtility.wrapSemicomma(SQLStringUtility.sqlEntryValuePair("arrivalTime", arrivalTime)) 
			+ SQLStringUtility.wrapSemicomma(SQLStringUtility.sqlEntryValuePair("arrivalLocation", arrivalLocation))
			+", vacantSeatsNo="+ vacantSeatsNo +", cost="+ cost
			+ SQLStringUtility.wrapSemicomma(SQLStringUtility.sqlEntryValuePair("vehicleLicense", vehicleLicense))+
			SQLStringUtility.wrapSemicomma(SQLStringUtility.sqlEntryValuePair("note", note)) 
			+ " where "+SQLStringUtility.sqlEntryValuePair("tripID", tripID) ;
			MyDatabase db1 =  new MyDatabase(sql);//����DBHelper����  
			ResultSet ret = db1.getPreparedStatementExecuteQuery();//ִ����䣬�õ������  
			ret.close();  
			db1.close();//�ر�����  
			return true;
		} catch (SQLException e) {  
			e.printStackTrace();  
			return "Error: "+e.getMessage();
		}  
	}

	private Object removeTrip(String tripID) {
		try {  
			String sql =  " delete from trip where tripID=" + SQLStringUtility.sqlString4IfNull(tripID);
			MyDatabase db1 =  new MyDatabase(sql);//����DBHelper����  
			db1.getPreparedStatementExecute();//ִ����䣬�õ������  
			db1.close();//�ر�����
			return "true:Deleted";
		} catch (SQLException e) {  
			e.printStackTrace();
			return "Error: "+e.getMessage();
		}  
	}

	private Object listSharedCars4Driver(String uid, String startDay, String endDay) {
		String sql = "SELECT t.*, v.* from trip t left join vehicle v "
				+ " ON t.vehicleLicense=v.license"
				+ " WHERE  t.driver=" + SQLStringUtility.sqlString4IfNull(uid) + " ";
		if( startDay!=null && startDay.length()>0 )
			sql+=" and departureTime>=" + SQLStringUtility.sqlString4IfNull(startDay);
		if( endDay!=null && endDay.length()>0 )
			sql+=" and departureTime<=" + SQLStringUtility.sqlString4IfNull(endDay);
		System.out.println(sql);
		MyDatabase database = new MyDatabase(sql);
		return database.getJSONResult();
	}
	/**
	 *ʾ��SQL
	 * SELECT t.*, tt.userID as passenger, tt.seatsTaken,
u.uid as driverUid,u.phone as driverPhone,u.username as driverName, uu.uid as passengerUid, uu.phone as passengerPhone, uu.username as passengerName,
v.license as vehicleLisence, v.type as vehicleType, v.color as vehicleColor
FROM trip as t left outer join trip_taken as tt
ON t.tripID=tt.tripID and (t.driver='00' or tt.userID='00')
left join user as u
on t.driver=u.uid
left join user as uu
on tt.userID=uu.uid
left join vehicle as v
on t.vehicleLicense=v.license
where departureTime>='2015-06-26 00:00:00'
	 * @param uid
	 * @param startDay
	 * @param endDay
	 * @return
	 */
	private Object listTripsInvolved(String uid, String startDay, String endDay) {
		String sql = "SELECT t.*, tt.userID as passenger, tt.seatsTaken,"
				+ " u.uid as driverUid,u.phone as driverPhone,u.username as driverName, uu.uid as passengerUid, uu.phone as passengerPhone, uu.username as passengerName,"
				+ " v.license as vehicleLisence, v.type as vehicleType, v.color as vehicleColor "
				+ "FROM trip as t left outer join trip_taken as tt "
				+ " ON t.tripID=tt.tripID and ( "+ SQLStringUtility.sqlEntryValuePair("t.driver", uid) + "  or  " + SQLStringUtility.sqlEntryValuePair("tt.userID", uid) + " ) "
				//				+ " ON t.tripID=tt.tripID and (t.driver='00' or tt.userID='00') "
				+ " left join user as u "
				+ " on t.driver=u.uid "
				+ " left join user as uu "
				+ " on tt.userID=uu.uid "
				+ " left join vehicle as v "
				+ " on t.vehicleLicense=v.license "
				+ " where true ";
		if( startDay!=null && startDay.length()>0 )
			sql+=" and departureTime>=" + SQLStringUtility.sqlString4IfNull(startDay);
		if( endDay!=null && endDay.length()>0 )
			sql+=" and departureTime<=" + SQLStringUtility.sqlString4IfNull(endDay);
		System.out.println(sql);
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

	public String deShareVehicle(String uid, String tripID)
	{
		if( uid==null || tripID==null || uid.length()==0 || tripID.length()==0)
			return "false";
		try 
		{
			int seatsTaken = 1;
			{
				String sql =  "select seatsTaken from trip_taken where tripID=" + SQLStringUtility.sqlString4IfNull(tripID) + " and userID=" + SQLStringUtility.sqlString4IfNull(uid);
				MyDatabase db1 =  new MyDatabase(sql);//����DBHelper����  
				ResultSet ret = db1.getPreparedStatementExecuteQuery();//ִ����䣬�õ������  
				boolean found  = ret.next();
				if( found == true )
				{
					seatsTaken = ret.getInt(1);
					ret.close();  
					db1.close();//�ر�����
				}
				else
				{
					ret.close();  
					db1.close();//�ر�����
					return "false: uid û���� tripID��";
				}
			}
			{
				String sql = "delete from trip_taken where tripID='"+tripID+"' and userID='"+uid+  "';";
				System.out.println( sql );
				MyDatabase db1 = new MyDatabase(sql);//����DBHelper����  
				db1.getPreparedStatementExecute();//ִ����䣬�õ������
				db1.close();//�ر�����  

				sql= " update trip set vacantSeat=" 
						+  "((SELECT vacantSeat where tripID='" + tripID +"')+"+seatsTaken
						+") where tripID='"+ tripID+ "'";
				System.out.println( sql );
				db1 = new MyDatabase(sql);//����DBHelper����  
				db1.getPreparedStatementExecute();//ִ����䣬�õ������
				db1.close();//�ر�����  
			}
			///**********************************����֪ͨ��˾��************************************************************//
			{
				String sql =  "select driver from trip where tripID=" + SQLStringUtility.sqlString4IfNull(tripID);
				MyDatabase db1 =  new MyDatabase(sql);//����DBHelper����  
				ResultSet ret = db1.getPreparedStatementExecuteQuery();//ִ����䣬�õ������  
				boolean found  = ret.next();
				if( found == true )
				{
					String driverID = ret.getString(1);
					ret.close();  
					db1.close();//�ر�����
					PincheWebsocket.sentMessage2Uid( driverID + ": vehicle released from "+ uid, driverID);
				}
			}
			///**********************************end of ����֪ͨ��˾��************************************************************//

			return "true";
		} catch (SQLException | IOException e) {  
			e.printStackTrace();  
		}
		return "false";
	}
	public String shareVehicle(String uid, String tripID, int seatsRequired)
	{
		if( uid==null || tripID==null || uid.length()==0 || tripID.length()==0)
			return "Error: tripID����uid����Ϊ��";
		try 
		{
			int vacantSeat = 0;
			{
				String sql =  "select vacantSeat from trip where tripID=" + SQLStringUtility.sqlString4IfNull(tripID);
				MyDatabase db1 =  new MyDatabase(sql);//����DBHelper����  
				ResultSet ret = db1.getPreparedStatementExecuteQuery();//ִ����䣬�õ������  
				boolean found  = ret.next();
				if( found == true )
				{
					vacantSeat = ret.getInt(1);
					ret.close();  
					db1.close();//�ر�����
				}
				else
				{
					ret.close();  
					db1.close();//�ر�����
					return "false: tripID �޷��ҵ�";
				}
				if( vacantSeat<seatsRequired )
					return "false: ��λ��Ŀ����";
			}

			String driverID ="";
			///**********************************����֪ͨ��˾��************************************************************//
			{
				String sql =  "select driver from trip where tripID=" + SQLStringUtility.sqlString4IfNull(tripID);
				MyDatabase db1 =  new MyDatabase(sql);//����DBHelper����  
				ResultSet ret = db1.getPreparedStatementExecuteQuery();//ִ����䣬�õ������  
				boolean found  = ret.next();
				if( found == true )
				{
					driverID = ret.getString(1);
					ret.close();  
					db1.close();//�ر�����
					if( driverID.equalsIgnoreCase(uid))
						return "false:˾������ѡ���Լ��ĳ���";
					PincheWebsocket.sentMessage2Uid( driverID + ": vehicle shared to "+ uid, driverID);
				}
			}
			///**********************************end of ����֪ͨ��˾��************************************************************//
			{
				String sql = "insert into trip_taken values('"+tripID+"','"+uid+  "'," +  seatsRequired +" );";
				System.out.println( sql );
				MyDatabase db1 = new MyDatabase(sql);//����DBHelper����  
				db1.getPreparedStatementExecute();//ִ����䣬�õ������
				db1.close();//�ر�����  

				int seatsLeft = vacantSeat - seatsRequired;
				sql= " update trip set vacantSeat="	+  seatsLeft +" where tripID='"+ tripID+ "'";
				System.out.println( sql );
				db1 = new MyDatabase(sql);//����DBHelper����  
				db1.getPreparedStatementExecute();//ִ����䣬�õ������
				db1.close();//�ر�����  
			}
			{
				String sql = "select * from trip_taken where tripID="+ SQLStringUtility.sqlString4IfNull(tripID)+ " and  userID="+ SQLStringUtility.sqlString4IfNull(uid);
				MyDatabase database = new MyDatabase(sql);
				return database.getJSONResult();
			}
		}
		catch (SQLException | IOException e)
		{  
			e.printStackTrace();
			System.out.println(e.getMessage());
			return "Error��"+ e.getMessage();
		}
	}
	public String searchVehicle(String departureLocation, String arrivalLocation, String departureTime, int seatsCount)
	{
		String sql = "select * from trip where ceased=false and departureLocation='" + departureLocation + "' and arrivalLocation='"+arrivalLocation +"'";
		if( departureTime!=null && departureTime.length()>0 )
			sql+=" and departureTime>=" + SQLStringUtility.sqlString4IfNull(departureTime);
		if( seatsCount>0)
			sql+=" and vacantSeat>=" + seatsCount;
		System.out.println(sql);
		MyDatabase database = new MyDatabase(sql);
		return database.getJSONResult();
	}
	public String searchTrips(String departureLocation, String arrivalLocation, String departureTime, int seatsCount)
	{
		double precise_location_departure[] = getPreciseLocationFromDatabase(departureLocation);
		double precise_location_arrival[] = getPreciseLocationFromDatabase(arrivalLocation);
		String sql = "";
		if( precise_location_departure!=null && precise_location_departure[0]!=-1 && precise_location_arrival!=null && precise_location_arrival[0]!=-1 )
		{
			/**
			 *�������һ�ȵĳ���,��������λ�õ�γ�Ȳ�ͬ���в���
				�ڳ����,360�ȵľ��ȿ�Խ40000��������,����1�ȿ�Խ����Ϊ111.11����
				�����������ļ�����,����1������Խ�ľ���ֻ��Ϊ0
				γ�����һ��֮��ľ��뵹�ǲ��㶨��,��Խ�ľ���Ϊһ������Ȧ��������ʮ��֮һ,Լ����111����
				1�ȵ���60��,1�ֵ���60�� 
			 * */
			double tolerance[] = { 0.01,  0.01 };
			double departure_upper[] = {precise_location_departure[0]+tolerance[0], precise_location_departure[1]+tolerance[1]};
			double departure_lower[] = {precise_location_departure[0]-tolerance[0], precise_location_departure[1]-tolerance[1]};
			double arrival_upper[]= {precise_location_arrival[0]+tolerance[0], precise_location_arrival[1]+tolerance[1]};
			double arrival_lower[]= {precise_location_arrival[0]-tolerance[0], precise_location_arrival[1]-tolerance[1]};

			sql = "SELECT t.*, v.* "
					+ " FROM precise_location p,precise_location pp, trip t left join vehicle v "
					+ " ON t.vehicleLicense=v.license "
					+ " where ceased=false "
					+ " and ( t.departureLocation=p.location and p.longitude<" + departure_upper[0] +" and p.longitude>" + departure_lower[0] +" and p.latitude<"+ departure_upper[1] +" and p.latitude>"+departure_lower[1]+ ")"
					+ " and ( t.arrivalLocation=pp.location and pp.longitude<" + arrival_upper[0] +" and pp.longitude>" + arrival_lower[0] +" and pp.latitude<"+ arrival_upper[1] +" and pp.latitude>"+arrival_lower[1]+ ")" ;
		}
		else
		{
			sql = "SELECT t.*, v.* from trip t left join vehicle v"
					+ " ON t.vehicleLicense=v.license"
					+ " where ceased=false "
					+ " and departureLocation='" + departureLocation + "' and arrivalLocation='"+arrivalLocation +"'";
		}
		if( departureTime!=null && departureTime.length()>0 )
			sql+=" and departureTime>=" + SQLStringUtility.sqlString4IfNull(departureTime);
		if( seatsCount>0)
			sql+=" and vacantSeat>=" + seatsCount;

		System.out.println( sql );
		MyDatabase database=new MyDatabase(sql);
		return database.getJSONResult();
	}
	public String searchVehicleDriven(String uid)
	{
		String sql = "SELECT vehicle.* from vehicle, trip where trip.driver = " + SQLStringUtility.sqlString4IfNull(uid) +" and trip.vehicleLicense=vehicle.license";
		MyDatabase database = new MyDatabase(sql);
		return database.getJSONResult();
	}
	private boolean publishTrip(String driver, String departureTime, String departureLocation, String arrivalTime, String arrivalLocation, int vacantSeats, double fee, String vehicleLicense, String note)
	{
		String id  = RandomStringUtils.randomAlphanumeric(32);
		while ( checkTripID(id) ==true  )
			id  = RandomStringUtils.randomAlphanumeric(32);
		try 
		{
			String sql = "insert into trip values('"+id+"','"+driver+  "','" + departureTime + "','" + departureLocation + "',"+SQLStringUtility.sqlString4IfNull(arrivalTime)+",'"+ arrivalLocation +"'," 
					+vacantSeats+","+fee+"," +SQLStringUtility.sqlString4IfNull(vehicleLicense)  +"," +SQLStringUtility.sqlString4IfNull(note)  +", false)";
			System.out.println( sql );
			MyDatabase db1 = new MyDatabase(sql);//����DBHelper����  
			db1.getPreparedStatementExecute();//ִ����䣬�õ������
			db1.close();//�ر�����  
			//*********�ҵ���ص�user��������ߣ����������Ϣ��Ӧ��ʹ��ר�ŵĴ�������ʵ���첽��������ÿ����¼��������һ���̳߳ɱ�̫����****************************************
			TripPusher pusher = new TripPusher(id, driver, departureTime, departureLocation, arrivalTime, arrivalLocation, vacantSeats, fee, vehicleLicense, note);
			new Thread(pusher).start();
			//*******************************************************************
			boolean hasUser = checkTripID(id);
			return hasUser;
		} catch (SQLException e) {  
			e.printStackTrace();  
		}
		return false;
	}
	//*********�ҵ���ص�user��������ߣ����������Ϣ��Ӧ��ʹ��ר�ŵĴ�������ʵ���첽��������ÿ����¼��������һ���̳߳ɱ�̫����****************************************
	class TripPusher implements Runnable
	{
		String tripID="";
		String driver = "";
		String departureTime="";
		String departureLocation="";
		String arrivalTime="";
		String arrivalLocation="";
		int vacantSeats;
		double fee;
		String vehicleLicense="";
		String note="";

		public TripPusher(String tripID, String driver, String departureTime, String departureLocation,
				String arrivalTime, String arrivalLocation, int vacantSeats, double fee, String vehicleLicense,
				String note) {
			super();
			this.tripID = tripID;
			this.driver = driver;
			this.departureTime = departureTime;
			this.departureLocation = departureLocation;
			this.arrivalTime = arrivalTime;
			this.arrivalLocation = arrivalLocation;
			this.vacantSeats = vacantSeats;
			this.fee = fee;
			this.vehicleLicense = vehicleLicense;
			this.note = note;
		}


		@Override
		public void run() {
			try {
				//�ȴ�˾�����յ�ȷ����Ϣ
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try 
			{
				double precise_location_departure[] = getPreciseLocationFromDatabase(departureLocation);
				double precise_location_arrival[] = getPreciseLocationFromDatabase(arrivalLocation);
				String sql = "";
				HashSet<String> uids = new HashSet <String>();

				if( precise_location_departure!=null && precise_location_departure[0]!=-1 && precise_location_arrival!=null && precise_location_arrival[0]!=-1 )
				{
					/**
					 *�������һ�ȵĳ���,��������λ�õ�γ�Ȳ�ͬ���в���
					�ڳ����,360�ȵľ��ȿ�Խ40000��������,����1�ȿ�Խ����Ϊ111.11����
					�����������ļ�����,����1������Խ�ľ���ֻ��Ϊ0
					γ�����һ��֮��ľ��뵹�ǲ��㶨��,��Խ�ľ���Ϊһ������Ȧ��������ʮ��֮һ,Լ����111����
					1�ȵ���60��,1�ֵ���60�� 
					 * */
					double tolerance[] = { 0.01,  0.01 };
					double departure_upper[] = {precise_location_departure[0]+tolerance[0], precise_location_departure[1]+tolerance[1]};
					double departure_lower[] = {precise_location_departure[0]-tolerance[0], precise_location_departure[1]-tolerance[1]};
					double arrival_upper[]= {precise_location_arrival[0]+tolerance[0], precise_location_arrival[1]+tolerance[1]};
					double arrival_lower[]= {precise_location_arrival[0]-tolerance[0], precise_location_arrival[1]-tolerance[1]};

					sql = "SELECT u.uid FROM precise_location p, user_habit u "
							+ "where ( u.departureLocation=p.location and p.longitude<" + departure_upper[0] +" and p.longitude>" + departure_lower[0] +" and p.latitude<"+ departure_upper[1] +" and p.latitude>"+departure_lower[1]+ ")"
							+ " or ( u.arrivalLocation=p.location and p.longitude<" + arrival_upper[0] +" and p.longitude>" + arrival_lower[0] +" and p.latitude<"+ arrival_upper[1] +" and p.latitude>"+arrival_lower[1]+ ")" ;
					System.out.println( sql );
					MyDatabase db1 =  new MyDatabase(sql);
					ResultSet ret = db1.getPreparedStatementExecuteQuery();
					while( ret.next() )
						uids.add( ret.getString(1) );
					ret.close();
					db1.close();//�ر�����  
				} 
				//�����ǰ��յ���λ�������������ˣ��������������ȷ��ַƥ��
				{
					sql = "select uid from user_habit "+ "where ("+SQLStringUtility.sqlEntryValuePair("departureLocation", departureLocation)+") or ("+SQLStringUtility.sqlEntryValuePair("arrivalLocation", arrivalLocation) +") ";
					System.out.println( sql );
					MyDatabase db1 =  new MyDatabase(sql);
					ResultSet ret = db1.getPreparedStatementExecuteQuery();
					while( ret.next() )
						uids.add( ret.getString(1) );
					ret.close();
					db1.close();//�ر�����  
				}
				//�������������
				{
					sql = "SELECT uid_friend FROM contact c where " +  SQLStringUtility.sqlEntryValuePair("uid", driver);
					System.out.println( sql );
					MyDatabase db1 =  new MyDatabase(sql);
					ResultSet ret = db1.getPreparedStatementExecuteQuery();
					while( ret.next() )
						uids.add( ret.getString(1) );
					ret.close();
					db1.close();//�ر�����  
				}

				for( String uid : uids)
				{
					String message = SQLStringUtility.sqlEntryValuePair("tripID", tripID) 
							+SQLStringUtility.wrapSemicomma( SQLStringUtility.sqlEntryValuePair("driver", driver ) )
							+SQLStringUtility.wrapSemicomma( SQLStringUtility.sqlEntryValuePair("departureTime", departureTime ) )
							+SQLStringUtility.wrapSemicomma( SQLStringUtility.sqlEntryValuePair("departureLocation", departureLocation ) )
							+SQLStringUtility.wrapSemicomma( SQLStringUtility.sqlEntryValuePair("arrivalTime", arrivalTime ) )
							+SQLStringUtility.wrapSemicomma( SQLStringUtility.sqlEntryValuePair("arrivalLocation", arrivalLocation ) )
							+SQLStringUtility.wrapSemicomma( SQLStringUtility.sqlEntryValuePair("vacantSeats", Integer.toString(vacantSeats ) )) 
							+SQLStringUtility.wrapSemicomma( SQLStringUtility.sqlEntryValuePair("vehicleLicense", vehicleLicense ) )
							+SQLStringUtility.wrapSemicomma( SQLStringUtility.sqlEntryValuePair("note", note ) );
					System.out.println("sent to uid:" + uid+"\t message:"+message);
					PincheWebsocket.sentMessage2Uid( "new trip:" +message  , uid);
				}
			} catch (SQLException | IOException e) {  
				e.printStackTrace();  
			}
		}

	}

	public double[] getPreciseLocationFromDatabase(String location)
	{
		double precise_location[]  = {-1.0,-1.0};
		try 
		{
			String sql = "select longitude, latitude from precise_location where "+SQLStringUtility.sqlEntryValuePair("location", location);
			System.out.println( sql );
			MyDatabase db1 =  new MyDatabase(sql);
			ResultSet ret = db1.getPreparedStatementExecuteQuery();
			if ( ret.next() )
			{
				precise_location[0] = ret.getDouble(1);
				precise_location[1] = ret.getDouble(2); 
			}
		}
		catch (SQLException e) {  
			e.printStackTrace();  
		}
		return precise_location;
	}
//	private String publishVehicle(String type, int seats, String color, String vehicleLicense)
//	{
//		String vehicleID="";
//		try {  
//			String sql =  "select id from vehicle where license = '" + vehicleLicense +"' and type = '"+type+"' limit 1;";//SQL���  
//			MyDatabase db1 =  new MyDatabase(sql);//����DBHelper����  
//			ResultSet ret = db1.getPreparedStatementExecuteQuery();//ִ����䣬�õ������  
//			boolean found  = ret.next();
//			if( found == true )
//				vehicleID = ret.getString(1);
//			ret.close();  
//			db1.close();//�ر�����
//			if( found = true )
//				return vehicleID;
//		} catch (SQLException e) {  
//			e.printStackTrace();  
//		}  
//
//		String id  = RandomStringUtils.randomAlphanumeric(32);
//		while ( checkVehicleID(id) ==true  )
//			id  = RandomStringUtils.randomAlphanumeric(32);
//		try 
//		{
//			String sql = "insert into vehicle values('"+id+"','"+type+  "',"+seats+",'"+color+"','"+ vehicleLicense +"')";
//			System.out.println( sql );
//			MyDatabase db1 = new MyDatabase(sql);//����DBHelper����  
//			db1.getPreparedStatementExecute();//ִ����䣬�õ������
//			db1.close();//�ر�����  
//			return id;
//		} catch (SQLException e) {  
//			e.printStackTrace();  
//		}
//		return null;
//	}
	private boolean checkVehicleID(String id)
	{
		try {  
			String sql =  "select 1 from vehicle where id = '" + id +"' limit 1;";//SQL���  
			MyDatabase db1 =  new MyDatabase(sql);//����DBHelper����  
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
	private boolean checkTripID(String id)
	{
		try {  
			String sql =  "select 1 from trip where tripID = '" + id +"' limit 1;";//SQL���  
			MyDatabase db1 =  new MyDatabase(sql);//����DBHelper����  
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
		//		String string = "�̾���";
		//		String eStr = URLEncoder.encode(string, "utf-8");
		//		System.out.println(eStr);
		//		System.out.println(URLDecoder.decode(eStr, "utf-8"));

		//		String uid  = RandomStringUtils.randomAlphanumeric(32);
		//		System.out.println( uid );
		Trip trip = new Trip();
		//		trip.publishTrip("00", "2016-06-28 09:00:00", "����", "", "�����·", 3, 5);
		//		System.out.println( trip.searchVehicle("����", "����·", "2016-06-28 09:00:00"));
//						System.out.println( trip.searchTrips("�Ž�", "���", "2016-06-28 09:00:00",2));
		//		trip.publishVehicle("DasAuto", 4, "red", "88888");
		//		trip.shareVehicle("0101", "trip2", 2);
		//		trip.deShareVehicle("0101", "trip2");
		//		trip.ceaseTrip("uWPf8G4LcXoXd1r4CC2EoYyKfj1g1PIh");
		//		trip.removeTrip("uWPf8G4LcXoXd1r4CC2EoYyKfj1g1PIh");
//		System.out.println( trip.listTripsInvolved("00", "2015-06-26 00:00:00" , null));
//						System.out.println( trip.listSharedCars4Driver("00", "2015-06-26 00:00:00" , null));
		//		System.out.println( trip.listSharedVehicles("00", "2010-1-1 00:00:00" , "2017-1-1 00:00:00"));
				trip.publishTrip("uid1", "2016-07-26 00:00:00", "�Ž�", null, "���", 4, 20, "88888", null);
		//		System.out.println( trip.searchFavoriteTrips("00") );
	}
}
