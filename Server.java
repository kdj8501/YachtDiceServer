import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Server {
    private static final int PORT = 7777;
 
	public static void main(String[] args)
    {
        ServerSocket serverSocket = null;
        ArrayList<String> users = new ArrayList<String>();
        ArrayList<Channel> rooms = new ArrayList<Channel>();
        Lobby lobby = new Lobby();
        try
        {
        	
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress("0.0.0.0", PORT));
            consolelog("Wait for Connect... PORT: " + PORT);
            
            while(true)
            {
            	String nickname;
                Socket socket = serverSocket.accept();
                try
		        {
                	PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
		        	BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
		            nickname = br.readLine();
		            if (users.indexOf(nickname) != -1)
		            {
		            	printWriter.println("N");
		            	printWriter.flush();
		            	socket.close();
		            }
		            else
		            {
		            	printWriter.println("Y");
		            	printWriter.flush();
		            	new ServerThread(socket, nickname, users, lobby, rooms).start();
		            }
		        }
		        catch (IOException h)
		        {
		        	h.printStackTrace();
		        }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (serverSocket != null && !serverSocket.isClosed())
                    serverSocket.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
	
	public static void consolelog(String msg)
	{
		LocalDateTime now = LocalDateTime.now();
		String formatedNow = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
		System.out.println("[" + formatedNow + "] " + msg);
	}
}