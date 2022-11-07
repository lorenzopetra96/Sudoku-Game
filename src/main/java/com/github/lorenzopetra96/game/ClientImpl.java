package com.github.lorenzopetra96.game;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;

import com.github.lorenzopetra96.beans.Challenge;
import com.github.lorenzopetra96.beans.Pair;
import com.github.lorenzopetra96.beans.Player;
import com.github.lorenzopetra96.interfaces.Client;


import com.github.lorenzopetra96.exceptions.*;
import com.github.lorenzopetra96.game.SudokuGame.MessageListener;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;

public class ClientImpl implements Client{
	final private Peer peer;
	final private PeerDHT _dht;
	final private int DEFAULT_MASTER_PORT=4000;
	final private Number160 playersKey = Number160.createHash("players");
	final private Number160 challengesKey = Number160.createHash("challenges");

	final private ArrayList<String> s_topics=new ArrayList<String>();

	private ArrayList<Challenge> challenges = new ArrayList<Challenge>();
	private ArrayList<Player> players = new ArrayList<Player>();
	private Challenge challenge = null;
	private Player player = null;



	public ArrayList<Player> getPlayers() {
		return players;
	}




	public void setPlayers(ArrayList<Player> players) {
		this.players = players;
	}




	public void setChallenges(ArrayList<Challenge> challenges) {
		this.challenges = challenges;
	}




	public void setChallenge(Challenge challenge) {
		this.challenge = challenge;
	}




	public ClientImpl( String _master_peer, int id, final MessageListener _listener) throws Exception
	{
		peer= new PeerBuilder(Number160.createHash(id)).ports(DEFAULT_MASTER_PORT+id).start();
		_dht = new PeerBuilderDHT(peer).start();	

		FutureBootstrap fb = peer.bootstrap().inetAddress(InetAddress.getByName(_master_peer)).ports(DEFAULT_MASTER_PORT).start();
		fb.awaitUninterruptibly();
		if(fb.isSuccess()) {
			peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
		}else {
			throw new Exception("Error in master peer bootstrap.");
		}

		peer.objectDataReply(new ObjectDataReply() {

			public Object reply(PeerAddress sender, Object request) throws Exception {
//				Robot robot = new Robot();
//				System.out.println("Sono in replay in ClientImpl: " + request.getClass());
//				robot.keyPress(KeyEvent.VK_ENTER);
				return _listener.parseMessage(request);
			}
		});

		try {
			_dht.get(playersKey).start().awaitUninterruptibly();
		} catch (Exception e) {
			_dht.put(playersKey).data(new Data(players)).start().awaitUninterruptibly();
		}

		try {
			_dht.get(challengesKey).start().awaitUninterruptibly();
		} catch (Exception e) {
			_dht.put(challengesKey).data(new Data(challenges)).start().awaitUninterruptibly();
		}

	}

	
	
	
	@Override
	public boolean checkPlayer(String nickname) throws Exception{

		if(player != null) {
			throw new RuntimeException("Player già presente.");
		}

		if(nickname.contains(" ")) return false;

		

		try {
			FutureGet futureGet = _dht.get(playersKey).start().awaitUninterruptibly();

			if(futureGet.isSuccess()) {
				if(!futureGet.isEmpty()) {
					players = (ArrayList<Player>) futureGet.dataMap().values().iterator().next().object();

					for(Player pl: players) {
						if(pl.getNickname().equals(nickname)) return false;
					}
				}
				player = new Player(nickname, peer.peerAddress());
				players.add(player);
				_dht.put(playersKey).data(new Data(players)).start().awaitUninterruptibly();
				return true;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		return false;
	}



	@Override
	public boolean generateNewSudoku(String codice_partita, int seed) throws Exception {


		try {



			challenge = new Challenge(codice_partita, player.getNickname(), seed);

			FutureGet futureGet = _dht.get(Number160.createHash(codice_partita)).start().awaitUninterruptibly();
			if (futureGet.isSuccess() && checkChallenge(codice_partita)) {

				if(!futureGet.isEmpty()) {
					return false;
				}

				_dht.put(Number160.createHash(codice_partita)).data(new Data(challenge)).start().awaitUninterruptibly();
				
				addChallenge();
				return true;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}
	
	public boolean addChallenge() throws Exception {

		try {

			
			FutureGet futureGet = _dht.get(challengesKey).start().awaitUninterruptibly();
			if (futureGet.isSuccess()) {

				challenges.add(challenge);
				_dht.put(challengesKey).data(new Data(challenges)).start().awaitUninterruptibly();
				reloadPlayers();
				for(Player peer: players)
				{
					if(peer.getNickname().equals(player.getNickname())) continue;
					FutureDirect futureDirect = _dht.peer().sendDirect(peer.getPeerAdd()).object(challenges).start();
					futureDirect.awaitUninterruptibly();
				}
				return true;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	@Override
	public boolean checkChallenge(String codice_partita) throws Exception {
		int index=0;

		try {

			FutureGet futureGet = _dht.get(challengesKey).start().awaitUninterruptibly();
			if (futureGet.isSuccess()) {

				if(!futureGet.isEmpty()) {
					challenges = (ArrayList<Challenge>) futureGet.dataMap().values().iterator().next().object();
				}

				Iterator<Challenge> myIter = challenges.iterator();

				while (myIter.hasNext()) {
					Challenge tmp1 = myIter.next();
					if (tmp1.getCodice_partita().equals(codice_partita)) {
						throw new ChallengeAlreadyExistsException();
					}
					index++;
				}

				return true;
			}

		}catch(ChallengeAlreadyExistsException e) {
			System.out.println("Sfida con codice partita " + codice_partita + " già esistente");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}


	

	@Override
	public boolean removeChallenge(String codice_partita) throws Exception {


		try {


			FutureRemove futureRem = _dht.remove(Number160.createHash(codice_partita)).all().start().awaitUninterruptibly();

			if (futureRem.isSuccess()) {

//				removeFromChallengeList();

				return true;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}
	
	@Override
	public void removeFromChallengeList() throws Exception{

		try {

			FutureGet futureGet = _dht.get(challengesKey).start().awaitUninterruptibly();

			if(futureGet.isSuccess()) {
				if(!futureGet.isEmpty()) {
					challenges = (ArrayList<Challenge>) futureGet.dataMap().values().iterator().next().object();
					if(challenges.size()<2) challenges.clear();
					else challenges.remove(findChallenge());
					

					_dht.put(challengesKey).data(new Data(challenges)).start().awaitUninterruptibly();

				}
				reloadPlayers();
				for(Player peer: players)
				{
					if(peer.getNickname().equals(player.getNickname())) continue;
					FutureDirect futureDirect = _dht.peer().sendDirect(peer.getPeerAdd()).object(challenges).start().awaitUninterruptibly();
				}
				

			}

		}catch (Exception e) {
			e.printStackTrace();
		}


	}

	@Override
	public void reloadChallengeList() throws Exception{

		try {

			FutureGet futureGet = _dht.get(challengesKey).start().awaitUninterruptibly();

			if(futureGet.isSuccess()) {
				if(!futureGet.isEmpty())
					challenges = (ArrayList<Challenge>) futureGet.dataMap().values().iterator().next().object();

			}

		}catch (Exception e) {
			e.printStackTrace();
		}


	}
	
	@Override
	public void reloadPlayers() throws Exception{

		try {

			FutureGet futureGet = _dht.get(playersKey).start().awaitUninterruptibly();

			if(futureGet.isSuccess()) {
				if(!futureGet.isEmpty())
					players = (ArrayList<Player>) futureGet.dataMap().values().iterator().next().object();

			}

		}catch (Exception e) {
			e.printStackTrace();
		}


	}
	
	

	@Override
	public void updateChallengeList() throws Exception{

		int index = findChallenge();

		if(index == -1) throw new ChallengeNotFoundException();

		try {

			FutureGet futureGet = _dht.get(challengesKey).start().awaitUninterruptibly();

			if(futureGet.isSuccess()) {
				if(!futureGet.isEmpty()) {
					challenges.get(index).setPlayers_scores(challenge.getPlayers_scores());
					

				}
				
			}
			_dht.put(challengesKey).data(new Data(challenges)).start().awaitUninterruptibly();
			reloadPlayers();
			for(Player peer: players)
			{
				FutureDirect futureDirect = _dht.peer().sendDirect(peer.getPeerAdd()).object(challenges).start();
				futureDirect.awaitUninterruptibly();
			}



		}catch (Exception e) {
			e.printStackTrace();
		}


	}

	

	@Override
	public boolean reloadChallenge(String codice_partita) throws Exception{

		try {

			FutureGet futureGet = _dht.get(Number160.createHash(codice_partita)).start().awaitUninterruptibly();

			if(futureGet.isSuccess()) {

				if(futureGet.isEmpty()) {
					challenge.setTerminated(true);
				}
				challenge = (Challenge) futureGet.dataMap().values().iterator().next().object();
				return true;
			}

		}catch (Exception e) {
			e.printStackTrace();
		}

		return false;


	}
	
	@Override
	public boolean sendUpdatedChallenge() throws Exception{

		try {
			
			
			
			for (Map.Entry<String, Integer> entry : challenge.getPlayers_scores().entrySet())
			{
				if(entry.getKey().equals(player.getNickname())) continue;
				FutureDirect futureDirect = _dht.peer().sendDirect(players.get(findPlayer(entry.getKey())).getPeerAdd()).object(challenge).start();
				futureDirect.awaitUninterruptibly();
				
			}

		}
		catch( IndexOutOfBoundsException e) {
			System.out.println("Player non trovato nella lista dei partecipanti");
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return false;


	}

	@Override
	public boolean startChallenge(String codice_partita) throws Exception{

		try {

			FutureGet futureGet = _dht.get(Number160.createHash(codice_partita)).start().awaitUninterruptibly();

			if(futureGet.isSuccess()) {

				if(futureGet.isEmpty()) {
					challenge.setTerminated(true);
				}
				challenge = (Challenge) futureGet.dataMap().values().iterator().next().object();
				challenge.setStarted(true);
				_dht.put(Number160.createHash(codice_partita)).data(new Data(challenge)).start().awaitUninterruptibly();
				for (Map.Entry<String, Integer> entry : challenge.getPlayers_scores().entrySet())
				{
					if(entry.getKey().equals(player.getNickname())) continue;
//					System.out.println("Player: " + entry.getKey());
					FutureDirect futureDirect = _dht.peer().sendDirect(players.get(findPlayer(entry.getKey())).getPeerAdd()).object(challenge).start();
					futureDirect.awaitUninterruptibly();
					
				}

				return true;
			}

		}catch (Exception e) {
			e.printStackTrace();
		}

		return false;


	}
	
	


	@Override
	public boolean joinChallenge(String codice_partita) throws Exception{

		try {

			FutureGet futureGet = _dht.get(Number160.createHash(codice_partita)).start().awaitUninterruptibly();

			if(futureGet.isSuccess()) {

				if(futureGet.isEmpty()) {
					challenge.setTerminated(true);
				}

				challenge = (Challenge) futureGet.dataMap().values().iterator().next().object();

				challenge.getPlayers_scores().put(player.getNickname(), 0);
//				System.out.println("SIZE PLAYERS: " + challenge.getPlayers_scores().size());
//				System.out.println(challenge.getPlayers_scores().toString());
				_dht.put(Number160.createHash(codice_partita)).data(new Data(challenge)).start().awaitUninterruptibly();
				updateChallengeList();
				sendUpdatedChallenge();
			}
			return true;

		}catch(ChallengeNotFoundException e) {
			System.out.println("Partita non trovata nella lista delle partite disponibili.");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;


	}

	@Override
	public boolean quitChallenge(String codice_partita) throws Exception{

		try {

			FutureGet futureGet = _dht.get(Number160.createHash(codice_partita)).start().awaitUninterruptibly();

			if(futureGet.isSuccess()) {

				if(futureGet.isEmpty()) {
					challenge.setTerminated(true);
				}

				challenge = (Challenge) futureGet.dataMap().values().iterator().next().object();
				
				challenge.getPlayers_scores().remove(player.getNickname());
				
				if(challenge.getPlayers_scores().size()!=0) {
					if(challenge.getPlayers_scores().size() != 1) updateChallengeList();
					else removeFromChallengeList();
					for (Map.Entry<String, Integer> entry : challenge.getPlayers_scores().entrySet())
					{
//						System.out.println("Player: " + entry.getKey());
						FutureDirect futureDirect = _dht.peer().sendDirect(players.get(findPlayer(entry.getKey())).getPeerAdd()).object(challenge).start();
						futureDirect.awaitUninterruptibly();
						
					}
					
					_dht.put(Number160.createHash(codice_partita)).data(new Data(challenge)).start().awaitUninterruptibly();
					
				}else {
					
					removeChallenge(challenge.getCodice_partita());
					
				}
				
				
				
//				if(challenge.getPlayers_scores().size()==1) {
//					challenge.getPlayers_scores().clear();
//
//
//					removeFromChallengeList();
//				}
//				else {
//					challenge.getPlayers_scores().remove(player.getNickname());
//
//					
//					updateChallengeList();
//				}
//
//				
//				
//				if(challenge.getPlayers_scores().size()==1 || challenge.isTerminated()) {
//					removeFromChallengeList();
//				}
//
//
//				if(!(challenge.getPlayers_scores().isEmpty())) {
//
//
//					_dht.put(Number160.createHash(codice_partita)).data(new Data(challenge)).start().awaitUninterruptibly();
//					sendUpdatedChallenge();
//				}
//				else removeChallenge(codice_partita); 




			}
			return true;

		}catch (Exception e) {
			e.printStackTrace();
		}
		return false;


	}






	@Override
	public Integer placeNumber(String codice_partita, int x, int y, int value) throws Exception{
		Iterator<Pair<Player, Integer>> myIter;
		int index=0;
		Integer flag=0;
//		System.out.println("X: " + x + " - Y: " + y + " - N: " + value);
		try {

			FutureGet futureGet = _dht.get(Number160.createHash(codice_partita)).start().awaitUninterruptibly();

			if(futureGet.isSuccess()) {

				if(futureGet.isEmpty()) {
					challenge.setTerminated(true);
				}

				challenge = (Challenge) futureGet.dataMap().values().iterator().next().object();


				if(challenge.getSudoku_board().getSudoku_risolto()[x][y]!=value) {			//Se il value inserito è errato, lo score del giocatore perde un punto

					challenge.getPlayers_scores().put(player.getNickname(), challenge.getPlayers_scores().get(player.getNickname()) - 1);
					flag=-1;

				}else if(challenge.getSudoku_board().getSudoku_risolto()[x][y]==value) {	//Se il value inserito è giusto...

					if(challenge.getSudoku_board().getSudoku_sfida()[x][y]==0) {			//e la casella è ancora vuota allora lo score del giocatore guadagna un punto

						challenge.getPlayers_scores().put(player.getNickname(), challenge.getPlayers_scores().get(player.getNickname()) + 1);
						challenge.getSudoku_board().getSudoku_sfida()[x][y] = value;
						flag=1;

					}else flag=0;														//altrimenti guadagna zero punti


				}

				if(challenge.getSudoku_board().contaZeri(challenge.getSudoku_board().getSudoku_sfida()) == 0) {

					challenge.setFull(true);
					Map.Entry<String, Integer> maxEntry = null;
					for (Map.Entry<String, Integer> entry : challenge.getPlayers_scores().entrySet())
					{

						if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
						{
							maxEntry = entry;
						}
					}


					challenge.setTerminated(true);
					Pair<String, Integer> winner = new Pair<String,Integer>(maxEntry.getKey(), maxEntry.getValue());

					challenge.setWinner(winner);
					challenges.remove(findChallenge());
					
//					challenge.getSudoku_board().printSudoku(challenge.getSudoku_board().getSudoku_sfida());
					removeFromChallengeList();
//					removeChallenge(codice_partita);
				}
				
				_dht.put(Number160.createHash(codice_partita)).data(new Data(challenge)).start().awaitUninterruptibly();
				sendUpdatedChallenge();

			}
			return flag;

		}catch (Exception e) {
			e.printStackTrace();
		}
		return -100;


	}

	public int findChallenge() {

		int index=0;

		Iterator<Challenge> myIter = challenges.iterator();

		while (myIter.hasNext()) {
			Challenge tmp1 = myIter.next();
			if (tmp1.getCodice_partita().equals(challenge.getCodice_partita())) {
				return index;
			}
			index++;
		}


		return -1;

	}

	public int findPlayer(String nickname) {

		int index=0;

		Iterator<Player> myIter = players.iterator();

		while (myIter.hasNext()) {
			Player tmp1 = myIter.next();
			if (tmp1.getNickname().equals(nickname)) {
				return index;
			}
			index++;
		}


		return -1;

	}


	public boolean leaveNetwork() {

		try {
			FutureGet futureGet = _dht.get(playersKey).start().awaitUninterruptibly();

			if(futureGet.isSuccess()) {

				if(!futureGet.isEmpty()) {
					players = (ArrayList<Player>) futureGet.dataMap().values().iterator().next().object();
					if(players.size()==1) players.clear();
					else players.remove(findPlayer(player.getNickname()));
					_dht.put(playersKey).data(new Data(players)).start().awaitUninterruptibly();
				}

				return true;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}


		return false;
	}

	public void shutdown() {
		_dht.peer().announceShutdown().start().awaitUninterruptibly();

		challenges.clear();
		players.clear();
		challenge = null;
		player = null;
		peer.shutdown();
	}


	public ArrayList<Challenge> getChallenges() {
		return challenges;
	}


	public Challenge getChallenge() {
		return challenge;
	}


	public Player getPlayer() {
		return player;
	}


}