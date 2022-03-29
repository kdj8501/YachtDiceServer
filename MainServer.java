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
import java.io.File;
import java.io.FileWriter;

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
	        	PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
	        	if (br.readLine().split(":")[0].equals("name")) {
		            nickname = br.readLine().split(":")[1];
		            if (users.indexOf(nickname) == -1) {
		            	writer.println("connect:able");
		            	writer.flush();
		            	writer.println("chact:1");
		            	writer.flush();
		            	users.add(nickname);
		            	lobby.addUser(nickname, writer);
		        	    consolelog(nickname + " Connect");
		        	    lobby.castChannel("message:" + nickname + "님이 로비에 입장하셨습니다.");
		        	    lobby.castChannel("users:" + lobby.getUserList());
		            	new MainServerThread(socket, nickname, writer, users, lobby, rooms).start();
		            }
		            else {
		            	writer.println("connect:unable");
		            	writer.flush();
		            	socket.close();
		            }
	        	}
	        	else {
	        		writer.println("connect:error");
	        		writer.flush();
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
	
	public static void consolelog(String msg) {
		LocalDateTime now = LocalDateTime.now();
		String formatedNow = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
		System.out.println("[" + formatedNow + "] " + msg);
		try {
			File file = new File("./yachtlog.txt");
			if (!file.exists())
				file.createNewFile();
			FileWriter fw = new FileWriter(file, true);
			PrintWriter writer = new PrintWriter(fw);
			writer.println("[" + formatedNow + "] " + msg);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
