package fr.dauphine.ja.student.pandemiage.gameengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.FileNotFoundException;
import java.util.Objects;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.cli.ParseException;
import org.junit.Test;
import fr.dauphine.ja.pandemiage.common.Disease;
import fr.dauphine.ja.pandemiage.common.GameStatus;
import fr.dauphine.ja.pandemiage.common.PlayerCardInterface;
import fr.dauphine.ja.pandemiage.common.UnauthorizedActionException;

public class BoardTest {

	/**
	 * Test if the infectorCardsDeck contains exactly 48 - 9(first draws) cards at
	 * initialisation
	 * 
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 * @throws ParseException
	 */
	@Test
	public void infectorCardsInitTest() throws FileNotFoundException, XMLStreamException, ParseException {
		Board b = new Board("pandemic.graphml", 0);
		assertEquals(39, b.getInfectorCardsSize());
	}

	/**
	 * Test if the playerCardsDeck contains exactly the good number Epidemy cards
	 * 
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 * @throws ParseException
	 */

	@Test
	public void playerEpidemyCardsInitTest() throws FileNotFoundException, XMLStreamException, ParseException {
		Board b = new Board("pandemic.graphml", 2);
		int nbEpidemyCards = 0;
		int size = b.getPlayerCardsSize();

		for (int i = 0; i < size; ++i) {
			if (b.playerCardsDeckPoll().getCityName().equals("EPIDEMY"))
				++nbEpidemyCards;

		}
		assertEquals(6, nbEpidemyCards);
	}

	/**
	 * Test if the players cards deck contains 48 (standards cards) + 4 (epidemy
	 * cards) = 52 cards at initialization for difficulty 0 , 53 for difficulty 1
	 * and 54 for difficulty 2
	 * 
	 * 
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 * @throws ParseException
	 */

	@Test
	public void playerCardsInitTest() throws FileNotFoundException, XMLStreamException, ParseException {
		Board zero = new Board("pandemic.graphml", 0);
		Board one = new Board("pandemic.graphml", 1);
		Board two = new Board("pandemic.graphml", 2);
		assertEquals(52, zero.getPlayerCardsSize());
		assertEquals(53, one.getPlayerCardsSize());
		assertEquals(54, two.getPlayerCardsSize());
	}

	/**
	 * test if the function addDisease add a disease on a city and if it decreases
	 * cubes stock
	 * 
	 * @throws ParseException
	 * @throws XMLStreamException
	 * @throws IllegalArgumentException
	 * @throws FileNotFoundException
	 */

	@Test
	public void addDisease()
			throws FileNotFoundException, IllegalArgumentException, XMLStreamException, ParseException {
		Board board = new Board("pandemic.graphml", 0);
		board.treatAllDiseases();
		PlayerCardInterface card = new PlayerCard("Atlanta", Disease.BLUE);
		int nbBlue = board.getDiseasesCubes().get(Disease.BLUE);

		board.applyInfectorCard(card);
		assertEquals(nbBlue - 1, (int) board.getDiseasesCubes().get(Disease.BLUE));
		assertTrue(board.getCities().get("Atlanta").getDiseases().get(Disease.BLUE) == 1);

	}

	/**
	 * Test if a chain reaction impacts correctly the outbreakCounter without
	 * applying to already outbroken cities in the chain reaction.
	 * 
	 * @throws FileNotFoundException
	 * @throws IllegalArgumentException
	 * @throws XMLStreamException
	 * @throws ParseException
	 */
	@Test
	public void chainReactionTestSimple()
			throws FileNotFoundException, IllegalArgumentException, XMLStreamException, ParseException {
		Board board = new Board("pandemic.graphml", 0);
		board.treatAllDiseases();

		board.applyInfectorCard(new InfectorCard("Paris", Disease.BLUE));
		board.applyInfectorCard(new InfectorCard("Paris", Disease.BLUE));
		board.applyInfectorCard(new InfectorCard("Paris", Disease.BLUE));
		board.applyInfectorCard(new InfectorCard("Paris", Disease.BLUE));

		// Tests Paris has only 3 blue disease cubes
		assertEquals((int) board.getCities().get("Paris").getDiseases().get(Disease.BLUE), 3);

		// Tests Paris's neighbors have only 1 blue disease cube
		for (City c : board.getCities().get("Paris").getNeighbours().values()) {
			assertEquals((int) c.getDiseases().get(Disease.BLUE), 1);
		}

		// Tests the outbreakCounter has recorder only 1 outbreak
		assertEquals(board.getOutbreakCounter(), 1);

		// Tests all the other cities have 0 blue disease cube
		for (City c : board.getCities().values()) {
			if (!board.getCities().get("Paris").getNeighbours().values().contains(c)
					&& !c.equals(board.getCities().get("Paris")))
				assertEquals((int) c.getDiseases().get(Disease.BLUE), 0);
		}

		// London is one of Paris neighbors
		board.applyInfectorCard(new InfectorCard("London", Disease.BLUE));
		board.applyInfectorCard(new InfectorCard("London", Disease.BLUE));
		board.applyInfectorCard(new InfectorCard("London", Disease.BLUE));

		// Tests London has only 3 blue disease cubes
		assertEquals((int) board.getCities().get("London").getDiseases().get(Disease.BLUE), 3);

		// Tests Paris still has only 3 blue disease cubes
		assertEquals((int) board.getCities().get("Paris").getDiseases().get(Disease.BLUE), 3);

		/*
		 * Tests London's neighbors have now : - if they are also neighbors of Paris : 1
		 * (Paris' first outbreak) + 1 (London Outbreak) + 1 (Paris' second outbreak) =
		 * 3 blue disease cubes - if not : 1 (London outbreak) disease cube
		 */
		for (City c : board.getCities().get("London").getNeighbours().values()) {
			// Verification because the city itself is contained in
			// city.getNeighbors().values() :
			if (!c.equals(board.getCities().get("London")) && !c.equals(board.getCities().get("Paris"))) {
				if (c.getNeighbours().values().contains(board.getCities().get("Paris")))
					assertEquals((int) c.getDiseases().get(Disease.BLUE), 3);
				else
					assertEquals((int) c.getDiseases().get(Disease.BLUE), 1);
			}
		}

		// Tests the outbreakCounter has recorder now 1 + 1+ 1 = 3 outbreaks
		assertEquals(board.getOutbreakCounter(), 3);

		// Tests all the other cities have 0 blue disease cube
		for (City c : board.getCities().values()) {
			if (!board.getCities().get("Paris").getNeighbours().values().contains(c)
					&& !board.getCities().get("London").getNeighbours().values().contains(c)
					&& !c.equals(board.getCities().get("Paris")) && !c.equals(board.getCities().get("London")))
				assertEquals((int) c.getDiseases().get(Disease.BLUE), 0);
		}

	}
	
	

	/**
	 * @throws ParseException
	 * @throws XMLStreamException
	 * @throws IllegalArgumentException
	 * @throws FileNotFoundException
	 * 
	 */

	@Test
	public void outbreakLoose()
			throws FileNotFoundException, IllegalArgumentException, XMLStreamException, ParseException {
		Board board = new Board("pandemic.graphml", 0);
		PlayerCardInterface card = new InfectorCard("Atlanta", Disease.BLACK);
		for (int i = 0; i < 8; i++) {
			board.applyInfectorCard(card);

		}
		assertEquals(board.updateGameStatus(), GameStatus.DEFEATED);

	}

	/**
	 * Tests if after poll deck size is decreased 
	 * Test if the card polled isn't null
	 * 
	 * @throws FileNotFoundException
	 * @throws IllegalArgumentException
	 * @throws XMLStreamException
	 * @throws ParseException
	 * @throws UnauthorizedActionException
	 */
	@Test
	public void drawTest() throws FileNotFoundException, IllegalArgumentException, XMLStreamException, ParseException,
			UnauthorizedActionException {
		Board board = new Board("pandemic.graphml", 0);
		assertEquals(52, board.playerCardsDeck.size());
		assertTrue(!Objects.isNull(board.playerCardsDeckPoll()));
		assertEquals(51, board.playerCardsDeck.size());

	}

	@Test
	public void TreadAllDisease()
			throws FileNotFoundException, IllegalArgumentException, XMLStreamException, ParseException {
		Board board = new Board("pandemic.graphml", 0);
		board.treatAllDiseases();
		for (String city : board.getCities().keySet()) {
			for (Disease d : Disease.values()) {
				assertEquals(0,(int)board.getCity(city).getDiseases().get(d));
			}
		}
		assertEquals(24, (int) board.getDiseasesCubes().get(Disease.BLACK));
		assertEquals(24, (int) board.getDiseasesCubes().get(Disease.BLUE));
		assertEquals(24, (int) board.getDiseasesCubes().get(Disease.RED));
		assertEquals(24, (int) board.getDiseasesCubes().get(Disease.YELLOW));

	}

}
