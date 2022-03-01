import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;

public class MainServer {
	private static final int PORT = 7778;
	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		ArrayList<String> users = new ArrayList<String>();
        ArrayList<Channel> rooms = new ArrayList<Channel>();
        Lobby lobby = new Lobby();
	    try {   
	        serverSocket = new ServerSocket(PORT);
	        consolelog("Wait for Connect... PORT: " + PORT);
	        while(true) {
	        	String nickname;
	            Socket socket = serverSocket.accept();
	        	BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
	            nickname = br.readLine();
	            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
	            if (users.indexOf(nickname) == -1) {
	            	writer.println("connect:able");
	            	writer.flush();
	            	writer.println("chact:1");
	            	writer.flush();
	            	users.add(nickname);
	            	lobby.addUser(nickname, writer);
	        	    consolelog(nickname + " Connect");
	        	    lobby.castChannel("message:" + nickname + "���� �κ� �����ϼ̽��ϴ�.");
	        	    lobby.castChannel("users:" + lobby.getUserList());
	            	new MainServerThread(socket, nickname, writer, users, lobby, rooms).start();
	            }
	            else {
	            	writer.println("connect:unable");
	            	writer.flush();
	            	socket.close();
	            }
	        }
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	    finally {
            try {
                if (serverSocket != null && !serverSocket.isClosed())
                    serverSocket.close();
            } catch (IOException e) {
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
