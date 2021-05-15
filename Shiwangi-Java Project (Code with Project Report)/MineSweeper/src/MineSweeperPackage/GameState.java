package MineSweeperPackage;


import java.util.List;



public class GameState implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final int N_ROWS = 16;
	private final int N_COLS = 16;
	private final int N_MINES = 40;
	
    int allCells=N_ROWS*N_COLS;
	private int[] field = new int[allCells];
	
	private Long timePassed=0L;
	
	private String playerName;
	
	private List<String> topPlayers;
	
	private Long randomSeed;
	
	private int numberOfMines;
	
	private boolean inGame;
	
	private boolean hasWon;
	
	public boolean isHasWon() {
		return hasWon;
	}

	Boolean toStopTimer;
	public Boolean getToStopTimer() {
		return toStopTimer;
	}


	public void setToStopTimer(Boolean toStopTimer) {
		this.toStopTimer = toStopTimer;
	}


	public void setHasWon(boolean hasWon) {
		this.hasWon = hasWon;
	}


	public int getAllCells() {
		return allCells;
	}


	public void setAllCells(int allCells) {
		this.allCells = allCells;
	}


	public int[] getField() {
		return field;
	}


	public void setField(int[] field) {
		this.field = field;
	}


	public String getPlayerName() {
		return playerName;
	}


	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}


	public List<String> getTopPlayers() {
		return topPlayers;
	}


	public void setTopPlayers(List<String> topPlayers) {
		this.topPlayers = topPlayers;
	}


	public Long getRandomSeed() {
		return randomSeed;
	}


	public void setRandomSeed(Long randomSeed) {
		this.randomSeed = randomSeed;
	}


	public int getNumberOfMines() {
		return numberOfMines;
	}


	public void setNumberOfMines(int numberOfMines) {
		this.numberOfMines = numberOfMines;
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}


	public int getN_ROWS() {
		return N_ROWS;
	}


	public int getN_COLS() {
		return N_COLS;
	}
	public boolean isInGame() {
		return inGame;
	}

	public void setInGame(boolean inGame) {
		this.inGame = inGame;
	}

	public GameState()
	{
		super();
		this.field = new int[allCells];;
		this.numberOfMines = N_MINES;
		this.inGame=true;
	}
	
	public GameState(int allCells, int[] field, Long timePassed, String playerName, List<String> topPlayers,
			Long randomSeed, int numberOfMines) {
		super();
		this.field = field;
		this.numberOfMines = numberOfMines;
		this.playerName = playerName;
		this.timePassed = timePassed;
	}


	public Long getTimePassed() {
		return timePassed;
	}


	public void setTimePassed(Long timePassed) {
		this.timePassed = timePassed;
	}
	
	
}

