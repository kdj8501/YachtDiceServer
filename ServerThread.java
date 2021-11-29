import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ServerThread extends Thread{
	private static final String SPLITER = "#!#";
	
    private String nickname = null;
    private Socket socket = null;
    private int curRoom;
    private PrintWriter writer;
    private Lobby lobby;
    private ArrayList<Channel> rooms;
    private ArrayList<String> users;
    
    public ServerThread(Socket socket, String nickname, ArrayList<String> users, Lobby lobby, ArrayList<Channel> rooms)
    {
        this.socket = socket;
        this.nickname = nickname;
        this.users = users;
        this.lobby = lobby;
        this.rooms = rooms;
        curRoom = -1;
    }

    @Override
    public void run()
    {
        try
        {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            while(true)
            {
                String request = bufferedReader.readLine();
                if(request == null)
                	break;

                String[] tokens = request.split(":");
                if("join".equals(tokens[0]))
                    doJoin();
                else if("quit".equals(tokens[0]))
                {
                    if (doQuit())
                    	break;
                }
                else if("message".equals(tokens[0]))
                {
                	if (lobby.isUserExist(nickname))
                		lobby.castChannel("message:" + nickname + ": " + request.substring(request.indexOf(":") + 1));
                	else
                		for (Channel x : rooms)
                			if (x.isUserExist(nickname))
                				x.castChannel("message:" + nickname + ": " + request.substring(request.indexOf(":") + 1));
                }
                else if("create".equals(tokens[0]))
                {
                	rooms.add(new Channel(request.substring(request.indexOf(":") + 1)));
                	doMessage("join:" + rooms.get(rooms.size() - 1).getName());
                	toJoin((rooms.size() - 1));
                }
                else if ("tojoin".equals(tokens[0]))
            	{
            		int num = Integer.parseInt(tokens[1]);
            		if (num >= rooms.size())
						doMessage("join:error");
            		else if (!rooms.get(num).isConnectable())
            			doMessage("join:full");
            		else if (rooms.get(num).isStarted())
            			doMessage("join:started");
            		else
            		{
            			doMessage("join:" + rooms.get(num).getName());
            			toJoin(num);
            		}
            	}
                else if("request".equals(tokens[0]))
                {
                	if ("rooms".equals(tokens[1]))
                	{
                		String str = "rooms:";
                		for (Channel x : rooms)
                		{
                			if (x.isStarted())
                				str = str + x.getName() + "(Playing)" + SPLITER;
                			else
                				str = str + x.getName() + "(" + x.getNumUsers() + "/" + x.getMaxUsers() + ")" + SPLITER;
                		}
                		doMessage(str);
                	}
                }
                else if("btn".equals(tokens[0]))
                {
                	if (!rooms.get(curRoom).isStarted())
                		doMessage("error:notstart");
                	else if (rooms.get(curRoom).getTurn() != rooms.get(curRoom).getUserIndex(nickname))
	                	doMessage("error:turn");
                	else
                	{
                		if ("roll".equals(tokens[1]))
                		{
                			if (!rooms.get(curRoom).isRollable())
                				doMessage("error:fullroll");
                			else
                			{
                				rooms.get(curRoom).rollDice();
                				rooms.get(curRoom).castChannel(rooms.get(curRoom).printGameInfo());
                			}
                		}
                		else
                		{
                			if (!rooms.get(curRoom).isSetable())
                				doMessage("error:cannot");
                			else
                			{
                				if (Integer.parseInt(tokens[1].split(SPLITER)[0]) != rooms.get(curRoom).getTurn())
                					doMessage("error:cannot");
                				else
                				{
	                				int n = Integer.parseInt(tokens[1].split(SPLITER)[1]);
	                				rooms.get(curRoom).castChannel("visible:" + rooms.get(curRoom).getTurn() + SPLITER + n);
		                			rooms.get(curRoom).setScore(n);
		                			rooms.get(curRoom).castChannel(rooms.get(curRoom).printGameInfo());
		                			if (rooms.get(curRoom).isFinished())
		                				rooms.get(curRoom).castChannel("message:Server:승자는 " + rooms.get(curRoom).getUserName((rooms.get(curRoom).getWinner())) + "님 입니다!");
                				}
                			}
                		}
                	}
                }
                else if("fixbtn".equals(tokens[0]))
                {
                	if (!rooms.get(curRoom).isStarted())
                		doMessage("error:notstart");
                	else if (rooms.get(curRoom).getTurn() != rooms.get(curRoom).getUserIndex(nickname))
	                	doMessage("error:turn");
                	else
                	{
                		int diceno = Integer.parseInt(tokens[1]);
                		if (diceno > 4)
                		{
                			diceno -= 5;
                			if (rooms.get(curRoom).isFixable(diceno))
                			{
                				rooms.get(curRoom).diceFix(diceno);
                				rooms.get(curRoom).castChannel(rooms.get(curRoom).printGameInfo());
                				if (rooms.get(curRoom).isSetable())
                					;// rooms.get(curRoom).castChannel(rooms.get(curRoom).printTmpInfo());
                			}
                			else
                				doMessage("error:cannot");
                		}
                		else
                		{
                			if (rooms.get(curRoom).isUnfixable(diceno))
                			{
                				rooms.get(curRoom).diceUnfix(diceno);
                				rooms.get(curRoom).castChannel(rooms.get(curRoom).printGameInfo());
                			}
                			else
                				doMessage("error:cannot");
                		}
                	}
                }
                else if("command".equals(tokens[0]))
                {
                	if (tokens[1].equals("start"))
                	{
                		if (rooms.get(curRoom).getUserIndex(nickname) != 0)
                			doMessage("error:auth");
                		else
                		{
                			if (rooms.get(curRoom).isStarted())
                				doMessage("error:start");
                			else if (rooms.get(curRoom).isConnectable())
                				doMessage("error:notfull");
                			else
                			{
	                			rooms.get(curRoom).gameStart();
	                			rooms.get(curRoom).castChannel("message:Server:게임을 시작합니다.");
                			}
                		}
                	}
                	if (tokens[1].equals("stop"))
                	{
                		if (rooms.get(curRoom).getUserIndex(nickname) != 0)
                			doMessage("error:auth");
                		else
                		{
                			if (!rooms.get(curRoom).isStarted())
                				doMessage("error:notstart");
                			else
                			{
	                			rooms.get(curRoom).gameStop();
	                			rooms.get(curRoom).castChannel("message:Server:게임을 중단합니다.");
                			}
                		}
                	}
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private synchronized void doJoin()
    {
	    lobby.addUser(nickname, writer);
	    Server.consolelog(nickname + " Connect");
	    lobby.castChannel("users:" + lobby.getUserList());
	    users.add(nickname);
    }
    
    private synchronized void toJoin(int n)
    {
    	curRoom = n;
    	if (n == -1)
    	{
    		lobby.addUser(nickname, writer);
    		lobby.castChannel("users:" + lobby.getUserList());
    	}
    	else
    	{
    		rooms.get(n).addUser(nickname, writer);
    		lobby.delUser(nickname, writer);
    		lobby.castChannel("users:" + lobby.getUserList());
    		rooms.get(n).castChannel("users:" + rooms.get(n).getUserList());
    	}
    }
    
    private synchronized boolean doQuit()
    {
    	boolean result = false;
    	if (lobby.isUserExist(nickname))
    	{
		    lobby.delUser(nickname, writer);
		    Server.consolelog(nickname + " Disconnect");
		    lobby.castChannel("users:" + lobby.getUserList());
		    users.remove(nickname);
		    result = true;
    	}
    	else
    	{
    		for (Channel x : rooms)
    			if (x.isUserExist(nickname))
    			{
    				x.delUser(nickname, writer);
    				x.castChannel("users:" + x.getUserList());
    				if (x.getNumUsers() == 0)
    					rooms.remove(x);
    				if (x.isStarted() && x.isConnectable())
    				{
    					x.castChannel("message:Server:게임 도중 유저가 탈주하여 게임이 중단됩니다.");
    					x.gameStop();
    				}
    				doMessage("join:-1");
    				toJoin(-1);
    				break;
    			}
    	}
    	return result;
    }
    
    private void doMessage(String data)
    {
    	writer.println(data);
    	writer.flush();
    }
    
}