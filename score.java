public class score {
    private String name;
    private int[] scores;
    public score(String name) {
        this.name = name;
        scores = new int[15];
        for (int i = 0; i < 15; i++)
            scores[i] = -1;
        setSubTotal();
        setBonus();
        setTotal();
    }
    public int getScore(int val) { return scores[val]; }
    public void setScore(int val, int score) { scores[val] = score;}
    public String getName() { return name; }
    public void setSubTotal() {
    	int val = 0;
    	for (int i = 0; i < 6; i++)
    		if (scores[i] != -1)
    			val += scores[i];
    	scores[6] = val;
    }
    public int getSubTotal() { return scores[6]; }
    public void setBonus() {
    	if (scores[6] > 62)
    		scores[7] = 35;
    	else
    		scores[7] = 0;
    }
    public int getBonus() { return scores[7]; }
    public void setTotal() {
    	int val = scores[6] + scores[7];
    	for (int i = 8; i < 14; i++)
    		if (scores[i] != -1)
    			val += scores[i];
    	scores[14] = val;
    }
    public int getTotal() { return scores[14]; }
	public boolean equals(score other) { return (getName() == other.getName()); }
}
