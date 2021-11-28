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
	private int[] user1val;
	private int[] user2val;
	private int[] user1tmp;
	private int[] user2tmp;
	private boolean isStart;
	private int turn;
	private int rollcount;
	private int round;
	
	public Channel(String name)
	{
		this.name = name;
		userID = new ArrayList<String>();
		PrintWriters = new ArrayList<PrintWriter>();
		maxUsers = 2;
		numUsers = 0;
		diceval = new int[10];
		user1val = new int[15];
		user2val = new int[15];
		user1tmp = new int[15];
		user2tmp = new int[15];
		isStart = false;
		turn = 0;
		rollcount = 0;
		round = 0;
		for (int i = 0; i < 15; i++)
		{
			user1val[i] = 0;
			user2val[i] = 0;
			user1tmp[i] = 0;
			user2tmp[i] = 0;
			if (i < 10)
				diceval[i] = 0;
		}
	}
	
	public void diceFix(int n)
	{
		diceval[n] = diceval[n + 5];
		diceval[n + 5] = 0;
	}
	
	public void diceUnfix(int n)
	{
		diceval[n + 5] = diceval[n];
		diceval[n] = 0;
	}
	
	public void addUser(String str, PrintWriter writer)
	{
		userID.add(str);
		PrintWriters.add(writer);
		numUsers++;
	}
	
	public void delUser(String str, PrintWriter writer)
	{
		userID.remove(str);
		PrintWriters.remove(writer);
		numUsers--;
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
	
	public void rollDice()
	{
		rollcount++;
		Random random = new Random();
	    random.setSeed(System.currentTimeMillis());
		for (int i = 5; i < 10; i++)
			if (diceval[i - 5] == 0)
				diceval[i] = random.nextInt(6) + 1;
	}
	
	public void setScore(int n)
	{
		if (turn == 0)
		{
			user1val[n] = evaluate(n);
			user1val[6] = user1val[0] + user1val[1] + user1val[2] + user1val[3] + user1val[4] + user1val[5];
			user1val[7] = user1val[6] >= 63 ? 35 : 0;
			user1val[14] = user1val[6] + user1val[7] + user1val[8] + user1val[9] + user1val[10] + user1val[11] + user1val[12] + user1val[13];
		}
		else
		{
			user2val[n] = evaluate(n);
			user2val[6] = user2val[0] + user2val[1] + user2val[2] + user2val[3] + user2val[4] + user2val[5];
			user2val[7] = user2val[6] >= 63 ? 35 : 0;
			user2val[14] = user2val[6] + user2val[7] + user2val[8] + user2val[9] + user2val[10] + user2val[11] + user2val[12] + user2val[13];
		}
		for (int i = 0; i < 10; i++)
			diceval[i] = 0;
		for (int i = 0; i < 15; i++)
		{
			user1tmp[i] = 0;
			user2tmp[i] = 0;
		}
		turn = turn == 0 ? 1 : 0;
		rollcount = 0;
		round++;
	}
	
	public void setTmpScore()
	{
		for (int i = 0; i < 15; i++)
		{
			user1tmp[i] = user1val[i];
			user2tmp[i] = user2val[i];
			if (turn == 0)
			{
				if (i != 6 && i != 7 && i != 14 && user1tmp[i] != 0)
					user1tmp[i] = evaluate(i);
			}
			else
			{
				if (i != 6 && i != 7 && i != 14 && user2tmp[i] != 0)
					user2tmp[i] = evaluate(i);
			}
		}
	}
	
	public int evaluate(int n)
	{
		int result = 0;
		if (n == 0)
		{
			for (int i = 0; i < 5; i++)
				if (diceval[i] == 1)
					result += 1;
		}
		else if (n == 1)
		{
			for (int i = 0; i < 5; i++)
				if (diceval[i] == 2)
					result += 2;
		}
		else if (n == 2)
		{
			for (int i = 0; i < 5; i++)
				if (diceval[i] == 3)
					result += 3;
		}
		else if (n == 3)
		{
			for (int i = 0; i < 5; i++)
				if (diceval[i] == 4)
					result += 4;
		}
		else if (n == 4)
		{
			for (int i = 0; i < 5; i++)
				if (diceval[i] == 5)
					result += 5;
		}
		else if (n == 5)
		{
			for (int i = 0; i < 5; i++)
				if (diceval[i] == 6)
					result += 6;
		}
		else if (n == 8)
		{ // Choice
			for (int i = 0; i < 5; i++)
				result += diceval[i];
		}
		else if (n == 9)
		{ // 4 of a kind
			if (isFourKind())
				for (int i = 0; i < 5; i++)
					result += diceval[i];
		}
		else if (n == 10)
		{ // Full House
			if (isFullHouse())
				for (int i = 0; i < 5; i++)
					result += diceval[i];
		}
		else if (n == 11)
		{ // S. Straight
			if (isSmallStraight())
				result = 15;
		}
		else if (n == 12)
		{ // L. Straight
			if (isLargeStraight())
				result = 30;
		}
		else
		{ // Yacht
			if (diceval[0] == diceval[1] && diceval[0] == diceval[2] && diceval[0] == diceval[3] && diceval[0] == diceval[4])
				result = 50;
		}
		
		return result;
	}
	
	private boolean isFourKind()
	{
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
	
	private boolean isFullHouse()
	{
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
		
		if ((tmp[0] == tmp[1] && tmp[0] == tmp[2] && tmp[3] == tmp[4]) || (tmp[0] == tmp[1] && tmp[2] == tmp[3] && tmp[2] == tmp[4]))
			return true;
		else
			return false;
	}
	
	private boolean isSmallStraight()
	{
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
		
		int count = 0;
		
		for (int i = 0; i< 4; i++)
		{
			if (tmp[i] == tmp[i + 1] || tmp[i] == (tmp[i + 1] - 1))
			{
				count++;
				if (count >= 3)
					break;
			}
			else
				count = 0;
		}
		
		if (count >= 3)
			return true;
		else
			return false;
	}
	
	private boolean isLargeStraight()
	{
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
		
		if ((tmp[0] == 1 && tmp[1] == 2 && tmp[2] == 3 && tmp[3] == 4 && tmp[4] == 5) || (tmp[0] == 2 && tmp[1] == 3 && tmp[2] == 4 && tmp[3] == 5 && tmp[4] == 6))
			return true;
		else
			return false;
	}
	
	public int getWinner()
	{
		int result = user1val[14] > user2val[14] ? 0 : 1;
		if (user1val[14] == user2val[14])
			result = 2;
		gameStop();
		return result;
	}
	
	public String printGameInfo()
	{
		String str = "game:";
		for (int i = 0; i < 10; i++)
			str = str + diceval[i] + SPLITER;
		for (int i = 0; i < 15; i++)
			str = str + user1val[i] + SPLITER;
		for (int i = 0; i < 15; i++)
			str = str + user2val[i] + SPLITER;
		str = str + rollcount;
		return str;
	}
	
	public String printTmpInfo()
	{
		setTmpScore();
		String str = "game:";
		for (int i = 0; i < 10; i++)
			str = str + diceval[i] + SPLITER;
		for (int i = 0; i < 15; i++)
			str = str + user1tmp[i] + SPLITER;
		for (int i = 0; i < 15; i++)
			str = str + user2tmp[i] + SPLITER;
		str = str + rollcount;
		return str;
	}
	
	public void gameStart()
	{
		isStart = true;
		turn = 0;
		rollcount = 0;
		round = 0;
		for (int i = 0; i < 15; i++)
		{
			user1val[i] = 0;
			user2val[i] = 0;
			user1tmp[i] = 0;
			user2tmp[i] = 0;
			if (i < 10)
				diceval[i] = 0;
		}
		castChannel(printGameInfo());
		castChannel("game:reset");
	}
	
	public void gameStop() { isStart = false; }
	
	public String getName() { return name; }
	public int getMaxUsers() { return maxUsers; }
	public int getNumUsers() { return numUsers; }
	public boolean isConnectable() { return numUsers < maxUsers; }
	public boolean isUserExist(String str) { return userID.indexOf(str) != -1; }
	public boolean isUserExist(PrintWriter writer) { return PrintWriters.indexOf(writer) != -1; }
	public boolean isStarted() { return isStart; }
	public boolean isRollable() { return rollcount < 3; }
	public boolean isFinished() { return round == 24; }
	public int getTurn() { return turn; }
	public boolean isFixable(int n) { return diceval[n] == 0; }
	public boolean isUnfixable(int n) { return diceval[n + 5] == 0; }
	public boolean isSetable() { return (diceval[0] != 0 && diceval[1] != 0 && diceval[2] != 0 && diceval[3] != 0 && diceval[4] != 0); }
	public int getUserIndex(String name) { return userID.indexOf(name); }
	public String getUserName(int n) { return userID.get(n); }
}
