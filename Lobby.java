import java.util.ArrayList;
import java.io.PrintWriter;

public class Lobby {
	private static final String SPLITER = "#!#";
	
	private ArrayList<String> userID;
	private ArrayList<PrintWriter> PrintWriters;

	public Lobby()
	{
		userID = new ArrayList<String>();
		PrintWriters = new ArrayList<PrintWriter>();
	}
	
	public void addUser(String str, PrintWriter writer)
	{
		userID.add(str);
		PrintWriters.add(writer);
	}
	
	public void delUser(String str, PrintWriter writer)
	{
		userID.remove(str);
		PrintWriters.remove(writer);
	}
	
	public void castChannel(String data)
    {
        for(PrintWriter writer : PrintWriters)
        {
            writer.println(data);
            writer.flush();
        }
    }
	
	public String getUserList()
	{
		String str = "";
		for (String tmp : userID)
			str = str + tmp + SPLITER;
		return str;
	}
	
	public boolean isUserExist(String str) { return userID.indexOf(str) != -1; }
	public boolean isUserExist(PrintWriter writer) { return PrintWriters.indexOf(writer) != -1; }
}
