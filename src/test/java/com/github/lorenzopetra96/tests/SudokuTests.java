package com.github.lorenzopetra96.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.lorenzopetra96.game.ClientImpl;
import com.github.lorenzopetra96.interfaces.Client;

public class SudokuTests {

	private static Client peer1;
	private static Client peer2;
	private static Client peer3;
	private static Client peer4;


	@BeforeEach
	public void init() throws Exception{

		peer1 = new ClientImpl("127.0.0.1", 0);
		peer2 = new ClientImpl("127.0.0.1", 1);
		peer3 = new ClientImpl("127.0.0.1", 2);
		peer4 = new ClientImpl("127.0.0.1", 3);
	}

	@AfterEach
	public void terminate() {

		peer1.leaveNetwork();
		peer2.leaveNetwork();
		peer3.leaveNetwork();
		peer4.leaveNetwork();
		peer1 = null;
		peer2 = null;
		peer3 = null;
		peer4 = null;

	}

	// Login tests

		@Test
		void testCase_Checklogin() throws Exception {
			
			assertTrue(peer1.checkPlayer("lorenzo"));
			assertTrue(peer2.checkPlayer("mario"));
			assertTrue(peer3.checkPlayer("dario"));
			assertTrue(peer4.checkPlayer("marcello"));
			
		}

		@Test
		void testCase_NicknameAlreadyExists() throws Exception{
			
			peer1.checkPlayer("lorenzo");
			
			assertFalse(peer2.checkPlayer("lorenzo"));
			
		}

		@Test
		void testCase_PeerAlreadyLoggedIn() throws Exception{
			
			peer1.checkPlayer("lorenzo");
	
	        Exception exception = assertThrows(RuntimeException.class, () -> {
	            peer1.checkPlayer("mario");
	        });
	
	        assertEquals(exception.getMessage(), "Player già presente.");
		}
		
		
		@Test
		void testCase_NicknameUnavailable() throws Exception{
			
			assertFalse(peer1.checkPlayer("lor enzo"));
		}
		

	// Challenges tests

		@Test
		void testCase_GenerateNewSudokuChallenge() throws Exception{
			peer1.checkPlayer("lorenzo");
			assertTrue(peer1.generateNewSudoku("challenge1", 0));
			assertNotNull(peer1.getChallenge());
			
		}
		
		@Test
		void testCase_FindChallenge() throws Exception{
			
			peer1.checkPlayer("lorenzo");
			assertTrue(peer1.generateNewSudoku("challenge1", 0));
			assertEquals(peer1.findChallenge(), 0);
			
		}

		@Test
		void testCase_CodicePartitaAlreadyExists() throws Exception{
			
			peer1.checkPlayer("lorenzo");
			peer2.checkPlayer("mario");
			
			peer1.generateNewSudoku("challenge1", 0);
			peer2.reloadChallengeList();
			
			
			assertFalse(peer2.generateNewSudoku("challenge1", 0));
			
			
		}

		@Test
		void testCase_AddChallengeToChallengesList() throws Exception{
			peer1.checkPlayer("lorenzo");
			
			peer1.generateNewSudoku("challenge1", 0);
			
			assertTrue(peer1.getChallenges().size()!=0);
		}

		@Test
		void testCase_RemoveChallengeFromChallengesList() throws Exception{
			peer1.checkPlayer("lorenzo");
			
			peer1.generateNewSudoku("challenge1", 0);
			
			assertTrue(peer1.getChallenges().size()!=0);
			
			peer1.removeChallenge("challenge1");
			
			assertTrue(peer1.getChallenges().size()==0);
		}

		@Test
		void testCase_JoinToChallenge() throws Exception{
			
			peer1.checkPlayer("lorenzo");
			peer2.checkPlayer("mario");
			
			peer1.generateNewSudoku("challenge1", 0);
			peer2.reloadChallengeList();
			peer2.joinChallenge("challenge1");
			
			assertEquals(peer1.getChallenge().getCodice_partita(), peer2.getChallenge().getCodice_partita());
			
		}
	@Test
	void testCase_JoinToChallengeNotYetPresent() throws Exception{

		peer1.checkPlayer("lorenzo");
		peer2.checkPlayer("mario");

		peer1.generateNewSudoku("challenge1", 0);
		assertFalse(peer2.joinChallenge("challenge1"));

	}

		@Test
		void testCase_StartChallenge() throws Exception{
	
			peer1.checkPlayer("lorenzo");
	
			peer1.generateNewSudoku("challenge1", 0);
			
			assertFalse(peer1.getChallenge().isStarted());
			
			peer1.startChallenge("challenge1");
			
			assertTrue(peer1.getChallenge().isStarted());
	
		}
	
		@Test
		void testCase_LeaveNetwork() throws Exception{
			
			peer1.checkPlayer("lorenzo");
			peer1.leaveNetwork();
			assertTrue(peer2.checkPlayer("lorenzo"));
			
		}

	// Game tests

	@Test 
	void testCase_QuitChallenge() throws Exception{
		
		peer1.checkPlayer("lorenzo");
		peer2.checkPlayer("mario");
		
		peer1.generateNewSudoku("challenge1", 0);
		peer2.reloadChallengeList();
		peer2.joinChallenge("challenge1");
		
		assertTrue(peer2.quitChallenge("challenge1"));
		assertTrue(peer1.quitChallenge("challenge1"));
		
	}
	

	@Test
	void testCase_PlaceCorrectNumber() throws Exception{
		peer1.checkPlayer("lorenzo");
		peer2.checkPlayer("mario");
		
		peer1.generateNewSudoku("challenge1", -1);
		peer2.reloadChallengeList();
		peer2.joinChallenge("challenge1");
		peer1.reloadChallenge("challenge1");
		
		assertEquals(peer1.placeNumber("challenge1", 0, 0, 8), 1);
		
	}
	
	@Test
	void testCase_PlaceWrongNumber() throws Exception{
		peer1.checkPlayer("lorenzo");
		peer2.checkPlayer("mario");
		
		peer1.generateNewSudoku("challenge1", -1);
		peer2.reloadChallengeList();
		peer2.joinChallenge("challenge1");
		peer1.reloadChallenge("challenge1");
		
		assertEquals(peer1.placeNumber("challenge1", 0, 0, 1), -1);
		
	}
	
	@Test
	void testCase_PlaceAlreadyPlacedNumber() throws Exception{
		peer1.checkPlayer("lorenzo");
		peer2.checkPlayer("mario");
		
		peer1.generateNewSudoku("challenge1", -1);
		peer2.reloadChallengeList();
		peer2.joinChallenge("challenge1");
		peer1.reloadChallenge("challenge1");
		
		assertEquals(peer1.placeNumber("challenge1", 0, 1, 4), 0);
		
	}
	
	@Test
	void testCase_SudokuTerminated() throws Exception{
		
		peer1.checkPlayer("lorenzo");
		peer2.checkPlayer("mario");
		
		peer1.generateNewSudoku("challenge1", -1);
		peer2.reloadChallengeList();
		peer2.joinChallenge("challenge1");
		peer1.reloadChallenge("challenge1");
		
		peer1.placeNumber("challenge1", 0, 0, 8);
		peer1.placeNumber("challenge1", 0, 2, 3);
		peer1.placeNumber("challenge1", 0, 4, 7);
		peer1.placeNumber("challenge1", 0, 6, 6);
		peer1.placeNumber("challenge1", 0, 8, 2);
		
		peer1.placeNumber("challenge1", 1, 1, 1);
		peer1.placeNumber("challenge1", 1, 3, 8);
		peer1.placeNumber("challenge1", 1, 5, 2);
		peer1.placeNumber("challenge1", 1, 7, 4);
		
		peer1.placeNumber("challenge1", 2, 0, 6);
		peer1.placeNumber("challenge1", 2, 2, 2);
		peer1.placeNumber("challenge1", 2, 4, 4);
		peer1.placeNumber("challenge1", 2, 6, 1);
		peer1.placeNumber("challenge1", 2, 8, 7);
		
		peer1.placeNumber("challenge1", 3, 1, 3);
		peer1.placeNumber("challenge1", 3, 3, 1);
		peer1.placeNumber("challenge1", 3, 5, 5);
		peer1.placeNumber("challenge1", 3, 7, 7);
		
		peer1.placeNumber("challenge1", 4, 0, 2);
		peer1.placeNumber("challenge1", 4, 2, 5);
		peer1.placeNumber("challenge1", 4, 4, 3);
		peer1.placeNumber("challenge1", 4, 6, 4);
		peer1.placeNumber("challenge1", 4, 8, 6);
		
		peer1.placeNumber("challenge1", 5, 1, 9);
		peer1.placeNumber("challenge1", 5, 3, 4);
		peer1.placeNumber("challenge1", 5, 5, 6);
		peer1.placeNumber("challenge1", 5, 7, 2);
		
		peer1.placeNumber("challenge1", 6, 0, 5);
		peer1.placeNumber("challenge1", 6, 2, 9);
		peer1.placeNumber("challenge1", 6, 4, 1);
		peer1.placeNumber("challenge1", 6, 6, 2);
		peer1.placeNumber("challenge1", 6, 8, 4);
	
		peer1.placeNumber("challenge1", 7, 1, 6);
		peer1.placeNumber("challenge1", 7, 3, 2);
		peer1.placeNumber("challenge1", 7, 5, 7);
		peer1.placeNumber("challenge1", 7, 7, 3);
		
		peer1.placeNumber("challenge1", 8, 0, 3);
		peer1.placeNumber("challenge1", 8, 2, 8);
		peer1.placeNumber("challenge1", 8, 4, 9);
		peer1.placeNumber("challenge1", 8, 6, 7);
		
		assertFalse(peer1.getChallenge().isFull());
		assertFalse(peer1.getChallenge().isTerminated());
		
		assertEquals(peer1.placeNumber("challenge1", 8, 8, 1), 1);
		
		assertTrue(peer1.getChallenge().isFull());
		assertTrue(peer1.getChallenge().isTerminated());
		
		
	}

}
