package pinche;

public class SQLStringUtility {
	public synchronized static String sqlString4IfNull(String srcString)
	{
		if( srcString==null || srcString.length()==0)
			srcString = "null";
		else
			srcString = "'"+srcString + "'";
		return srcString;
	}
	public synchronized static String wrapSemicomma(String sql)
	{
		if( sql==null )
			return null;
		if( sql.length()==0)
			return "";
		return ","+sql;
	}
	public synchronized static String sqlEntryValuePair(String entry, String value)
	{
		if( entry==null || value==null )
			return "";
		String sql = "";
		if( value.length()==0 )
			sql = entry+"=null";
		else
		{
			sql = entry+"='"+value + "'";
		}
		return sql;
	}
}
