package pinche;

import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import net.sf.json.JSONObject;
 
/**
 * Servlet implementation class ProjectInterfaces
 */
@WebServlet("/ClientServerInterfaces")
public class ClientServerInterfaces extends SelfDescriptiveServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ClientServerInterfaces() {
        super();
    }

    public void init() throws ServletException {  
    	System.out.println("init ClientServerInterfaces ... ");
    	SelfDescriptiveServlet.descriptiveClasses.add(new Ad());
    	SelfDescriptiveServlet.descriptiveClasses.add(this);
    }
	@Override
	public Object get_action(String action) {
		return getAllInterfaces();
	}

	private Object getAllInterfaces() {
		ArrayList<SelfDescriptiveServlet> descriptiveClasses= SelfDescriptiveServlet.descriptiveClasses;
		JSONObject root =new JSONObject();
		for( int i=0;i<descriptiveClasses.size();i++)
		{
			SelfDescriptiveServlet descriptive = descriptiveClasses.get(i);
			JSONObject  obj =  JSONObject.fromObject( descriptive.getInterfaceDesctription() );
			root.put( descriptive.getClass().getSimpleName(), obj);
//					
//			Class c = descriptiveClasses.get(i);
//			try {
//				SelfDescriptiveServlet descriptive =(SelfDescriptiveServlet) ( c.newInstance() );
//				JSONObject  obj = JSONObject.fromObject( descriptive.getInterfaceDesctription() );
//				root.put( c.getSimpleName(), obj);
//			} catch (InstantiationException | IllegalAccessException e) {
//				e.printStackTrace();
//			}
		}
		System.out.println(root.toString());
		return root.toString();
	}

	@Override
	public Object post_action(String action) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getInterfaceDesctription() 
	{
		String description=super.getInterfaceDesctription();
		JSONObject root =JSONObject.fromObject(description);
		JSONObject ad = root.getJSONObject("get_action");
		ad.put("parameter action=",	"help");
		return root.toString();
	}

	
	public static void main(String[] args) {
		ClientServerInterfaces face = new ClientServerInterfaces();
		System.out.println( face.getAllInterfaces() );
	}
	

}
