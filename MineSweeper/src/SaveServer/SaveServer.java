package SaveServer;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import MineSweeperPackage.GameState;





public class SaveServer extends JFrame implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ObjectInputStream inputFromClient;
	private ObjectOutputStream outputToClient;
	// Text area for displaying contents
	JTextArea ta;
	
	private Connection conn;
	private PreparedStatement queryStmtAllRows;
	private PreparedStatement insertStatement;
	
	public SaveServer() {
		super("Server");
		ta = new JTextArea();
		this.add(ta);

		setSize(400, 200);
		
		try {
		
	        conn = DriverManager.getConnection("jdbc:sqlite:MineSweeper.db");
			queryStmtAllRows = conn.prepareStatement("Select * from ms_history order by saved_ts desc");		
			insertStatement = conn.prepareStatement("INSERT INTO ms_history (name, game_state_data,saved_ts) Values (?, ?,datetime('now', 'localtime'))");
			
		} catch (SQLException e) {
			System.err.println("Connection error: " + e);
			System.exit(1);
		}
		Thread t = new Thread(this);
		t.start();
	}

	@Override
	public void run() {
		ServerSocket serverSocket = null;

		try {
			 serverSocket = new ServerSocket(8000);
			
			System.out.println("Server started ");


			while (true) {
				// Listen for a new connection request
				Socket socket = serverSocket.accept();

				// Create an input stream from the socket
				inputFromClient =
						new ObjectInputStream(socket.getInputStream());
				outputToClient = new ObjectOutputStream(
				          socket.getOutputStream());
				// Read from input
				Object object = inputFromClient.readObject();
				
				// Write to the file
				if(object instanceof GameState) {
					GameState g = (GameState)object;
					ta.append("Save player" + g.getPlayerName()+"\n");
					
					PreparedStatement ins = insertStatement;
					try {
						ins.setString(1, g.getPlayerName());
						ins.setString(2, toString(g));
						ins.execute();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					
					outputToClient.writeObject(retriveAll());
					outputToClient.flush();
					//outputToClient.close();
				}	        

			}
		}
		catch(IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			/*
			 * try { serverSocket.close(); } catch (IOException e) { // TODO Auto-generated
			 * catch block e.printStackTrace(); }
			 */
		}
	}

	/**
	 * The main method is only needed for the IDE with limited JavaFX support. Not
	 * needed for running from the command line.
	 */
	public static void main(String[] args) {
		SaveServer s = new SaveServer();
		s.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		s.setVisible(true);
	}
	
	private HashMap<String,GameState> retriveAll() {
		
		HashMap<String,GameState> hashmapGames = new HashMap<>();
		PreparedStatement stmt = queryStmtAllRows;
			
			ResultSet rset;
			try {
				rset = stmt.executeQuery();
				while (rset.next()) {
					if(rset.getString("name")!=null && !rset.getString("name").isEmpty())
					hashmapGames.put("Player name:"+rset.getString("name")+" , Game saved at:"+rset.getString("saved_ts")  ,(GameState)fromString(rset.getString("game_state_data")));
					else {
						hashmapGames.put("Player name: unknown , Game saved at:"+rset.getString("saved_ts")  ,(GameState)fromString(rset.getString("game_state_data")));	
					}
				}
				//System.out.print("rowString  is  " + rowString);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return hashmapGames;
	}
	
	
	/** Read the object from Base64 string. */
	private static Object fromString(String s) throws IOException, ClassNotFoundException {
		byte[] data = Base64.getDecoder().decode(s);
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
		Object o = ois.readObject();
		ois.close();
		return o;
	}

	/** Write the object to a Base64 string. */
	private static String toString(Serializable o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}

}


