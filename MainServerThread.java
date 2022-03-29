import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MainServerThread extends Thread{
	private static final String SPLITER = "#!#";
	
    private String nickname = null;
    private Socket socket = null;
    private int curRoom;
    private PrintWriter writer;
    private Lobby lobby;
    private ArrayList<Channel> rooms;
    private ArrayList<String> users;
    
    public MainServerThread(Socket socket, String nickname, PrintWriter writer, ArrayList<String> users, Lobby lobby, ArrayList<Channel> rooms) {
        this.socket = socket;
        this.nickname = nickname;
        this.writer = writer;
        this.lobby = lobby;
        this.rooms = rooms;
        this.users = users;
        curRoom = -1;
    }

    @Override
    public void run() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            while(true) {
                String request = br.readLine();
                if(request == null) {
                	for (Channel h : rooms)
            			if (h.isUserExist(nickname)) {
            				doQuit();
            				break;
            			}
                	if (lobby.isUserExist(nickname))
                		doQuit();
                	break;
                }

                String[] tokens = request.split(":");
                if("quit".equals(tokens[0])) {
                    if (doQuit())
                    	break;
                }
                else if("message".equals(tokens[0])) {
                	MainServer.consolelog("(room: " + curRoom + ") " + nickname + ": " + request.substring(request.indexOf(":") + 1));
                	if (lobby.isUserExist(nickname))
                		lobby.castChannel("message:" + nickname + ": " + request.substring(request.indexOf(":") + 1));
                	else
                		for (Channel x : rooms)
                			if (x.isUserExist(nickname))
                				x.castChannel("message:" + nickname + ": " + request.substring(request.indexOf(":") + 1));
                }
                else if("create".equals(tokens[0])) {
                	rooms.add(new Channel(request.substring(request.indexOf(":") + 1)));
                	doMessage("join:" + rooms.get(rooms.size() - 1).getName());
                	doMessage("chact:2");
                	doJoin((rooms.size() - 1));
                	String str = "rooms:";
                	for (Channel h : rooms) {
            			if (h.isStarted())
            				str = str + h.getName() + "(Playing)" + SPLITER;
            			else
            				str = str + h.getName() + "(" + h.getNumUsers() + "/" + h.getMaxUsers() + ")" + SPLITER;
            		}
            		lobby.castChannel(str);
                }
                else if ("tojoin".equals(tokens[0])) {
            		int num = Integer.parseInt(tokens[1]);
            		if (num >= rooms.size())
						doMessage("join:error");
            		else if (!rooms.get(num).isConnectable())
            			doMessage("join:full");
            		else if (rooms.get(num).isStarted())
            			doMessage("join:started");
            		else {
            			if (num != -1)
            				doMessage("join:ok");
            			doMessage("chact:2");
            			doJoin(num);
            		}
            	}
                else if("request".equals(tokens[0])) {
                	if ("rooms".equals(tokens[1])) {
                		String str = "rooms:";
                		for (Channel x : rooms) {
                			if (x.isStarted())
                				str = str + x.getName() + "(Playing)" + SPLITER;
                			else
                				str = str + x.getName() + "(" + x.getNumUsers() + "/" + x.getMaxUsers() + ")" + SPLITER;
                		}
                		lobby.castChannel(str);
                	}
                }
                else if("btn".equals(tokens[0])) {
                	if (!rooms.get(curRoom).isStarted())
                		doMessage("error:notstart");
                	else if (rooms.get(curRoom).getTurn() != rooms.get(curRoom).getUserIndex(nickname))
	                	doMessage("error:turn");
                	else {
                		if ("roll".equals(tokens[1]))
                		{
                			if (!rooms.get(curRoom).isRollable())
                				doMessage("error:fullroll");
                			else {
                				rooms.get(curRoom).rollDice();
                				rooms.get(curRoom).castChannel(rooms.get(curRoom).printGameInfo());
                			}
                		}
                		else {
                			if (!rooms.get(curRoom).isSetable(Integer.parseInt(tokens[1].split(SPLITER)[0]), Integer.parseInt(tokens[1].split(SPLITER)[1])))
                				doMessage("error:cannot");
                			else {
                				if (Integer.parseInt(tokens[1].split(SPLITER)[0]) != rooms.get(curRoom).getTurn())
                					doMessage("error:cannot");
                				else {
	                				int n = Integer.parseInt(tokens[1].split(SPLITER)[1]);
		                			rooms.get(curRoom).setScore(n);
		                			rooms.get(curRoom).castChannel(rooms.get(curRoom).printGameInfo());
		                			if (rooms.get(curRoom).isFinished())
		                				rooms.get(curRoom).castChannel("message:Server:승자는 " + rooms.get(curRoom).getWinner() + " 입니다!");
                				}
                			}
                		}
                	}
                }
                else if("fixbtn".equals(tokens[0])) {
                	if (!rooms.get(curRoom).isStarted())
                		doMessage("error:notstart");
                	else if (rooms.get(curRoom).getTurn() != rooms.get(curRoom).getUserIndex(nickname))
	                	doMessage("error:turn");
                	else {
                		int diceno = Integer.parseInt(tokens[1]);
                		if (diceno > 4) {
                			diceno -= 5;
                			if (rooms.get(curRoom).isFixable(diceno)) {
                				rooms.get(curRoom).diceFix(diceno);
                				rooms.get(curRoom).castChannel(rooms.get(curRoom).printGameInfo());
                			}
                			else
                				doMessage("error:cannot");
                		}
                		else {
                			if (rooms.get(curRoom).isUnfixable(diceno)) {
                				rooms.get(curRoom).diceUnfix(diceno);
                				rooms.get(curRoom).castChannel(rooms.get(curRoom).printGameInfo());
                			}
                			else
                				doMessage("error:cannot");
                		}
                	}
                }
                else if("command".equals(tokens[0])) {
                	if (tokens[1].equals("start")) {
                		if (rooms.get(curRoom).getUserIndex(nickname) != 0)
                			doMessage("error:auth");
                		else {
                			if (rooms.get(curRoom).isStarted())
                				doMessage("error:start");
                			//else if (rooms.get(curRoom).getNumUsers() < 2)
                				//doMessage("error:notyet");
                			else {
	                			rooms.get(curRoom).gameStart();
	                			rooms.get(curRoom).castChannel("message:Server:게임을 시작합니다.");
                			}
                		}
                	}
                	if (tokens[1].equals("stop")) {
                		if (rooms.get(curRoom).getUserIndex(nickname) != 0)
                			doMessage("error:auth");
                		else {
                			if (!rooms.get(curRoom).isStarted())
                				doMessage("error:notstart");
                			else {
	                			rooms.get(curRoom).gameStop();
	                			rooms.get(curRoom).castChannel("message:Server:게임을 중단합니다.");
                			}
                		}
                	}
                }
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    private synchronized void doJoin(int n) {
    	curRoom = n;
    	if (n == -1) {
    		lobby.addUser(nickname, writer);
    		lobby.castChannel("message:" + nickname + "님이 로비에 입장하셨습니다.");
    		lobby.castChannel("users:" + lobby.getUserList());
    	}
    	else {
    		lobby.delUser(nickname, writer);
    		lobby.castChannel("users:" + lobby.getUserList());
    		doMessage("message:Server:화면을 스크롤하여 하단의 점수판을 사용할 수 있습니다.");
    		rooms.get(n).addUser(nickname, writer);
    		rooms.get(n).castChannel("users:" + rooms.get(n).getUserList());
    		rooms.get(n).castChannel("message:" + nickname + "님이 입장하셨습니다.");
    	}
    }
    
    private synchronized boolean doQuit() {
    	boolean result = false;
    	if (lobby.isUserExist(nickname)) {
		    lobby.delUser(nickname, writer);
		    MainServer.consolelog(nickname + " Disconnect");
		    lobby.castChannel("users:" + lobby.getUserList());
		    result = true;
		    users.remove(nickname);
    	}
    	else {
    		doMessage("chact:1");
    		for (Channel x : rooms)
    			if (x.isUserExist(nickname)) {
    				x.delUser(nickname, writer);
    				x.castChannel("message:" + nickname + "님이 퇴장하셨습니다.");
    				x.castChannel("users:" + x.getUserList());
    				if (x.getNumUsers() == 0) {
    					rooms.remove(x);
    					String str = "rooms:";
                		for (Channel h : rooms) {
                			if (h.isStarted())
                				str = str + h.getName() + "(Playing)" + SPLITER;
                			else
                				str = str + h.getName() + "(" + h.getNumUsers() + "/" + h.getMaxUsers() + ")" + SPLITER;
                		}
                		lobby.castChannel(str);
    				}
    				if (x.isStarted()) {
    					x.castChannel("message:Server:게임 도중 유저가 탈주하여 게임이 중단됩니다.");
    					x.gameStop();
    				}
    				doJoin(-1);
    				break;
    			}
    	}
    	return result;
    }
    
    private void doMessage(String data) {
    	writer.println(data);
    	writer.flush();
    }
    
}