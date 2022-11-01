package com.github.lorenzopetra96.game;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.beryx.textio.StringInputReader;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;

import com.github.lorenzopetra96.beans.Challenge;
import com.github.lorenzopetra96.beans.Pair;
import com.github.lorenzopetra96.beans.Player;
import com.github.lorenzopetra96.beans.Sudoku;
import com.github.lorenzopetra96.interfaces.Client;

import java.awt.Robot;
import java.awt.event.KeyEvent;


/**
 * docker build --no-cache -t test  .
 * docker run -i -e MASTERIP="127.0.0.1" -e ID=0 test
 * use -i for interactive mode
 * use -e to set the environment variables
 * @author carminespagnuolo
 *
 */
public class SudokuGame {


	private TextIO textIO = TextIoFactory.getTextIO();
	private TextTerminal terminal = textIO.getTextTerminal();
	private Client peer;
	



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
		peer = new ClientImpl(master, id, new MessageListener());


	}
	
	class MessageListener{

		public MessageListener(){}

		public Object parseMessage(Object obj) throws Exception {
			
			System.out.println("OBJ: " + obj.getClass().toString() + " | CHA: " + peer.getChallenges().getClass().toString());
			if(obj.getClass().equals(peer.getChallenges().getClass())) {
				peer.setChallenges((ArrayList<Challenge>) obj);
				if(peer.getChallenge() == null) {
					Robot robot = new Robot();
					terminal.resetLine();
					terminal.getProperties().setPromptColor("yellow");
					terminal.println("Aggiornando lista partite...");
					terminal.getProperties().setPromptColor("white");
					Thread.sleep(1000);
					robot.keyPress(KeyEvent.VK_I);
			        robot.keyRelease(KeyEvent.VK_I);
					
					
				}
			}
			else if(obj.getClass().equals(peer.getChallenge().getClass())) {
				
				peer.setChallenge((Challenge) obj);
				
			}
			return "success";
		}
		

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


		terminal.getProperties().setPromptColor("white");


		while(true) {

			terminal.setBookmark("BOOKMARK");
			terminal.println(" ");
			create_home_screen();



			String nickname = textIO.newStringInputReader()
					.read("   Inserisci un nickname: ");
			
			

			if(peer.checkPlayer(nickname.toString()) && nickname.length()>2 && nickname.length()<8) {
				terminal.resetToBookmark("BOOKMARK");
				break;
			}
			else {
				terminal.println("  ");
				terminal.println("  ");
				terminal.getProperties().setPromptColor("red");
				terminal.println("  Nickname non disponibile");
				Thread.sleep(1000);
				terminal.getProperties().setPromptColor("white");
				terminal.resetToBookmark("BOOKMARK");
			}

		}
	}




	public void choices_screen() throws Exception{
		
		String choice;
		String codice_partita_0;
		while(true) {
			terminal.setBookmark("TABELLONE");
			peer.reloadChallengeList();
			
			terminal.println("[PEER " + id + " | " + peer.getPlayer().getNickname() + "]");
			
			terminal.println("\n\n-------------------------------------------------------------------");
			terminal.println(" Codice partita             N. Giocatori           Creatore stanza");
			terminal.println("-------------------------------------------------------------------");
			
			
			if(!(peer.getChallenges() == null) && !peer.getChallenges().isEmpty() && peer.getChallenges().size()!=0) {
				
				for(Challenge chall: peer.getChallenges()) {
					codice_partita_0 = chall.getCodice_partita();
					if(codice_partita_0.length() <7) {
						for(int i = (7-(codice_partita_0.length()));i<7; i++ ) codice_partita_0 += " ";
					}
					terminal.println("\n  " + codice_partita_0 + "\t\t\t" + Integer.toString(chall.getPlayers_scores().size()) + "\t\t\t" + chall.getOwner());
				}
			}
			else {
				terminal.print("\n\n\t\t\tNon ci sono sfide attive...");
			}
			
			choice = textIO.newStringInputReader()
					.read("\n\n\n   TABELLONE SFIDE\n\n  Digita '>' e un codice partita per partecipare ad una partita.\n"
							+ "  Digita '@' e un nuovo codice partita per creare una nuova sfida.\n  Digita 'exit' per uscire dal gioco.\n\n"
							+ "  >   ");
			
			if(choice.contains(" refresh! ")) {
				
				Thread.sleep(300);
				
				continue;
			}
			terminal.resetToBookmark("TABELLONE");
			char checkC = choice.toString().charAt(0);
			switch (checkC) {
			case '@': {

				String codice_partita = choice.toString().substring(1);
				if(codice_partita.contains(" ") || codice_partita.isEmpty()) {
					terminal.println("  ");
					terminal.println("  ");
					terminal.getProperties().setPromptColor("red");
					terminal.println("  Codice partita non valido");
					Thread.sleep(1000);
					terminal.getProperties().setPromptColor("white");
					terminal.resetToBookmark("BOOKMARK");
					continue;
				}

				if(peer.generateNewSudoku(codice_partita, 0)) {
					game_screen();
					terminal.resetToBookmark("BOOKMARK");
					continue;
				}
				else {
					terminal.println("  ");
					terminal.println("  ");
					terminal.getProperties().setPromptColor("red");
					terminal.println("  Codice partita già presente");
					Thread.sleep(1000);
					terminal.getProperties().setPromptColor("white");
					terminal.resetToBookmark("BOOKMARK");
					continue;
				}
			}
			case '>': {

				String challenge_name = choice.toString().substring(1);
				
				if(challenge_name.isEmpty() || challenge_name.contains(" ")) {
					terminal.println("  ");
					terminal.println("  ");
					terminal.getProperties().setPromptColor("red");
					terminal.println("  Codice partita non valido");
					Thread.sleep(1000);
					terminal.getProperties().setPromptColor("white");
					terminal.resetToBookmark("BOOKMARK");
					continue;
				}
				else if(peer.joinChallenge(challenge_name)) {
					game_screen();
					terminal.resetToBookmark("BOOKMARK");
					continue;
				}
				else {
					terminal.println("  ");
					terminal.println("  ");
					terminal.getProperties().setPromptColor("red");
					terminal.println("  Codice partita non presente");
					Thread.sleep(1000);
					terminal.getProperties().setPromptColor("white");
					terminal.resetToBookmark("BOOKMARK");
				}

				continue;
			}
			default: {

				if(choice.toString().equals("exit")) {
					game.exit();
					break;
				}
			}
			}
			terminal.resetToBookmark("BOOKMARK");
			
		}

		
	}

	public void game_screen() throws Exception{

		
		
		while(true) {
			
			terminal.println("GAME SCREEN");
			terminal.resetToBookmark("TABELLONE");
		}
		//		int row = 13;
		//		StringBuffer choice = new StringBuffer();
		//		screen.clear();
		//		screen.setCursorPosition(tp.withRelativeRow(6).withRelativeColumn(45));
		//		while(true) {
		//			if(!peer.reloadChallenge(peer.getChallenge().getCodice_partita())) choices_screen();
		//			textGraphics.setForegroundColor(TextColor.ANSI.DEFAULT);
		//			textGraphics.putString(1, 23, "[PEER: " + id + " - " + peer.getPlayer().getNickname() + "]" , SGR.BOLD);
		//			
		//			textGraphics.putString(4, 1, "SUDOKU GAME - " + peer.getChallenge().getCodice_partita(), SGR.BOLD);
		//
		//			sudoku_screen(peer.getChallenge().getSudoku_board());
		//
		//			textGraphics.putString(43,2, "COMANDI ", SGR.BOLD);
		//			textGraphics.putString(43,3, "'XY-N' per inserire N in X,Y.");
		//
		//			textGraphics.putString(43,4, "'exit' per abbandonare.");
		//			
		//			textGraphics.setCharacter(43,6, Symbols.TRIANGLE_LEFT_POINTING_MEDIUM_BLACK );
		//			
		//			
		//			
		//			textGraphics.putString(43, 10, "-----------------------------", SGR.BOLD);
		//			textGraphics.putString(43, 11, " Nickname         Punteggio", SGR.BOLD);
		//			textGraphics.putString(43, 12, "-----------------------------", SGR.BOLD);
		//			
		//			
		//			svuota(43, 14, 7);
		//			
		//			if(!peer.getChallenge().isStarted() && peer.getChallenge().getPlayers_scores().size()<2 && !peer.getChallenge().isTerminated()) {
		//				textGraphics.setForegroundColor(TextColor.ANSI.YELLOW);
		//				textGraphics.putString(20, 23, "La partita comincerà dall'ingresso del secondo giocatore", SGR.ITALIC);
		//				continue;
		//				
		//			}else if(!peer.getChallenge().isStarted() && peer.getChallenge().getPlayers_scores().size()>1) {
		//				textGraphics.putString(20, 23, "                                                        ", SGR.ITALIC);
		//				peer.startChallenge(peer.getChallenge().getCodice_partita());
		//			}
		//			else {
		//				
		//				if(peer.getChallenge().isStarted() && peer.getChallenge().getPlayers_scores().size()==1) 
		//					peer.getChallenge().setTerminated(true);
		//				
		//				
		//				if(peer.getChallenge().isTerminated()) {
		//					
		//					svuota(43, 14, 7);
		//					textGraphics.setForegroundColor(TextColor.ANSI.GREEN);
		//					textGraphics.putString(43, 14, "La partita è terminata. ", SGR.ITALIC);
		//					if(!peer.getChallenge().isFull()) {
		//						
		//						peer.getChallenge().setWinner(new Pair<String,Integer>(peer.getPlayer().getNickname(), peer.getChallenge().getPlayers_scores().get(peer.getPlayer().getNickname())) );
		//						
		//					}
		//					textGraphics.putString(43, 15, peer.getChallenge().getWinner().element0() + " vince con punti pari a " + peer.getChallenge().getWinner().element1(), SGR.ITALIC );
		//					
		//				}
		//				else {
		//					textGraphics.putString(20, 23, "                                                          ", SGR.ITALIC);
		//					row=14;
		//					
		//					
		//					for(Map.Entry<String, Integer> set: peer.getChallenge().getPlayers_scores().entrySet()) {
		//						textGraphics.putString(44, row, set.getKey() );
		//						textGraphics.putString(65, row, set.getValue().toString(), SGR.BOLD);
		//						row++;
		//					}
		//					
		//				}
		//				
		//				
		//			}
		//				
		//			screen.refresh();
		//			
		//			KeyStroke ks = screen.pollInput();
		//
		//			if (ks == null) {
		//				Thread.sleep(100);
		//				continue;
		//			}
		//			if(ks.getKeyType() == KeyType.Enter) { 
		//
		//				if(choice.length()<4) {
		//					textGraphics.putString(50,8, "                      ", SGR.ITALIC, SGR.BOLD);
		//					textGraphics.putString(50,8, "Comando non valido!", SGR.BOLD);
		//					Thread.sleep(100);
		//					screen.refresh();
		//					
		//					
		//					continue;
		//				}
		//				
		//				if(choice.toString().equals("exit")) {
		//					
		//					peer.quitChallenge(peer.getChallenge().getCodice_partita());
		//					textGraphics.setForegroundColor(TextColor.ANSI.DEFAULT);
		//
		//					Thread.sleep(200);
		//					return;
		//					
		//				}
		//				
		//				if(!checkInput(choice.toString().toUpperCase())){
		//					textGraphics.putString(50,8, "                      ", SGR.ITALIC, SGR.BOLD);
		//					textGraphics.putString(50,8, "Comando non valido!", SGR.BOLD);
		//					Thread.sleep(100);
		//					screen.refresh();
		//					continue;
		//				}
		//				int x_axes = (choice.toString().toUpperCase().charAt(0)) - 65;
		//				int y_axes = (choice.toString().toUpperCase().charAt(1)) - 65;
		//				int value = (choice.toString().toUpperCase().charAt(3)) - 48;
		//				
		//
		//
		//				switch(peer.placeNumber(peer.getChallenge().getCodice_partita(), x_axes, y_axes, value)) {
		//					
		//				case 1:{
		//					textGraphics.putString(50,8, "                      ", SGR.ITALIC, SGR.BOLD);
		//					textGraphics.setForegroundColor(TextColor.ANSI.GREEN);
		//					textGraphics.putString(50,8, "Valore corretto ", SGR.ITALIC, SGR.BOLD);
		//					continue;
		//				}
		//				case -1:{
		//					textGraphics.putString(50,8, "                      ", SGR.ITALIC, SGR.BOLD);
		//					textGraphics.setForegroundColor(TextColor.ANSI.RED);
		//					textGraphics.putString(50,8, "Valore errato ", SGR.ITALIC, SGR.BOLD);
		//					continue;
		//				}
		//				case 0:{
		//					textGraphics.putString(50,8, "                      ", SGR.ITALIC, SGR.BOLD);
		//					textGraphics.setForegroundColor(TextColor.ANSI.DEFAULT);
		//					textGraphics.putString(50,8, "Valore già presente ", SGR.ITALIC, SGR.BOLD);
		//					continue;
		//				}
		//				default:{
		//					textGraphics.putString(50,8, "                      ", SGR.ITALIC, SGR.BOLD);
		//					textGraphics.setForegroundColor(TextColor.ANSI.BLUE);
		//					textGraphics.putString(50,8, "Valore non inviato ", SGR.ITALIC, SGR.BOLD);
		//					continue;
		//				}
		//				
		//				}
		//				
		//				
		//			}
		//			else if(ks.getKeyType() == KeyType.Character) {
		//
		//				textGraphics.setForegroundColor(TextColor.ANSI.DEFAULT);
		//				if(choice.length() == 4) {
		//					continue;
		//				}
		//				choice.append(ks.getCharacter());
		//				System.out.println(choice.toString());
		//
		//				textGraphics.putString(tp.withRelativeRow(6).withRelativeColumn(45), choice.toString(), SGR.BOLD);
		//				screen.setCursorPosition(tp.withRelativeRow(6).withRelativeColumn(45 + choice.length()));
		//			}
		//			else if(ks.getKeyType() == KeyType.Backspace && choice.length()!=0) {
		//				textGraphics.setForegroundColor(TextColor.ANSI.DEFAULT);
		//				choice.deleteCharAt(choice.length()-1);
		//				System.out.println(choice.toString());
		//
		//				textGraphics.putString(tp.withRelativeRow(6).withRelativeColumn(45), "               ", SGR.BOLD);
		//				textGraphics.putString(tp.withRelativeRow(6).withRelativeColumn(45), choice.toString(), SGR.BOLD);
		//				screen.setCursorPosition(tp.withRelativeRow(6).withRelativeColumn(45 + choice.length()));
		//			}
		//
		//		}

	}

	public void create_home_screen() {

          
		terminal.println("   .--.        .-.    .-.           .--.                      ");
		terminal.println("  : .--'       : :    : :.-.       : .--'                     ");
		terminal.println("  `. `..-..-..-' :.--.: `'..-..-.  : : _ .--. ,-.,-.,-..--.   ");
		terminal.println("   _`, : :; ' .; ' .; : . `: :; :  : :; ' .; ;: ,. ,. ' '_.'  ");
		terminal.println("  `.__.`.__.`.__.`.__.:_;:_`.__.'  `.__.`.__,_:_;:_;:_`.__.'  ");
		terminal.println("                                                              ");
		terminal.println("                                                              ");

	}

	//	public static void sudoku_screen(Sudoku sudoku) throws Exception {
	//
	//		int column=0, row=0, i=0, j=0;
	//		StringBuilder rows = new StringBuilder();
	//
	//		sudoku_header(rows, 3, 2);
	//		rows.delete(0, rows.length());
	//
	//		for(i=3;i<20;i++) {
	//
	//			if(i%2==1) sudoku_line(3, i);
	//			else sudoku_body(rows, 3, i);
	//
	//			rows.delete(0, rows.length());
	//		}
	//		sudoku_footer(rows, 3, 20);
	//		rows.delete(0, rows.length());
	//		column = 5;
	//		row = 3;
	//		for(i=0;i<9;i++) {
	//			for(j=0;j<9;j++) {
	//				textGraphics.putString(column, row, sudoku.getSudoku_sfida()[i][j]!=0 ? Integer.toString(sudoku.getSudoku_sfida()[i][j]) : "", SGR.BOLD);
	//				column+=4;
	//			}
	//			row+=2;
	//			column = 5;
	//		}
	//
	//		sudoku_indexes();
	//
	//
	//	}
	//
	//	public static void sudoku_header(StringBuilder rows, int column, int row) {
	//
	//		rows.append(Symbols.SINGLE_LINE_TOP_LEFT_CORNER);
	//
	//		for(int i=1;i<9;i++) {
	//
	//			rows.append(Symbols.SINGLE_LINE_HORIZONTAL);
	//			rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
	//			rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
	//			rows.append(Symbols.SINGLE_LINE_T_DOWN);
	//
	//
	//		}
	//
	//		rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
	//		rows.append(Symbols.SINGLE_LINE_HORIZONTAL);
	//		rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
	//		rows.append(Symbols.SINGLE_LINE_TOP_RIGHT_CORNER  );
	//		textGraphics.putString(column, row, rows.toString());
	//
	//	}
	//
	//	public static void sudoku_footer(StringBuilder rows, int column, int row) {
	//
	//		rows.append(Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER  );
	//		for(int i=1;i<9;i++) {
	//
	//			rows.append(Symbols.SINGLE_LINE_HORIZONTAL);
	//			rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
	//			rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
	//			rows.append(Symbols.SINGLE_LINE_T_UP);
	//
	//		}
	//		rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
	//		rows.append(Symbols.SINGLE_LINE_HORIZONTAL);
	//		rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
	//		rows.append(Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER    );
	//		textGraphics.putString(column, row, rows.toString());
	//
	//	}
	//
	//	public static void sudoku_body(StringBuilder rows, int column, int row) {
	//
	//		rows.append(Symbols.SINGLE_LINE_T_RIGHT );
	//		for(int i=1;i<9;i++) {
	//
	//			rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
	//			rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
	//			rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
	//			rows.append(Symbols.SINGLE_LINE_CROSS );
	//
	//		}
	//		rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
	//		rows.append(Symbols.SINGLE_LINE_HORIZONTAL);
	//		rows.append(Symbols.SINGLE_LINE_HORIZONTAL );
	//		rows.append(Symbols.SINGLE_LINE_T_LEFT );
	//		textGraphics.putString(column, row, rows.toString());
	//	}
	//
	//	public static void sudoku_line(int column, int row) {
	//
	//		for(int i=0;i<37;i++) {
	//
	//			if(i==0 || i%4==0 || i==36) textGraphics.setCharacter(i+column,row, Symbols.SINGLE_LINE_VERTICAL );
	//
	//		}
	//
	//	}
	//
	//	public static void sudoku_indexes() {
	//		char [] index = {'A','B','C','D','E','F','G','H','I'};
	//		int row_column = 3;
	//		for(char i: index) {
	//			textGraphics.setCharacter(1, row_column, i );
	//			row_column+=2;
	//		}
	//		row_column = 5;
	//		for(char i: index) {
	//			textGraphics.setCharacter(row_column, 21, i );
	//			row_column+=4;
	//		}
	//
	//	}
	//
		public void exit() throws Exception {
			try {
				
				peer.leaveNetwork();
				peer.shutdown();
				terminal.dispose();
				Thread.sleep(300);
				System.exit(0);
				
			}catch(Exception e) {
				e.printStackTrace();
			}
			
		}
	//	
	//	public boolean checkInput(String input) {
	//		return 		(Character.valueOf(input.charAt(0)).compareTo('A')) >= 0 && (Character.valueOf(input.charAt(0)).compareTo('I')) <= 0 
	//				&&	(Character.valueOf(input.charAt(1)).compareTo('A')) >= 0 && (Character.valueOf(input.charAt(1)).compareTo('I')) <= 0 
	//				&&	(Character.valueOf(input.charAt(2)).compareTo('-')) == 0 
	//				&&	(Character.valueOf(input.charAt(3)).compareTo('1')) >= 0 && (Character.valueOf(input.charAt(3)).compareTo('9')) <= 0;
	//	}
	//	
	//	public void svuota(int column, int row, int n) throws Exception {
	//		for(int i=0; i<n;i++, row++) 
	//			textGraphics.putString(column, row, "                                                            ");
	//			
	//		
	//		screen.refresh();
	//	}
	//	
}
