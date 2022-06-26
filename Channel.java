import java.util.ArrayList;
import java.util.Random;
import java.io.PrintWriter;

public class Channel {
	
	private String name;
	private ArrayList<String> userID;
	private ArrayList<PrintWriter> PrintWriters;
	private int maxUsers;
	private int numUsers;
	private static final String SPLITER = "#!#";
	private int[] diceval;
	ArrayList<score> users;
	private boolean isStart;
	private int turn;
	private int rollcount;
	private int round;
	
	public Channel(String name) {
		this.name = name;
		userID = new ArrayList<String>();
		PrintWriters = new ArrayList<PrintWriter>();
		maxUsers = 4;
		numUsers = 0;
		users = new ArrayList<score>();
		diceval = new int[10];
		isStart = false;
		turn = 0;
		rollcount = 0;
		round = 0;
		for (int i = 0; i < 10; i++)
			diceval[i] = 0;
	}
	
	public void diceFix(int n) {
		diceval[n] = diceval[n + 5];
		diceval[n + 5] = 0;
	}
	
	public void diceUnfix(int n) {
		diceval[n + 5] = diceval[n];
		diceval[n] = 0;
	}
	
	public void addUser(String str, PrintWriter writer) {
		userID.add(str);
		PrintWriters.add(writer);
		numUsers++;
	}
	
	public void delUser(String str, PrintWriter writer) {
		userID.remove(str);
		PrintWriters.remove(writer);
		numUsers--;
	}
	
	public void castChannel(String data) {
        for(PrintWriter writer : PrintWriters) {
            writer.println(data);
            writer.flush();
        }
    }
	
	public String getUserList() {
		String str = "";
		for (String tmp : userID)
			str = str + tmp + SPLITER;
		return str;
	}
	
	public void rollDice() {
		Random random = new Random();
	    random.setSeed(System.currentTimeMillis());
		for (int i = 5; i < 10; i++)
			if (diceval[i - 5] == 0)
				diceval[i] = random.nextInt(6) + 1;
		rollcount++;
	}
	
	public void setScore(int n) {
		users.get(turn).setScore(n, evaluate(n));
		users.get(turn).setSubTotal();
		users.get(turn).setBonus();
		users.get(turn).setTotal();
		for (int i = 0; i < 10; i++)
			diceval[i] = 0;
		turn = (turn + 1) % numUsers;
		rollcount = 0;
		round++;
	}
	
	public int evaluate(int n) {
		int result = 0;
		if (n == 0) {
			for (int i = 0; i < 5; i++)
				if (diceval[i] == 1)
					result += 1;
		}
		else if (n == 1) {
			for (int i = 0; i < 5; i++)
				if (diceval[i] == 2)
					result += 2;
		}
		else if (n == 2) {
			for (int i = 0; i < 5; i++)
				if (diceval[i] == 3)
					result += 3;
		}
		else if (n == 3) {
			for (int i = 0; i < 5; i++)
				if (diceval[i] == 4)
					result += 4;
		}
		else if (n == 4) {
			for (int i = 0; i < 5; i++)
				if (diceval[i] == 5)
					result += 5;
		}
		else if (n == 5) {
			for (int i = 0; i < 5; i++)
				if (diceval[i] == 6)
					result += 6;
		}
		else if (n == 8) { // Choice
			for (int i = 0; i < 5; i++)
				result += diceval[i];
		}
		else if (n == 9) { // 4 of a kind
			if (isFourKind())
				for (int i = 0; i < 5; i++)
					result += diceval[i];
		}
		else if (n == 10) { // Full House
			if (isFullHouse())
				for (int i = 0; i < 5; i++)
					result += diceval[i];
		}
		else if (n == 11) { // S. Straight
			if (isSmallStraight())
				result = 15;
		}
		else if (n == 12) { // L. Straight
			if (isLargeStraight())
				result = 30;
		}
		else { // Yacht
			if (diceval[0] == diceval[1] && diceval[0] == diceval[2] && diceval[0] == diceval[3] && diceval[0] == diceval[4])
				result = 50;
		}
		
		return result;
	}
	
	private boolean isFourKind() {
		int swap;
		int[] tmp = new int[5];
		for (int i = 0; i < 5; i++)
			tmp[i] = diceval[i];
		
		for (int i = 0; i < 4; i++)
			for (int j = i; j < 5; j++)
				if (tmp[i] > tmp[j])
				{
					swap = tmp[i];
					tmp[i] = tmp[j];
					tmp[j] = swap;
				}
		
		if ((tmp[0] == tmp[1] && tmp[0] == tmp[2] && tmp[0] == tmp[3]) || (tmp[1] == tmp[2] && tmp[1] == tmp[3] && tmp[1] == tmp[4]))
			return true;
		else
			return false;
	}
	
	private boolean isFullHouse() {
		int swap;
		int[] tmp = new int[5];
		for (int i = 0; i < 5; i++)
			tmp[i] = diceval[i];
		
		for (int i = 0; i < 4; i++)
			for (int j = i; j < 5; j++)
				if (tmp[i] > tmp[j]) {
					swap = tmp[i];
					tmp[i] = tmp[j];
					tmp[j] = swap;
				}
		
		if ((tmp[0] == tmp[1] && tmp[0] == tmp[2] && tmp[3] == tmp[4]) || (tmp[0] == tmp[1] && tmp[2] == tmp[3] && tmp[2] == tmp[4]))
			return true;
		else
			return false;
	}
	
	private boolean isSmallStraight() {
		int swap;
		int[] tmp = new int[5];
		for (int i = 0; i < 5; i++)
			tmp[i] = diceval[i];
		
		for (int i = 0; i < 4; i++)
			for (int j = i; j < 5; j++)
				if (tmp[i] > tmp[j]) {
					swap = tmp[i];
					tmp[i] = tmp[j];
					tmp[j] = swap;
				}
		
		for (int i = 0; i < 4; i++)
			if (tmp[i] == tmp[i + 1])
				for (int j = i + 1; j < 4; j++)
					tmp[j] = tmp [j + 1];
		
		if (((tmp[3] == tmp[2] + 1) && (tmp[2] == tmp[1] + 1) && (tmp[1] == tmp[0] + 1)) || ((tmp[4] == tmp[3] + 1) && (tmp[3] == tmp[2] + 1) && (tmp[2] == tmp[1] + 1)))
			return true;
		else
			return false;
	}
	
	private boolean isLargeStraight() {
		int swap;
		int[] tmp = new int[5];
		for (int i = 0; i < 5; i++)
			tmp[i] = diceval[i];
		
		for (int i = 0; i < 4; i++)
			for (int j = i; j < 5; j++)
				if (tmp[i] > tmp[j]) {
					swap = tmp[i];
					tmp[i] = tmp[j];
					tmp[j] = swap;
				}
		
		if ((tmp[0] == 1 && tmp[1] == 2 && tmp[2] == 3 && tmp[3] == 4 && tmp[4] == 5) || (tmp[0] == 2 && tmp[1] == 3 && tmp[2] == 4 && tmp[3] == 5 && tmp[4] == 6))
			return true;
		else
			return false;
	}
	
	public String getWinner() {
		String str = "";
		int maxScore = -1, lastIdx = -1;
		for (score x : users) {
			maxScore = x.getTotal() > maxScore ? x.getTotal() : maxScore;
			if (maxScore == x.getTotal())
				lastIdx = users.indexOf(x);
		}
		for (score x : users)
			if (x.getTotal() == maxScore) {
				if (users.indexOf(x) == lastIdx)
					str = str + x.getName();
				else
					str = str + x.getName() + ", ";
			}
		gameStop();
		return str;
	}
	
	public String printGameInfo() {
		String str = "game:";
		for (int i = 0; i < 10; i++)
			str = str + diceval[i] + SPLITER;
		for (score x : users)
			for (int i = 0; i < 15; i++)
				str = str + x.getScore(i) + SPLITER;
		str = str + rollcount + SPLITER + turn;
		return str;
	}
	
	public void gameStart() {
		isStart = true;
		turn = 0;
		rollcount = 0;
		round = 0;
		for (int i = 0; i < 10; i++)
			diceval[i] = 0;
		users.clear();
		for (String x : userID)
			users.add(new score(x));
		castChannel("game:start");
	}
	
	public void gameStop() {
		isStart = false;
		castChannel("game:reset");
	}
	
	public String getName() { return name; }
	public int getMaxUsers() { return maxUsers; }
	public int getNumUsers() { return numUsers; }
	public boolean isConnectable() { return numUsers < maxUsers; }
	public boolean isUserExist(String str) { return userID.indexOf(str) != -1; }
	public boolean isUserExist(PrintWriter writer) { return PrintWriters.indexOf(writer) != -1; }
	public boolean isStarted() { return isStart; }
	public boolean isRollable() { return rollcount < 3; }
	public boolean isFinished() { return round == 12 * numUsers; }
	public int getTurn() { return turn; }
	public boolean isFixable(int n) { return diceval[n] == 0; }
	public boolean isUnfixable(int n) { return diceval[n + 5] == 0; }
	public boolean isSetable(int id, int val) { return (diceval[0] != 0 && diceval[1] != 0 && diceval[2] != 0 && diceval[3] != 0 && diceval[4] != 0 && users.get(id).getScore(val) == -1); }
	public int getUserIndex(String name) { return userID.indexOf(name); }
	public String getUserName(int n) { return userID.get(n); }
}
