package pinche;

import javax.servlet.annotation.WebServlet;

import net.sf.json.JSONObject;
@WebServlet("/Ad")
public class Ad extends SelfDescriptiveServlet {

	public Ad() {
        super();
    }
	
	@Override
	public Object get_action(String action) {
		Object callback =null;
		if( action.equalsIgnoreCase("getRelativeAds"))
		{
			callback=get_action_getRelativeAds( request.getParameter( "uid") );
		}
		return callback;
	}

	private Object get_action_getRelativeAds(String uid) {
		return "Ads for uid:"+uid;
	}

	@Override
	public Object post_action(String action) {
		return null;
	}

	@Override
	public String getInterfaceDesctription() 
	{
		String description=super.getInterfaceDesctription();
		JSONObject root =JSONObject.fromObject(description);
		JSONObject ad = root.getJSONObject("get_action_getRelativeAds");
		ad.put("parameter  uid=",	"String");
		return root.toString();
	}

	public static void main(String[] args) {
		Ad ad=new Ad();
		System.out.println( ad.getInterfaceDesctription() );
	}

}
