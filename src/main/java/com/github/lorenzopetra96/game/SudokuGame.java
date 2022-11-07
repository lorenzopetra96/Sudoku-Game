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


	private static TextIO textIO = TextIoFactory.getTextIO();
	private static TextTerminal terminal = textIO.getTextTerminal();
	private static Client peer;

	private int row;
	private static SudokuGame game;

	@Option(name="-m", aliases="--masterip", usage="the master peer ip address", required=true)
	private static String master;

	@Option(name="-id", aliases="--identifierpeer", usage="the unique identifier for this peer", required=true)
	private static int id;

	private SudokuGame(String[] args) throws Exception {

		System.out.print("\033[H\033[2J");  
		System.out.flush();

		CmdLineParser parser = new CmdLineParser(this); 
		parser.parseArgument(args); 
//		System.out.println("\nPeer: " + id + " on Master: " + master + "\n");
		peer = new ClientImpl(master, id, new MessageListener());


	}

	class MessageListener{

		public MessageListener(){}

		public Object parseMessage(Object obj) throws Exception {

//			System.out.println("OBJ: " + obj.getClass().toString() + " | CHA: " + peer.getChallenges().getClass().toString());

			if(obj.getClass().equals(peer.getChallenges().getClass())) {
//				System.out.println("Aggiornamento lista partite disponibili. Listener lista ha prelevato un messaggio");
				peer.setChallenges((ArrayList<Challenge>) obj);
				if(peer.getChallenge() == null) {
//					Robot robot = new Robot();
					terminal.resetLine();
					terminal.getProperties().setPromptColor("yellow");
					terminal.println("!!! Nuove partite create, clicca invio per aggiornare !!!");
					terminal.getProperties().setPromptColor("white");
					Thread.sleep(1000);
//					robot.keyPress(KeyEvent.VK_I);
//					robot.keyRelease(KeyEvent.VK_I);
				
				}
			}
			else if(obj.getClass().equals(peer.getChallenge().getClass())) {
				
				peer.setChallenge((Challenge) obj);
				if(peer.getChallenge() != null) {
//					Robot robot = new Robot();
					terminal.resetLine();
					peer.setChallenge( (Challenge) obj);
					terminal.getProperties().setPromptColor("yellow");
					terminal.println("!!! Aggiornamento sfida, clicca invio per aggiornare !!!");
					terminal.getProperties().setPromptColor("white");
					Thread.sleep(1000);
//					robot.keyPress(KeyEvent.VK_I);
//					robot.keyRelease(KeyEvent.VK_I);
				}

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

//		System.out.println("Eccomi nella home!");


		terminal.getProperties().setPromptColor("white");
		terminal.getProperties().setPaneDimension(700, 700);

		while(true) {

			terminal.setBookmark("BOOKMARK");
			terminal.println(" ");
			create_home_screen();

			String nickname = textIO.newStringInputReader()
					.read("   Inserisci un nickname: ");



			if(nickname.length()>2 && nickname.length()<8 && peer.checkPlayer(nickname.toString())) {
				terminal.resetToBookmark("BOOKMARK");
				break;
			}
			else {
				terminal.println("  ");
				terminal.println("  ");
				terminal.getProperties().setPromptColor("red");
				if(!(nickname.length()>2 && nickname.length()<8)) 
					terminal.println("  La lunghezza del nickname deve essere compresa tra 3 e 7 caratteri");
				else terminal.println("  Nickname già utilizzato");
				
				Thread.sleep(2000);
				terminal.getProperties().setPromptColor("white");
				terminal.resetToBookmark("BOOKMARK");
			}

		}
	}




	public void choices_screen() throws Exception{

		System.out.print("\033[H\033[2J");  
		System.out.flush();
		String choice;
		String codice_partita_0;
		while(true) {
			terminal.setBookmark("TABELLONE");
			peer.reloadChallengeList();
			System.out.print("\033[H\033[2J");  
			System.out.flush();
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
				terminal.print("\n\n\t\t\tNon ci sono sfide attive...\n\n\n");
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

		int countdown = 6;

		while(true) {
			System.out.print("\033[H\033[2J");  
			System.out.flush();
			
			terminal.resetToBookmark("BOOKMARK");
			terminal.println("[PEER " + id + " | " + peer.getPlayer().getNickname() + "]");
			
			if(!peer.getChallenge().isTerminated()) peer.reloadChallenge(peer.getChallenge().getCodice_partita());

			terminal.println("\n\t  SUDOKU GAME - " + peer.getChallenge().getCodice_partita());

			terminal.println("\n");

			sudoku_screen(peer.getChallenge().getSudoku_board());


			terminal.println("\n\n\n Digita 'XY-N' per inserire il valore N nella cella X,Y");
			terminal.println(" Digita 'exit' per abbandonare la partita");

			
			
			if(!peer.getChallenge().isStarted() && peer.getChallenge().getPlayers_scores().size()<2 && !peer.getChallenge().isTerminated()) {
				terminal.getProperties().setPromptColor("yellow");
				terminal.getProperties().setPromptItalic(true);

				terminal.println(" \nLa partita comincerà dall'ingresso del secondo giocatore");
				Thread.sleep(1500);

				terminal.getProperties().setPromptColor("white");
				terminal.getProperties().setPromptItalic(false);
				continue;

			}
			else if(!peer.getChallenge().isStarted() && peer.getChallenge().getPlayers_scores().size()>1) {
				peer.startChallenge(peer.getChallenge().getCodice_partita());
			}
			else if(peer.getChallenge().isStarted() && peer.getChallenge().getPlayers_scores().size()==1 && !peer.getChallenge().isTerminated()){
				terminal.resetToBookmark("BOOKMARK");
				peer.getChallenge().setTerminated(true);
//				peer.removeFromChallengeList();
				continue;

			}


			if(peer.getChallenge().isTerminated()) {

				
				terminal.getProperties().setPromptColor("green");
				terminal.getProperties().setPromptBold(true);
				terminal.println(" La partita è terminata. ");
				
//				if(peer.getChallenge() != null && !peer.getChallenge().isFull() && peer.getChallenge().getWinner()!=null && peer.getChallenge().getWinner().isEmpty()) {
//
//					peer.getChallenge().setWinner(new Pair<String,Integer>(peer.getPlayer().getNickname(), peer.getChallenge().getPlayers_scores().get(peer.getPlayer().getNickname())) );
//					terminal.println(peer.getChallenge().getWinner().element0() + " vince con punti pari a " + peer.getChallenge().getWinner().element1() );
//
//				}
				
				if(peer.getChallenge().isFull()) terminal.println("  \n" + peer.getChallenge().getWinner().element0() + " vince con punti pari a " + peer.getChallenge().getWinner().element1() );
				
				if(peer.getChallenge().getPlayers_scores().size()==1) {

					peer.quitChallenge(peer.getChallenge().getCodice_partita());
					terminal.getProperties().setPromptBold(false);
					terminal.getProperties().setPromptColor("white");
					Thread.sleep(1000);
					return;
				}
				
				terminal.getProperties().setPromptBold(false);
				terminal.getProperties().setPromptColor("white");
				terminal.println("\n Sarai reindirizzato al tabellone sfide tra " + countdown-- + " secondi...");

				Thread.sleep(1000);
				if(countdown==0) return;
			}else {
				String choice = "   ";
				
				choice = textIO.newStringInputReader()
							.read("\n   > ");
				
				
				
				if(choice.equals("exit")) {

					peer.quitChallenge(peer.getChallenge().getCodice_partita());
					return;
				}
				else if(!peer.getChallenge().isStarted() && peer.getChallenge().getPlayers_scores().size()<2 && !peer.getChallenge().isTerminated()) continue;
				else{
					if(checkInput(choice.toString().toUpperCase())) {


						int x_axes = (choice.toString().toUpperCase().charAt(0)) - 65;
						int y_axes = (choice.toString().toUpperCase().charAt(1)) - 65;
						int value = (choice.toString().toUpperCase().charAt(3)) - 48;



						switch(peer.placeNumber(peer.getChallenge().getCodice_partita(), x_axes, y_axes, value)) {

						case 1:{
							terminal.getProperties().setPromptColor("green");
							terminal.println("Valore corretto!");
							Thread.sleep(1000);
							terminal.getProperties().setPromptColor("white");
							terminal.resetToBookmark("BOOKMARK");
							continue;
						}
						case -1:{
							terminal.getProperties().setPromptColor("red");
							terminal.println("Valore errato!");
							Thread.sleep(1000);
							terminal.getProperties().setPromptColor("white");
							terminal.resetToBookmark("BOOKMARK");
							continue;
						}
						case 0:{
							terminal.getProperties().setPromptColor("yellow");
							terminal.println("Valore già presente!");
							Thread.sleep(1000);
							terminal.getProperties().setPromptColor("white");
							terminal.resetToBookmark("BOOKMARK");
							continue;
						}
						default:{
							terminal.getProperties().setPromptColor("white");
							terminal.println("Valore non inviato!");
							Thread.sleep(1000);
							terminal.getProperties().setPromptColor("white");
							terminal.resetToBookmark("BOOKMARK");
							continue;
						}

						}
					}
				}

			}

			terminal.resetToBookmark("BOOKMARK");

		}

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

	public static void sudoku_screen(Sudoku sudoku) throws Exception {

		ArrayList<Pair<String, Integer>> scores = new ArrayList<Pair<String, Integer>>();
		String score = "";
		char[] index = {'A','B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};

		for(Map.Entry<String, Integer> set: peer.getChallenge().getPlayers_scores().entrySet()) {

			scores.add(new Pair<String,Integer>(set.getKey(), set.getValue()));

		}

		terminal.println("     A   B   C   D   E   F   G   H   I");
		terminal.println("   +---+---+---+---+---+---+---+---+---+" + "\t Nickname  Punteggio");

		for(int i=0;i<9;i++) {

			if(scores.size()> (i-1) && i!=0) {
				score = "\t " + scores.get(i-1).element0();
				for(int len=scores.get(i-1).element0().length(); len<7; len++) score+= " ";
				score += "       " + Integer.toString(scores.get(i-1).element1());
			}

			terminal.println(" " + index[i] + " | " + (sudoku.getSudoku_sfida()[i][0]==0 ? " " : Integer.toString(sudoku.getSudoku_sfida()[i][0])) +
					" | " + (sudoku.getSudoku_sfida()[i][1]==0 ? " " : Integer.toString(sudoku.getSudoku_sfida()[i][1])) +
					" | " + (sudoku.getSudoku_sfida()[i][2]==0 ? " " : Integer.toString(sudoku.getSudoku_sfida()[i][2])) +
					" | " + (sudoku.getSudoku_sfida()[i][3]==0 ? " " : Integer.toString(sudoku.getSudoku_sfida()[i][3])) +
					" | " + (sudoku.getSudoku_sfida()[i][4]==0 ? " " : Integer.toString(sudoku.getSudoku_sfida()[i][4])) +
					" | " + (sudoku.getSudoku_sfida()[i][5]==0 ? " " : Integer.toString(sudoku.getSudoku_sfida()[i][5])) +
					" | " + (sudoku.getSudoku_sfida()[i][6]==0 ? " " : Integer.toString(sudoku.getSudoku_sfida()[i][6])) +
					" | " + (sudoku.getSudoku_sfida()[i][7]==0 ? " " : Integer.toString(sudoku.getSudoku_sfida()[i][7])) +
					" | " + (sudoku.getSudoku_sfida()[i][8]==0 ? " " : Integer.toString(sudoku.getSudoku_sfida()[i][8])) + " |" + score);
			score = "";
			terminal.println("   +---+---+---+---+---+---+---+---+---+");
		}




	}

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

	public boolean checkInput(String input) {
		return 		(Character.valueOf(input.charAt(0)).compareTo('A')) >= 0 && (Character.valueOf(input.charAt(0)).compareTo('I')) <= 0 
				&&	(Character.valueOf(input.charAt(1)).compareTo('A')) >= 0 && (Character.valueOf(input.charAt(1)).compareTo('I')) <= 0 
				&&	(Character.valueOf(input.charAt(2)).compareTo('-')) == 0 
				&&	(Character.valueOf(input.charAt(3)).compareTo('1')) >= 0 && (Character.valueOf(input.charAt(3)).compareTo('9')) <= 0;
	}

}
