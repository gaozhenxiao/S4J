package pinche;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import net.sf.json.JSONObject;

public class BaiduMap {

	private static final double EARTH_RADIUS = 6378.137;//����뾶
	private static double rad(double d)
	{
	   return d * Math.PI / 180.0;
	}

	public static double GetDistance(double lng1, double lat1, double lng2,  double lat2)
	{
	   double radLat1 = rad(lat1);
	   double radLat2 = rad(lat2);
	   double a = radLat1 - radLat2;
	   double b = rad(lng1) - rad(lng2);

	   double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) + Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
	   s = s * EARTH_RADIUS;
//	   s = Math.round(s * 10000) / 10000;
	   return s;
	}
	public double[] getLongitudeLatitude(String place)
	{
		double[] longitudeLatitude={-1,-1};
		String json=getLongitudeLatitudeString(place);
		if( json==null || json.length()<  "showLocation&&showLocation".length() )
			return longitudeLatitude;
		int start = "showLocation&&showLocation".length()+1;
		json=json.substring(start, json.length()-1);
		//{"status":0,"result":{"location":{"lng":121.56489229156465,"lat":31.190289707129688},"precise":0,"confidence":30,"level":"��·"}}
		//{"status":1,"msg":"Internal Service Error:����ؽ��","results":[]}
		JSONObject jsonObject =  JSONObject.fromObject(json);
		int status=jsonObject.getInt("status");
		if( status==1)
			return longitudeLatitude;
		JSONObject jsonResult = jsonObject.getJSONObject("result").getJSONObject("location");
		longitudeLatitude[0]= jsonResult.getDouble("lng");
		longitudeLatitude[1]=  jsonResult.getDouble("lat");
		return longitudeLatitude;
	}
	public String  getLongitudeLatitudeString( String place )
	{
		try 
		{
			/**
			 * ��ַ���漰�������ַ����确������&���ȡ����Բ���ֱ����String����URI�����ʡ��������%0xXX��ʽ����������ַ��������ְ취��ֱ�ۡ�����ֻ���Ȱ�Stringת��URL�����ܹ�URL����URI�ķ�����������⡣
			 * ��δ����ǱȽϱ�׼��д����ʹ��URLEncode.encode()ֱ�ӱ��������е���ĸ���ַ�������%0xXX��������
			 */
			String strUrl = "http://api.map.baidu.com/geocoder/v2/?address=" +  place +"&output=json&ak=XtPr14sMLsfBuQZwk0idTzQiztCpFAgs&callback=showLocation";
			URL url = new URL(strUrl); 
			URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);
			HttpGet httpGet = new HttpGet( uri );
			System.out.println( "������ַΪ��"+ uri ); 

//			String host="http://120.26.76.25/S4J/Trip?";
//			String parameters =  "action=searchCar&departureLocation="+ URLEncoder.encode("����", "utf-8")+"&arrivalLocation="
//					+ URLEncoder.encode("�����·", "utf-8")+"&departureTime="+URLEncoder.encode("2000-05-30 09:00:00", "utf-8")+"&seatsCount="+URLEncoder.encode("2", "utf-8");
//			//			String parameters="action=test&departureLocation="+ URLEncoder.encode("�Ž�", "utf-8");
//
//			System.out.println( "������ַΪ��"+ host+parameters ); 
//			//			String eUrl=host+ URLEncoder.encode(parameters, "utf-8");
//			//			System.out.println( "�����������ַΪ��"+ eUrl );
//			HttpGet httpGet = new HttpGet( host+parameters );

//			httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.72 Safari/537.36");
//			DefaultHttpClient httpclient = new DefaultHttpClient( new BasicClientConnectionManager() );
			HttpClient httpclient = HttpClients.createDefault();  
			//			httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
			HttpResponse response = httpclient.execute(httpGet);
			String responseString =  EntityUtils.toString( response.getEntity() , "UTF-8");
			httpGet.releaseConnection();

			System.out.println(responseString);
			return responseString;
		} catch ( IOException | URISyntaxException e) {
			e.printStackTrace();
		}
		return "";
	}
	public static void main(String[] args) {
		BaiduMap test = new BaiduMap();
//		test.getLongitudeLatitudeString( "����·" );
		double[] location=  test.getLongitudeLatitude("��Ϫһ��");
		System.out.println("pricise location ��Ϫһ��:"+location[0]+","+location[1]);
		double[] location2=  test.getLongitudeLatitude("����·����վ");
		System.out.println("pricise location ����·����վ:"+location2[0]+","+location2[1]);
		double[] location3=  test.getLongitudeLatitude("������ͥ");
		System.out.println("pricise location ������ͥ:"+location3[0]+","+location3[1]);
		double[] location4=  test.getLongitudeLatitude("�Ž��߿�");
		System.out.println("pricise location �Ž��߿�:"+location4[0]+","+location4[1]);
		double[] location5=  test.getLongitudeLatitude("�Ž�");
		System.out.println("pricise location 4 �Ž�:"+location5[0]+","+location5[1]);
		double[] location6=  test.getLongitudeLatitude("���");
		System.out.println("pricise location 4 ���:"+location6[0]+","+location6[1]);
		double[] location7=  test.getLongitudeLatitude("�Ϻ�����·");
		System.out.println("pricise location �Ϻ�����·:"+location7[0]+","+location7[1]);
		double distance = BaiduMap.GetDistance(location[0], location[1], location2[0], location2[1]);
		System.out.println( distance );
	}

}
