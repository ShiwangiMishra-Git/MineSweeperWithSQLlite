package MineSweeperPackage;




import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

public class MineSweeper extends JFrame implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private static final String menu_NEW_ACTION = "new";
	private static final String menu_SAVE_ACTION = "save";
	private static final String menu_QUIT_ACTION = "quit";
	private JLabel statusbar;

	private JMenuBar menuBar;
	private JMenuItem newMenuItem;
	private JMenuItem saveMenuItem;
	private JMenuItem quitMenuItem;
	JMenu loadMenu;
	JMenu fileMenu;
	JMenu topPlayersMenu;
	JPanel playerPanel;
	private JTextField playerNameField;
	private JPanel boardPanel;
	
	String timertext;
	Timer timer;
	Boolean inGame;
	Boolean toStopTimer;

	HashMap<String, GameState> loadedGames;
	private GameState gameState = new GameState();

	public GameState getGameState() {
		return gameState;
	}

	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}

	public JPanel getBoardPanel() {
		return boardPanel;
	}

	public void setBoardPanel(JPanel boardPanel) {
		this.boardPanel = boardPanel;
	}


	public MineSweeper() {
		initUI();
	}

	public static void main(String[] args) {

		EventQueue.invokeLater(() -> {

			var ex = new MineSweeper();
			ex.setVisible(true);
		});
	}

	private void initUI() {

		this.setBounds(0, 0, 5000, 5000);
		this.setSize(5000, 5000);
		statusbar = new JLabel("");
		add(statusbar, BorderLayout.SOUTH);

		timer = new Timer();
		add(timer, BorderLayout.NORTH);

		buildMenuBar();

		JLabel playerName = new JLabel("Player Name:");
		playerPanel = new JPanel();

		playerPanel.add(playerName);
		playerNameField = new JTextField(10);

		playerNameField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				gameState.setPlayerName(playerNameField.getText());
				System.out.println("Text=" + playerNameField.getText());
			}
		});

		playerPanel.add(playerNameField);

		add(playerPanel, BorderLayout.EAST);
		boardPanel = new Board(statusbar, "new");
		add(boardPanel, BorderLayout.CENTER);

		setResizable(false);
		pack();

		setTitle("Minesweeper");
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}


	private void buildMenuBar() {

		// MenuBar
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");

		// New
		newMenuItem = new JMenuItem("New");
		newMenuItem.setActionCommand(menu_NEW_ACTION);
		newMenuItem.addActionListener(this);
		fileMenu.add(newMenuItem);

		// Save
		saveMenuItem = new JMenuItem("Save");
		saveMenuItem.setActionCommand(menu_SAVE_ACTION);
		saveMenuItem.addActionListener(this);
		fileMenu.add(saveMenuItem);

		// Quit
		quitMenuItem = new JMenuItem("Quit");
		quitMenuItem.setActionCommand(menu_QUIT_ACTION);
		quitMenuItem.addActionListener(this);
		fileMenu.add(quitMenuItem);

		fileMenu.addSeparator();

		// Open
		loadMenu = new JMenu("Open");
		loadMenu.addMenuListener(new LoadMenuListener());

		fileMenu.add(loadMenu);
		fileMenu.addSeparator();

		// Top Players
		topPlayersMenu = new JMenu("Top Players");
		topPlayersMenu.addMenuListener(new LoadTopPlayersMenuListener());
		fileMenu.add(topPlayersMenu);

		menuBar.add(fileMenu);
		// Show the menu
		setJMenuBar(menuBar);

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		switch (e.getActionCommand()) {

		case menu_NEW_ACTION:

			newGame();

			break;

		case menu_SAVE_ACTION:
			try {
				saveGame();
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;

		case menu_QUIT_ACTION:
			System.exit(0);
			break;

		}

		invalidate();
		validate();
		repaint();

	}

	private void newGame() {
		System.out.println("new game called");
		playerNameField.setText("");

		this.gameState = new GameState();
		this.gameState.setTimePassed(0L);
		this.gameState.setInGame(true);
		this.gameState.setToStopTimer(false);

		Container container = this.getContentPane();
		Component component = ((BorderLayout) container.getLayout()).getLayoutComponent(BorderLayout.CENTER);
		if (component != null) {
			container.remove(component);
		}
		container.add(new Board(statusbar, "new"), BorderLayout.CENTER);

		timer.resetCounting();
		this.pack();

	}


	private void saveGame() throws ClassNotFoundException, InterruptedException {

		System.out.println("save called");

		gameState.setPlayerName(playerNameField.getText());

		timer.stopCounting();
		gameState.setTimePassed(1000L - Long.valueOf(timer.getText()));

		if (statusbar.getText() == "Game won") {
			gameState.setNumberOfMines(0);

		}
		if (statusbar.getText() != "Game lost" && statusbar.getText() != "Game won") {
			gameState.setNumberOfMines(Integer.valueOf(statusbar.getText()));

		}

		Socket socket = null;

		try {
			// Establish connection with the server
			socket = new Socket("localhost", 8000);

			// Create an output stream to the server
			try (ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream())) {

				toServer.writeObject(gameState);
				toServer.flush();
				Thread.sleep(1000);
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	class LoadMenuListener implements MenuListener {

		@SuppressWarnings("unchecked")
		@Override
		public void menuSelected(MenuEvent e) {
			System.out.println("load called");

			// TODO Auto-generated method stub
			Socket socket = null;
			ObjectInputStream readFromServer = null;
			try {
				// Establish connection with the server
				socket = new Socket("localhost", 8000);

				// Create an output stream to the server
				ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());

				readFromServer = new ObjectInputStream(socket.getInputStream());

				toServer.writeObject("Get players");
				toServer.flush();
				loadedGames = (HashMap<String, GameState>) readFromServer.readObject();
			}

			catch (IOException ex) {
				ex.printStackTrace();
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}

			loadMenu.removeAll();
			HashMap<String, GameState> toshowgameHM = new HashMap<String, GameState>();
			if (loadedGames == null || loadedGames.size() <= 0) {
				JMenuItem jm = new JMenuItem("No Saved Games!!");
				loadMenu.add(jm);
			} else {

				Map<String, GameState> sortedGameStateMap = loadedGames.entrySet().stream()
						.sorted((e1, e2) -> e1.getValue().getPlayerName().compareTo(e2.getValue().getPlayerName()))
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
								LinkedHashMap::new));

				loadedGames = (HashMap<String, GameState>) sortedGameStateMap;

				if (!(playerNameField.getText() == null || playerNameField.getText().isBlank()
						|| playerNameField.getText().isEmpty())) {

					for (Entry<String, GameState> gs : loadedGames.entrySet()) {
						GameState g = gs.getValue();
						if (g.getPlayerName() != null
								&& (g.getPlayerName().trim().equals(playerNameField.getText().trim()))) {
							toshowgameHM.put(gs.getKey(), gs.getValue());
						}

					}
					if (toshowgameHM == null || toshowgameHM.size() <= 0) {
						JMenuItem jm = new JMenuItem("No Saved Games for player:" + playerNameField.getText() + " !!");
						loadMenu.add(jm);
					} else {
						for (String gs : toshowgameHM.keySet()) {
							System.out.println(gs);
							JMenuItem jm = new JMenuItem(gs);

							jm.addActionListener(event -> {
								System.out.println("selected:" + event.getActionCommand());
								loadBoardBySate(toshowgameHM.get(((JMenuItem) event.getSource()).getText()));
							});

							loadMenu.add(jm);
						}
					}
				} else {
					for (String gs : loadedGames.keySet()) {
						System.out.println(gs);
						JMenuItem jm = new JMenuItem(gs);

						jm.addActionListener(event -> {
							System.out.println("selected:" + event.getActionCommand());
							loadBoardBySate(loadedGames.get(((JMenuItem) event.getSource()).getText()));

						});

						loadMenu.add(jm);
					}

				}

			}

		}

		@Override
		public void menuDeselected(MenuEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void menuCanceled(MenuEvent e) {
			// TODO Auto-generated method stub

		}
	}

	class LoadTopPlayersMenuListener implements MenuListener {

		@SuppressWarnings("unchecked")
		@Override
		public void menuSelected(MenuEvent e) {
			System.out.println("Load Top Players called");

			// TODO Auto-generated method stub
			Socket socket = null;
			ObjectInputStream readFromServer = null;
			try {
				// Establish connection with the server
				socket = new Socket("localhost", 8000);

				// Create an output stream to the server
				try (ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream())) {
					readFromServer = new ObjectInputStream(socket.getInputStream());

					toServer.writeObject("Get players");
					toServer.flush();
					loadedGames = (HashMap<String, GameState>) readFromServer.readObject();
				}

				List<GameState> topPlayers = new LinkedList<GameState>();
				topPlayersMenu.removeAll();
				for (GameState gs : loadedGames.values()) {
					if (gs.isHasWon())
						topPlayers.add(gs);
				}

				if (topPlayers != null && topPlayers.size() > 0) {
					topPlayers = topPlayers.stream().sorted(Comparator.comparingLong(GameState::getTimePassed)).limit(5)
							.collect(Collectors.toList());

					for (GameState gs : topPlayers) {
						System.out.println(gs);
						String palyername = (gs.getPlayerName() == null || gs.getPlayerName().isEmpty()) ? "Unknown"
								: gs.getPlayerName();
						JMenuItem jm = new JMenuItem(
								"Player name: " + palyername + ", Top Score(Time Remaining): " + (1000L-gs.getTimePassed()));

						topPlayersMenu.add(jm);
					}
				} else {

					JMenuItem jm = new JMenuItem("No Top Players!!");
					topPlayersMenu.add(jm);

				}

			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
				if (socket != null) {
					try {
						socket.close();
						// readFromServer.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}

		}

		@Override
		public void menuDeselected(MenuEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void menuCanceled(MenuEvent e) {
			// TODO Auto-generated method stub

		}
	}

	private void loadBoardBySate(GameState gameState) {

		this.gameState = new GameState();
		this.gameState = gameState;

		Container container = this.getContentPane();
		Component component = ((BorderLayout) container.getLayout()).getLayoutComponent(BorderLayout.CENTER);
		if (component != null) {
			container.remove(component);
		}

		statusbar.setText(String.valueOf(gameState.getNumberOfMines()));
		container.add(new Board(statusbar, "load"), BorderLayout.CENTER);

		Component component2 = ((BorderLayout) container.getLayout()).getLayoutComponent(BorderLayout.NORTH);
		if (component2 != null) {
			container.remove(component2);
		}

		timer = new Timer();
		timer.setText(String.valueOf(1000L - getGameState().getTimePassed()));
		container.add(timer, BorderLayout.NORTH);

		Component component3 = ((BorderLayout) container.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
		if (component3 != null) {
			container.remove(component3);
		}

		container.add(statusbar, BorderLayout.SOUTH);

		this.pack();

	}


	public void startTimer() {
		timer.startCounting();
	}

	class Board extends JPanel implements ActionListener {

		private static final long serialVersionUID = 1L;
		private final int borad_NUM_IMAGES = 13;
		private final int board_CELL_SIZE = 30;

		private final int board_COVER_FOR_CELL = 10;
		private final int board_MARK_FOR_CELL = 10;
		private final int board_EMPTY_CELL = 0;
		private final int board_MINE_CELL = 9;
		private final int board_COVERED_MINE_CELL = board_MINE_CELL + board_COVER_FOR_CELL;
		private final int board_MARKED_MINE_CELL = board_COVERED_MINE_CELL + board_MARK_FOR_CELL;

		private final int board_DRAW_MINE = 9;
		private final int board_DRAW_COVER = 10;
		private final int board_DRAW_MARK = 11;
		private final int board_DRAW_WRONG_MARK = 12;

		private final int board_N_MINES = 40;
		private final int board_N_ROWS = 16;
		private final int board_N_COLS = 16;

		private final int board_WIDTH = board_N_COLS * board_CELL_SIZE + 1;
		private final int board_HEIGHT = board_N_ROWS * board_CELL_SIZE + 1;

		private int[] grid_field;
		private boolean player_inGame;
		private int player_minesLeft;
		private Image[] cell_img;

		private int allCells;
		private final JLabel game_statusbar;

		private boolean player_hasWon;
		String timertext = "";

		private boolean timerStarted = false;

		public Board(JLabel statusbar, String source) {

			this.game_statusbar = statusbar;
			if (source == "new")
				initBoard();
			else {
				loadBoard();
			}

		}

		private void initBoard() {

			setPreferredSize(new Dimension(board_WIDTH, board_HEIGHT));

			cell_img = new Image[borad_NUM_IMAGES];

			for (int i = 0; i < borad_NUM_IMAGES; i++) {

				var path = "assets/" + i + ".png";
				cell_img[i] = (new ImageIcon(path)).getImage();
			}
			addMouseListener(new MinesAdapter());
			newGame();
		}

		private void loadBoard() {

			setPreferredSize(new Dimension(board_WIDTH, board_HEIGHT));

			cell_img = new Image[borad_NUM_IMAGES];

			for (int i = 0; i < borad_NUM_IMAGES; i++) {

				var path = "assets/" + i + ".png";
				cell_img[i] = (new ImageIcon(path)).getImage();
			}

			addMouseListener(new MinesAdapter());
			loadGameFromDatabase();
		}

		public void loadGameFromDatabase() {

			player_inGame = getGameState().isInGame(); 
			player_minesLeft = getGameState().getNumberOfMines(); 

			allCells = board_N_ROWS * board_N_COLS;
			grid_field = getGameState().getField(); 

			timerStarted = false;
			timer.setText(String.valueOf(1000L - getGameState().getTimePassed()));


		}

		private void newGame() {

			timerStarted = false;
			timer.resetCounting();
			gameState = new GameState();

		
			player_inGame = true; 
			player_minesLeft = getGameState().getNumberOfMines(); 

			allCells = board_N_ROWS * board_N_COLS;
			grid_field = getGameState().getField(); 

			for (int i = 0; i < allCells; i++) {

				grid_field[i] = board_COVER_FOR_CELL;
			}

			game_statusbar.setText(Integer.toString(player_minesLeft));

			populateCellsWithAdjacentMineNumbers();

		}

		private void populateCellsWithAdjacentMineNumbers() {
			int cell;
			int i = 0;
			var random = new Random();

			while (i < board_N_MINES) {

				int position = (int) (allCells * random.nextDouble());

				if ((position < allCells) && (grid_field[position] != board_COVERED_MINE_CELL)) {

					int current_col = position % board_N_COLS;
					grid_field[position] = board_COVERED_MINE_CELL;
					i++;

					if (current_col > 0) {
						cell = position - 1 - board_N_COLS;
						if (cell >= 0) {
							if (grid_field[cell] != board_COVERED_MINE_CELL) {
								grid_field[cell] += 1;
							}
						}
						cell = position - 1;
						if (cell >= 0) {
							if (grid_field[cell] != board_COVERED_MINE_CELL) {
								grid_field[cell] += 1;
							}
						}

						cell = position + board_N_COLS - 1;
						if (cell < allCells) {
							if (grid_field[cell] != board_COVERED_MINE_CELL) {
								grid_field[cell] += 1;
							}
						}
					}

					cell = position - board_N_COLS;
					if (cell >= 0) {
						if (grid_field[cell] != board_COVERED_MINE_CELL) {
							grid_field[cell] += 1;
						}
					}

					cell = position + board_N_COLS;
					if (cell < allCells) {
						if (grid_field[cell] != board_COVERED_MINE_CELL) {
							grid_field[cell] += 1;
						}
					}

					if (current_col < (board_N_COLS - 1)) {
						cell = position - board_N_COLS + 1;
						if (cell >= 0) {
							if (grid_field[cell] != board_COVERED_MINE_CELL) {
								grid_field[cell] += 1;
							}
						}
						cell = position + board_N_COLS + 1;
						if (cell < allCells) {
							if (grid_field[cell] != board_COVERED_MINE_CELL) {
								grid_field[cell] += 1;
							}
						}
						cell = position + 1;
						if (cell < allCells) {
							if (grid_field[cell] != board_COVERED_MINE_CELL) {
								grid_field[cell] += 1;
							}
						}
					}
				}
			}
		}

		private void uncover_neighbour_cells(int cellNumber) {

			int current_col = cellNumber % board_N_COLS;
			int cell;

			if (current_col > 0) {
				cell = cellNumber - board_N_COLS - 1;
				if (cell >= 0) {
					if (grid_field[cell] > board_MINE_CELL) {
						grid_field[cell] -= board_COVER_FOR_CELL;
						if (grid_field[cell] == board_EMPTY_CELL) {
							uncover_neighbour_cells(cell);
						}
					}
				}

				cell = cellNumber - 1;
				if (cell >= 0) {
					if (grid_field[cell] > board_MINE_CELL) {
						grid_field[cell] -= board_COVER_FOR_CELL;
						if (grid_field[cell] == board_EMPTY_CELL) {
							uncover_neighbour_cells(cell);
						}
					}
				}

				cell = cellNumber + board_N_COLS - 1;
				if (cell < allCells) {
					if (grid_field[cell] > board_MINE_CELL) {
						grid_field[cell] -= board_COVER_FOR_CELL;
						if (grid_field[cell] == board_EMPTY_CELL) {
							uncover_neighbour_cells(cell);
						}
					}
				}
			}

			cell = cellNumber - board_N_COLS;
			if (cell >= 0) {
				if (grid_field[cell] > board_MINE_CELL) {
					grid_field[cell] -= board_COVER_FOR_CELL;
					if (grid_field[cell] == board_EMPTY_CELL) {
						uncover_neighbour_cells(cell);
					}
				}
			}

			cell = cellNumber + board_N_COLS;
			if (cell < allCells) {
				if (grid_field[cell] > board_MINE_CELL) {
					grid_field[cell] -= board_COVER_FOR_CELL;
					if (grid_field[cell] == board_EMPTY_CELL) {
						uncover_neighbour_cells(cell);
					}
				}
			}

			if (current_col < (board_N_COLS - 1)) {
				cell = cellNumber - board_N_COLS + 1;
				if (cell >= 0) {
					if (grid_field[cell] > board_MINE_CELL) {
						grid_field[cell] -= board_COVER_FOR_CELL;
						if (grid_field[cell] == board_EMPTY_CELL) {
							uncover_neighbour_cells(cell);
						}
					}
				}

				cell = cellNumber + board_N_COLS + 1;
				if (cell < allCells) {
					if (grid_field[cell] > board_MINE_CELL) {
						grid_field[cell] -= board_COVER_FOR_CELL;
						if (grid_field[cell] == board_EMPTY_CELL) {
							uncover_neighbour_cells(cell);
						}
					}
				}

				cell = cellNumber + 1;
				if (cell < allCells) {
					if (grid_field[cell] > board_MINE_CELL) {
						grid_field[cell] -= board_COVER_FOR_CELL;
						if (grid_field[cell] == board_EMPTY_CELL) {
							uncover_neighbour_cells(cell);
						}
					}
				}
			}

		}

		@Override
		public void paintComponent(Graphics boardToPaint) {

			int uncover = 0;

			for (int i = 0; i < board_N_ROWS; i++) {

				for (int j = 0; j < board_N_COLS; j++) {

					int cell = grid_field[(i * board_N_COLS) + j];

					if (player_inGame && cell == board_MINE_CELL) {

						player_inGame = false;
					}

					if (!player_inGame) {

						if (cell == board_COVERED_MINE_CELL) {
							cell = board_DRAW_MINE;
						} else if (cell == board_MARKED_MINE_CELL) {
							cell = board_DRAW_MARK;
						} else if (cell > board_COVERED_MINE_CELL) {
							cell = board_DRAW_WRONG_MARK;
						} else if (cell > board_MINE_CELL) {
							cell = board_DRAW_COVER;
						}

					} else {

						if (cell > board_COVERED_MINE_CELL) {
							cell = board_DRAW_MARK;
						} else if (cell > board_MINE_CELL) {
							cell = board_DRAW_COVER;
							uncover++;
						}
					}

					boardToPaint.drawImage(cell_img[cell], (j * board_CELL_SIZE), (i * board_CELL_SIZE), 30, 30, this);
				}
			}
			game_statusbar.setText(String.valueOf(player_minesLeft));
			if (timer.getText() == "0") {
				player_inGame = false;
			}

			if (uncover == 0 && player_inGame) {

				player_inGame = false;
				game_statusbar.setText("Game won");
				gameState.setHasWon(true);

				win();

			} else if (!player_inGame && !gameState.isHasWon()) {
				game_statusbar.setText("Game lost");
				lose();
			}
			if (gameState.isHasWon()) {
				game_statusbar.setText("Game won");

			}

			gameState.setInGame(player_inGame);
			gameState.setField(grid_field);
			gameState.setNumberOfMines(player_minesLeft);
			gameState.setTimePassed(1000L - Long.valueOf(timer.getText()));

		}

		private void win() {

			player_inGame = false;
			player_hasWon = true;
			gameState.setHasWon(player_hasWon);
			game_statusbar.setText("Game won");
			timerStarted = false;
			timer.stopCounting();

		}

		private void lose() {
			timerStarted = false;
			player_inGame = false;
			player_hasWon = false;
			gameState.setHasWon(player_hasWon);
			timer.stopCounting();
			game_statusbar.setText("Game lost");
		}

		private class MinesAdapter extends MouseAdapter {

			@Override
			public void mousePressed(MouseEvent e) {

				if (game_statusbar.getText() == "Game lost" || game_statusbar.getText() == "Game won") {
					player_inGame = false;
				}

				int x = e.getX();
				int y = e.getY();

				int clickCol = x / board_CELL_SIZE;
				int clickRow = y / board_CELL_SIZE;

				boolean doRepaint = false;

				if (!player_inGame) {

					newGame();
					repaint();

				}

				if (!timerStarted) {
					timer.startCounting();
					timerStarted = true;
				}

				if ((x < board_N_COLS * board_CELL_SIZE) && (y < board_N_ROWS * board_CELL_SIZE)) {

					if (e.getButton() == MouseEvent.BUTTON3) {

						if (grid_field[(clickRow * board_N_COLS) + clickCol] > board_MINE_CELL) {

							doRepaint = true;

							if (grid_field[(clickRow * board_N_COLS) + clickCol] <= board_COVERED_MINE_CELL) {

								if (player_minesLeft > 0) {
									grid_field[(clickRow * board_N_COLS) + clickCol] += board_MARK_FOR_CELL;
									player_minesLeft--;
									String msg = Integer.toString(player_minesLeft);
									game_statusbar.setText(msg);
								} else {
									game_statusbar.setText("No marks left");
								}
							} else {

								grid_field[(clickRow * board_N_COLS) + clickCol] -= board_MARK_FOR_CELL;
								player_minesLeft++;
								String msg = Integer.toString(player_minesLeft);
								game_statusbar.setText(msg);
							}
						}

					} else {

						if (grid_field[(clickRow * board_N_COLS) + clickCol] > board_COVERED_MINE_CELL) {

							return;
						}

						if ((grid_field[(clickRow * board_N_COLS) + clickCol] > board_MINE_CELL)
								&& (grid_field[(clickRow * board_N_COLS) + clickCol] < board_MARKED_MINE_CELL)) {

							grid_field[(clickRow * board_N_COLS) + clickCol] -= board_COVER_FOR_CELL;
							doRepaint = true;

							if (grid_field[(clickRow * board_N_COLS) + clickCol] == board_MINE_CELL) {
								player_inGame = false;
							}

							if (grid_field[(clickRow * board_N_COLS) + clickCol] == board_EMPTY_CELL) {
								uncover_neighbour_cells((clickRow * board_N_COLS) + clickCol);
							}
						}
					}

					if (doRepaint) {
						repaint();
					}
				}
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub

		}

	

	}
}