package com.github.lorenzopetra96.game;

import java.util.Map;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.github.lorenzopetra96.beans.Challenge;
import com.github.lorenzopetra96.beans.Pair;
import com.github.lorenzopetra96.beans.Player;
import com.github.lorenzopetra96.beans.Sudoku;
import com.github.lorenzopetra96.interfaces.Client;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;


/**
 * docker build --no-cache -t test  .
 * docker run -i -e MASTERIP="127.0.0.1" -e ID=0 test
 * use -i for interactive mode
 * use -e to set the environment variables
 * @author carminespagnuolo
 *
 */
public class SudokuGame {




	private static Screen screen;
	private Client peer;
	private static TextGraphics textGraphics;
	private Terminal terminal;
	private TerminalPosition tp;
	private int row;
	private static SudokuGame game;

	@Option(name="-m", aliases="--masterip", usage="the master peer ip address", required=true)
	private static String master;

	@Option(name="-id", aliases="--identifierpeer", usage="the unique identifier for this peer", required=true)
	private static int id;

	private SudokuGame(String[] args) throws Exception {

		CmdLineParser parser = new CmdLineParser(this); 
		parser.parseArgument(args); 
		System.out.println("\nPeer: " + id + " on Master: " + master + "\n");
		peer = new ClientImpl(master, id);
		terminal = new DefaultTerminalFactory().createTerminal();


		screen = new TerminalScreen(terminal);


		textGraphics = screen.newTextGraphics();

		tp = new TerminalPosition(2,0);

		screen.startScreen();

	}

	public static void main(String[] args) throws Exception {

		game = new SudokuGame(args);
		try {
			
			
			game.home_screen();
			game.choices_screen();
			
		    
		    
		    
		}catch(Exception e) {
			e.printStackTrace();
		}


	}

	public void home_screen() throws Exception{

		System.out.println("Eccomi nella home!");

		try  
		{  




			StringBuffer nickname = new StringBuffer();
			screen.setCursorPosition(tp.withRelativeRow(10).withRelativeColumn(52));

			while(true) {


				textGraphics.setForegroundColor(TextColor.ANSI.WHITE);
				textGraphics.setBackgroundColor(TextColor.ANSI.BLACK);

				create_home_screen();

				textGraphics.putString(2, 10, "Prima di iniziare a giocare, inserisci un nickname: " );

				textGraphics.setForegroundColor(TextColor.ANSI.DEFAULT);
				textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);

				screen.refresh();

				KeyStroke ks = screen.pollInput();
				if (ks == null) {
					Thread.sleep(100);
					continue;
				}
				if(ks.getKeyType() == KeyType.Enter) { 
					if(nickname.length()==0) {
						continue;
					}


					if(peer.checkPlayer(nickname.toString())) break;
					else {
						textGraphics.setForegroundColor(TextColor.ANSI.RED);
						textGraphics.putString(2, 14, "Nickname non disponibile. ", SGR.BOLD, SGR.ITALIC);
						screen.refresh();
					}




				}
				else if(ks.getKeyType() == KeyType.Character) {


					if(nickname.length() == 7) {
						continue;
					}
					nickname.append(ks.getCharacter());
					System.out.println(nickname.toString());
					textGraphics.putString(tp.withRelativeRow(10).withRelativeColumn(52), nickname.toString(), SGR.BOLD);
					screen.setCursorPosition(tp.withRelativeRow(10).withRelativeColumn(52+nickname.length()));

				}
				else if(ks.getKeyType() == KeyType.Backspace && nickname.length()!=0) {

					nickname.deleteCharAt(nickname.length()-1);
					textGraphics.putString(tp.withRelativeRow(10).withRelativeColumn(52), "               ");
					textGraphics.putString(tp.withRelativeRow(10).withRelativeColumn(52), nickname.toString(), SGR.BOLD);
					screen.setCursorPosition(tp.withRelativeRow(10).withRelativeColumn(52+nickname.length()));

				}

				screen.refresh();

			}


		}  
		catch (CmdLineException clEx)  
		{  
			System.err.println("ERROR: Unable to parse command-line options: " + clEx);  
		} 

	}


	public void choices_screen() throws Exception{

		StringBuffer choice = new StringBuffer();
		System.out.println("Eccomi nel tabellone sfide!");
		screen.clear();
		screen.setCursorPosition(tp.withRelativeRow(8).withRelativeColumn(5));
		
		while(true) {
			peer.reloadChallengeList();
			screen.doResizeIfNecessary();
			textGraphics.setForegroundColor(TextColor.ANSI.DEFAULT);
			textGraphics.putString(1, 23, "[PEER: " + id + " - " + peer.getPlayer().getNickname() + "]" , SGR.BOLD);
			
			
			textGraphics.putString(2, 2, "TABELLONE SFIDE", SGR.BOLD);
			textGraphics.putString(2, 4, "Digita '>' insieme al codice partita per partecipare ad una partita. ");
			textGraphics.putString(2, 5, "Digita '@' insieme ad un nuovo codice partita per creare una nuova sfida. ");
			textGraphics.putString(2, 6, "Digita 'exit' per uscire dal gioco. ");

			textGraphics.setCharacter(2,8, Symbols.TRIANGLE_LEFT_POINTING_MEDIUM_BLACK );
			textGraphics.putString(2, 10, "-------------------------------------------------------------------", SGR.BOLD);
			textGraphics.putString(2, 11, " Codice partita             N. Giocatori           Creatore stanza", SGR.BOLD);
			textGraphics.putString(2, 12, "-------------------------------------------------------------------", SGR.BOLD);
			
			svuota(0, 13, 8);
			
			if(!(peer.getChallenges() == null) && !peer.getChallenges().isEmpty() && peer.getChallenges().size()!=0) {
				row = 13;
				
				for(Challenge chall: peer.getChallenges()) {
					textGraphics.putString(3, ++row, chall.getCodice_partita());
					textGraphics.putString(34, row, Integer.toString(chall.getPlayers_scores().size()));
					textGraphics.putString(53, row, chall.getOwner());
				}
			}
			else {
				textGraphics.putString(25, 17, "Non ci sono sfide attive...", SGR.ITALIC);
			}

			screen.refresh();

			KeyStroke ks = screen.pollInput();

			if (ks == null) {
				Thread.sleep(100);
				continue;
			}
			if(ks.getKeyType() == KeyType.Enter) { 

				if(choice.length()<3) {
					textGraphics.setForegroundColor(TextColor.ANSI.RED);
					textGraphics.putString(2, 1, "Comando non valido, inserire almeno 3 caratteri", SGR.BOLD, SGR.ITALIC);
					screen.refresh();
					Thread.sleep(100);
					
					continue;
				}

				char checkC = choice.toString().charAt(0);
				switch (checkC) {
				case '@': {

					String codice_partita = choice.toString().substring(1);
					if(codice_partita.contains(" ")) {
						textGraphics.setForegroundColor(TextColor.ANSI.RED);
						textGraphics.putString(2,1, "                                             ");
						textGraphics.putString(2,1, "Comando non valido. '@' o '>' non specificati", SGR.BOLD, SGR.ITALIC);
						screen.refresh();
						Thread.sleep(500);
						continue;
					}
						
					if(!codice_partita.isEmpty() && peer.generateNewSudoku(codice_partita, 0)) {
						game_screen();
						choice.delete(0, choice.length());
						screen.setCursorPosition(tp.withRelativeRow(8).withRelativeColumn(5));
						screen.clear();
						screen.refresh();
					}
					else {
						textGraphics.setForegroundColor(TextColor.ANSI.RED);
						textGraphics.putString(2, 1, "Codice partita già presente", SGR.BOLD, SGR.ITALIC);
						screen.refresh();
						Thread.sleep(100);
					}
					
					continue;
				}
				case '>': {

					String challenge_name = choice.toString().substring(1);
					
						if(!challenge_name.isEmpty() && peer.joinChallenge(challenge_name)) {
							game_screen();
							choice.delete(0, choice.length());
							screen.setCursorPosition(tp.withRelativeRow(8).withRelativeColumn(5));
							screen.clear();
							screen.refresh();
						}

					continue;
				}
				default: {
					
					if(choice.toString().equals("exit")) {
						game.exit();
						break;
					}

					textGraphics.setForegroundColor(TextColor.ANSI.RED);
					textGraphics.putString(2,1, "                                             ");
					textGraphics.putString(2,1, "Comando non valido. '@' o '>' non specificati", SGR.BOLD, SGR.ITALIC);
					screen.refresh();
					continue;
				}

				}

			}
			else if(ks.getKeyType() == KeyType.Character) {

				textGraphics.putString(2, 1, "                                               ", SGR.BOLD, SGR.ITALIC);
				if(choice.length() == 8) {
					textGraphics.putString(tp.withRelativeRow(24).withRelativeColumn(0), "La lunghezza massima del nickname deve essere di 10 caratteri!", SGR.BOLD);
					continue;
				}
				choice.append(ks.getCharacter());
				System.out.println(choice.toString());

				textGraphics.putString(tp.withRelativeRow(8).withRelativeColumn(5), choice.toString(), SGR.BOLD);
				screen.setCursorPosition(tp.withRelativeRow(8).withRelativeColumn(5 + choice.length()));
			}
			else if(ks.getKeyType() == KeyType.Backspace && choice.length()!=0) {

				choice.deleteCharAt(choice.length()-1);
				System.out.println(choice.toString());

				textGraphics.putString(tp.withRelativeRow(8).withRelativeColumn(5), "               ", SGR.BOLD);
				textGraphics.putString(tp.withRelativeRow(8).withRelativeColumn(5), choice.toString(), SGR.BOLD);
				screen.setCursorPosition(tp.withRelativeRow(8).withRelativeColumn(5 + choice.length()));
			}


		}
	}

	public void game_screen() throws Exception{
		
		int row = 13;
		StringBuffer choice = new StringBuffer();
		screen.clear();
		screen.setCursorPosition(tp.withRelativeRow(6).withRelativeColumn(45));
		while(true) {
			if(!peer.reloadChallenge(peer.getChallenge().getCodice_partita())) choices_screen();
			textGraphics.setForegroundColor(TextColor.ANSI.DEFAULT);
			textGraphics.putString(1, 23, "[PEER: " + id + " - " + peer.getPlayer().getNickname() + "]" , SGR.BOLD);
			
			textGraphics.putString(4, 1, "SUDOKU GAME - " + peer.getChallenge().getCodice_partita(), SGR.BOLD);

			sudoku_screen(peer.getChallenge().getSudoku_board());

			textGraphics.putString(43,2, "COMANDI ", SGR.BOLD);
			textGraphics.putString(43,3, "'XY-N' per inserire N in X,Y.");

			textGraphics.putString(43,4, "'exit' per abbandonare.");
			
			textGraphics.setCharacter(43,6, Symbols.TRIANGLE_LEFT_POINTING_MEDIUM_BLACK );
			
			
			
			textGraphics.putString(43, 10, "-----------------------------", SGR.BOLD);
			textGraphics.putString(43, 11, " Nickname         Punteggio", SGR.BOLD);
			textGraphics.putString(43, 12, "-----------------------------", SGR.BOLD);
			
			
			svuota(43, 14, 7);
			
			if(!peer.getChallenge().isStarted() && peer.getChallenge().getPlayers_scores().size()<2 && !peer.getChallenge().isTerminated()) {
				textGraphics.setForegroundColor(TextColor.ANSI.YELLOW);
				textGraphics.putString(20, 23, "La partita comincerà dall'ingresso del secondo giocatore", SGR.ITALIC);
				continue;
				
			}else if(!peer.getChallenge().isStarted() && peer.getChallenge().getPlayers_scores().size()>1) {
				textGraphics.putString(20, 23, "                                                        ", SGR.ITALIC);
				peer.startChallenge(peer.getChallenge().getCodice_partita());
			}
			else {
				
				if(peer.getChallenge().isStarted() && peer.getChallenge().getPlayers_scores().size()==1) 
					peer.getChallenge().setTerminated(true);
				
				
				if(peer.getChallenge().isTerminated()) {
					
					svuota(43, 14, 7);
					textGraphics.setForegroundColor(TextColor.ANSI.GREEN);
					textGraphics.putString(43, 14, "La partita è terminata. ", SGR.ITALIC);
					if(!peer.getChallenge().isFull()) {
						
						peer.getChallenge().setWinner(new Pair<String,Integer>(peer.getPlayer().getNickname(), peer.getChallenge().getPlayers_scores().get(peer.getPlayer().getNickname())) );
						
					}
					textGraphics.putString(43, 15, peer.getChallenge().getWinner().element0() + " vince con punti pari a " + peer.getChallenge().getWinner().element1(), SGR.ITALIC );
					
				}
				else {
					textGraphics.putString(20, 23, "                                                          ", SGR.ITALIC);
					row=14;
					
					
					for(Map.Entry<String, Integer> set: peer.getChallenge().getPlayers_scores().entrySet()) {
						textGraphics.putString(44, row, set.getKey() );
						textGraphics.putString(65, row, set.getValue().toString(), SGR.BOLD);
						row++;
					}
					
				}
				
				
			}
				
			screen.refresh();
			
			KeyStroke ks = screen.pollInput();

			if (ks == null) {
				Thread.sleep(100);
				continue;
			}
			if(ks.getKeyType() == KeyType.Enter) { 

				if(choice.length()<4) {
					textGraphics.putString(50,8, "                      ", SGR.ITALIC, SGR.BOLD);
					textGraphics.putString(50,8, "Comando non valido!", SGR.BOLD);
					Thread.sleep(100);
					screen.refresh();
					
					
					continue;
				}
				
				if(choice.toString().equals("exit")) {
					
					peer.quitChallenge(peer.getChallenge().getCodice_partita());
					textGraphics.setForegroundColor(TextColor.ANSI.DEFAULT);

					Thread.sleep(200);
					return;
					
				}
				
				if(!checkInput(choice.toString().toUpperCase())){
					textGraphics.putString(50,8, "                      ", SGR.ITALIC, SGR.BOLD);
					textGraphics.putString(50,8, "Comando non valido!", SGR.BOLD);
					Thread.sleep(100);
					screen.refresh();
					continue;
				}
				int x_axes = (choice.toString().toUpperCase().charAt(0)) - 65;
				int y_axes = (choice.toString().toUpperCase().charAt(1)) - 65;
				int value = (choice.toString().toUpperCase().charAt(3)) - 48;
				


				switch(peer.placeNumber(peer.getChallenge().getCodice_partita(), x_axes, y_axes, value)) {
					
				case 1:{
					textGraphics.putString(50,8, "                      ", SGR.ITALIC, SGR.BOLD);
					textGraphics.setForegroundColor(TextColor.ANSI.GREEN);
					textGraphics.putString(50,8, "Valore corretto ", SGR.ITALIC, SGR.BOLD);
					continue;
				}
				case -1:{
					textGraphics.putString(50,8, "                      ", SGR.ITALIC, SGR.BOLD);
					textGraphics.setForegroundColor(TextColor.ANSI.RED);
					textGraphics.putString(50,8, "Valore errato ", SGR.ITALIC, SGR.BOLD);
					continue;
				}
				case 0:{
					textGraphics.putString(50,8, "                      ", SGR.ITALIC, SGR.BOLD);
					textGraphics.setForegroundColor(TextColor.ANSI.DEFAULT);
					textGraphics.putString(50,8, "Valore già presente ", SGR.ITALIC, SGR.BOLD);
					continue;
				}
				default:{
					textGraphics.putString(50,8, "                      ", SGR.ITALIC, SGR.BOLD);
					textGraphics.setForegroundColor(TextColor.ANSI.BLUE);
					textGraphics.putString(50,8, "Valore non inviato ", SGR.ITALIC, SGR.BOLD);
					continue;
				}
				
				}
				
				
			}
			else if(ks.getKeyType() == KeyType.Character) {

				textGraphics.setForegroundColor(TextColor.ANSI.DEFAULT);
				if(choice.length() == 4) {
					continue;
				}
				choice.append(ks.getCharacter());
				System.out.println(choice.toString());

				textGraphics.putString(tp.withRelativeRow(6).withRelativeColumn(45), choice.toString(), SGR.BOLD);
				screen.setCursorPosition(tp.withRelativeRow(6).withRelativeColumn(45 + choice.length()));
			}
			else if(ks.getKeyType() == KeyType.Backspace && choice.length()!=0) {
				textGraphics.setForegroundColor(TextColor.ANSI.DEFAULT);
				choice.deleteCharAt(choice.length()-1);
				System.out.println(choice.toString());

				textGraphics.putString(tp.withRelativeRow(6).withRelativeColumn(45), "               ", SGR.BOLD);
				textGraphics.putString(tp.withRelativeRow(6).withRelativeColumn(45), choice.toString(), SGR.BOLD);
				screen.setCursorPosition(tp.withRelativeRow(6).withRelativeColumn(45 + choice.length()));
			}

		}

	}

	public void create_home_screen() {

		// CREAZIONE 'S'

		textGraphics.setCharacter(2,3, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(3,3, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(4,3, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(5,3, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(6,3, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(2,4, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(2,5, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(3,5, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(4,5, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(5,5, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(6,5, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(6,6, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(6,7, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(5,7, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(4,7, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(3,7, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(2,7, Symbols.TRIANGLE_UP_POINTING_BLACK  );


		// CREAZIONE 'U'
		textGraphics.setCharacter(8,3, Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER);
		textGraphics.setCharacter(8,4, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(8,5, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(8,6, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(8,7, Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER);
		textGraphics.setCharacter(9,7, Symbols.DOUBLE_LINE_HORIZONTAL );
		textGraphics.setCharacter(10,7, Symbols.DOUBLE_LINE_HORIZONTAL );
		textGraphics.setCharacter(11,7, Symbols.DOUBLE_LINE_BOTTOM_RIGHT_CORNER );
		textGraphics.setCharacter(11,6, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(11,5, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(11,4, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(11,3, Symbols.DOUBLE_LINE_TOP_LEFT_CORNER);


		// CREAZIONE 'D'
		textGraphics.setCharacter(13,3, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(13,4, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(13,5, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(13,6, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(13,7, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(14,7, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(15,7, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(16,7, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(17,6, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(17,5, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(17,4, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(16,3, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(15,3, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(14,3, Symbols.TRIANGLE_UP_POINTING_BLACK );


		// CREAZIONE 'O'
		textGraphics.setCharacter(19,3, Symbols.DOUBLE_LINE_TOP_LEFT_CORNER);
		textGraphics.setCharacter(19,4, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(19,5, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(19,6, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(19,7, Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER);
		textGraphics.setCharacter(20,7, Symbols.DOUBLE_LINE_HORIZONTAL );
		textGraphics.setCharacter(21,7, Symbols.DOUBLE_LINE_HORIZONTAL );
		textGraphics.setCharacter(22,7, Symbols.DOUBLE_LINE_BOTTOM_RIGHT_CORNER );
		textGraphics.setCharacter(22,6, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(22,5, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(22,4, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(22,3, Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER);
		textGraphics.setCharacter(21,3, Symbols.DOUBLE_LINE_HORIZONTAL );
		textGraphics.setCharacter(20,3, Symbols.DOUBLE_LINE_HORIZONTAL );

		// CREAZIONE 'K'
		textGraphics.setCharacter(24,3, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(24,4, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(24,5, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(24,6, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(24,7, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(25,5, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(26,4, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(26,6, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(27,3, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(27,7, Symbols.TRIANGLE_UP_POINTING_BLACK );

		// CREAZIONE 'U'
		textGraphics.setCharacter(29,3, Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER);
		textGraphics.setCharacter(29,4, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(29,5, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(29,6, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(29,7, Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER);
		textGraphics.setCharacter(30,7, Symbols.DOUBLE_LINE_HORIZONTAL );
		textGraphics.setCharacter(31,7, Symbols.DOUBLE_LINE_HORIZONTAL );
		textGraphics.setCharacter(32,7, Symbols.DOUBLE_LINE_BOTTOM_RIGHT_CORNER );
		textGraphics.setCharacter(32,6, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(32,5, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(32,4, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(32,3, Symbols.DOUBLE_LINE_TOP_LEFT_CORNER);

		// CREAZIONE 'G'
		textGraphics.setCharacter(40,3, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(40,4, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(40,5, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(40,6, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(40,7, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(41,7, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(42,7, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(43,7, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(43,6, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(43,5, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(43,3, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(42,3, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(41,3, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(42,5, Symbols.TRIANGLE_UP_POINTING_BLACK  );

		// CREAZIONE 'A'
		textGraphics.setCharacter(45,3, Symbols.DOUBLE_LINE_TOP_LEFT_CORNER);
		textGraphics.setCharacter(45,4, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(45,5, Symbols.DOUBLE_LINE_T_RIGHT  );
		textGraphics.setCharacter(45,6, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(45,7, Symbols.DOUBLE_LINE_BOTTOM_RIGHT_CORNER);
		textGraphics.setCharacter(46,3, Symbols.DOUBLE_LINE_HORIZONTAL );
		textGraphics.setCharacter(47,3, Symbols.DOUBLE_LINE_HORIZONTAL );
		textGraphics.setCharacter(48,3, Symbols.DOUBLE_LINE_HORIZONTAL );
		textGraphics.setCharacter(48,7, Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER );
		textGraphics.setCharacter(48,6, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(48,5, Symbols.DOUBLE_LINE_T_LEFT );
		textGraphics.setCharacter(48,4, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(48,3, Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER);
		textGraphics.setCharacter(46,5, Symbols.DOUBLE_LINE_HORIZONTAL );
		textGraphics.setCharacter(47,5, Symbols.DOUBLE_LINE_HORIZONTAL );

		// CREAZIONE 'M'
		textGraphics.setCharacter(50,3, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(50,4, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(50,5, Symbols.TRIANGLE_UP_POINTING_BLACK   );
		textGraphics.setCharacter(50,6, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(50,7, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(54,7, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(54,6, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(54,5, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(54,4, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(54,3, Symbols.TRIANGLE_UP_POINTING_BLACK );
		textGraphics.setCharacter(51,4, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(53,4, Symbols.TRIANGLE_UP_POINTING_BLACK  );
		textGraphics.setCharacter(52,5, Symbols.TRIANGLE_UP_POINTING_BLACK  );

		// CREAZIONE 'E'
		textGraphics.setCharacter(56,3, Symbols.DOUBLE_LINE_TOP_LEFT_CORNER);
		textGraphics.setCharacter(56,4, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(56,5, Symbols.DOUBLE_LINE_T_RIGHT  );
		textGraphics.setCharacter(56,6, Symbols.DOUBLE_LINE_VERTICAL);
		textGraphics.setCharacter(56,7, Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER);
		textGraphics.setCharacter(57,3, Symbols.DOUBLE_LINE_HORIZONTAL );
		textGraphics.setCharacter(58,3, Symbols.DOUBLE_LINE_HORIZONTAL );
		textGraphics.setCharacter(59,3, Symbols.DOUBLE_LINE_HORIZONTAL );
		textGraphics.setCharacter(58,7, Symbols.DOUBLE_LINE_HORIZONTAL );
		textGraphics.setCharacter(57,5, Symbols.DOUBLE_LINE_HORIZONTAL );
		textGraphics.setCharacter(58,5, Symbols.DOUBLE_LINE_HORIZONTAL );
		textGraphics.setCharacter(57,7, Symbols.DOUBLE_LINE_HORIZONTAL );
		textGraphics.setCharacter(59,7, Symbols.DOUBLE_LINE_HORIZONTAL );
		textGraphics.setCharacter(59,5, Symbols.DOUBLE_LINE_HORIZONTAL );



	}

	public static void sudoku_screen(Sudoku sudoku) throws Exception {

		int column=0, row=0, i=0, j=0;
		StringBuilder rows = new StringBuilder();

		sudoku_header(rows, 3, 2);
		rows.delete(0, rows.length());

		for(i=3;i<20;i++) {

			if(i%2==1) sudoku_line(3, i);
			else sudoku_body(rows, 3, i);

			rows.delete(0, rows.length());
		}
		sudoku_footer(rows, 3, 20);
		rows.delete(0, rows.length());
		column = 5;
		row = 3;
		for(i=0;i<9;i++) {
			for(j=0;j<9;j++) {
				textGraphics.putString(column, row, sudoku.getSudoku_sfida()[i][j]!=0 ? Integer.toString(sudoku.getSudoku_sfida()[i][j]) : "", SGR.BOLD);
				column+=4;
			}
			row+=2;
			column = 5;
		}

		sudoku_indexes();


	}

	public static void sudoku_header(StringBuilder rows, int column, int row) {

		rows.append(Symbols.SINGLE_LINE_TOP_LEFT_CORNER);

		for(int i=1;i<9;i++) {

			rows.append(Symbols.SINGLE_LINE_HORIZONTAL);
			rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
			rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
			rows.append(Symbols.SINGLE_LINE_T_DOWN);


		}

		rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
		rows.append(Symbols.SINGLE_LINE_HORIZONTAL);
		rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
		rows.append(Symbols.SINGLE_LINE_TOP_RIGHT_CORNER  );
		textGraphics.putString(column, row, rows.toString());

	}

	public static void sudoku_footer(StringBuilder rows, int column, int row) {

		rows.append(Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER  );
		for(int i=1;i<9;i++) {

			rows.append(Symbols.SINGLE_LINE_HORIZONTAL);
			rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
			rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
			rows.append(Symbols.SINGLE_LINE_T_UP);

		}
		rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
		rows.append(Symbols.SINGLE_LINE_HORIZONTAL);
		rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
		rows.append(Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER    );
		textGraphics.putString(column, row, rows.toString());

	}

	public static void sudoku_body(StringBuilder rows, int column, int row) {

		rows.append(Symbols.SINGLE_LINE_T_RIGHT );
		for(int i=1;i<9;i++) {

			rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
			rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
			rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
			rows.append(Symbols.SINGLE_LINE_CROSS );

		}
		rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
		rows.append(Symbols.SINGLE_LINE_HORIZONTAL);
		rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
		rows.append(Symbols.SINGLE_LINE_T_LEFT );
		textGraphics.putString(column, row, rows.toString());
	}

	public static void sudoku_line(int column, int row) {

		for(int i=0;i<37;i++) {

			if(i==0 || i%4==0 || i==36) textGraphics.setCharacter(i+column,row, Symbols.SINGLE_LINE_VERTICAL );

		}

	}

	public static void sudoku_indexes() {
		char [] index = {'A','B','C','D','E','F','G','H','I'};
		int row_column = 3;
		for(char i: index) {
			textGraphics.setCharacter(1, row_column, i );
			row_column+=2;
		}
		row_column = 5;
		for(char i: index) {
			textGraphics.setCharacter(row_column, 21, i );
			row_column+=4;
		}

	}

	public void exit() throws Exception {
		try {
			
			peer.leaveNetwork();
			peer.shutdown();
			terminal.close();
			screen.close();
			Thread.sleep(300);
			System.exit(0);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public boolean checkInput(String input) {
		return 		(Character.valueOf(input.charAt(0)).compareTo('A')) >= 0 && (Character.valueOf(input.charAt(0)).compareTo('I')) <= 0 
				&&	(Character.valueOf(input.charAt(1)).compareTo('A')) >= 0 && (Character.valueOf(input.charAt(1)).compareTo('I')) <= 0 
				&&	(Character.valueOf(input.charAt(2)).compareTo('-')) == 0 
				&&	(Character.valueOf(input.charAt(3)).compareTo('1')) >= 0 && (Character.valueOf(input.charAt(3)).compareTo('9')) <= 0;
	}
	
	public void svuota(int column, int row, int n) throws Exception {
		for(int i=0; i<n;i++, row++) 
			textGraphics.putString(column, row, "                                                            ");
			
		
		screen.refresh();
	}
	
}
