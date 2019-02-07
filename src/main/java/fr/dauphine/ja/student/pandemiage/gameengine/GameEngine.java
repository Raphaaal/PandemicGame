package fr.dauphine.ja.student.pandemiage.gameengine;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import fr.dauphine.ja.student.pandemiage.gameengine.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.ParseException;

import fr.dauphine.ja.pandemiage.common.AiInterface;
import fr.dauphine.ja.pandemiage.common.AiLoader;
import fr.dauphine.ja.pandemiage.common.DefeatReason;
import fr.dauphine.ja.pandemiage.common.Disease;
import fr.dauphine.ja.pandemiage.common.GameInterface;
import fr.dauphine.ja.pandemiage.common.GameStatus;
import fr.dauphine.ja.pandemiage.common.PlayerCardInterface;
import fr.dauphine.ja.pandemiage.common.PlayerInterface;
import fr.dauphine.ja.pandemiage.common.UnauthorizedActionException;

/**
 * GameEngine implementing GameInterface
 *
 */

public class GameEngine implements GameInterface {

	private final String aiJar;
	private final String cityGraphFilename;
	private GameStatus gameStatus;
	private final Board board;
	private final int turnDuration;
	private final int handSize;
	private final List<PlayerInterface> players;
	private final int nbPlayer;
	private int nbTurns;
	private int nbEpidemy;

	// Do not change!
	private void setDefeated(String msg, DefeatReason dr) {
		gameStatus = GameStatus.DEFEATED;
		System.err.println("Player(s) have been defeated: " + msg);
		System.err.println("Result: " + gameStatus);
		System.err.println("Reason: " + dr);
		printGameStats();
		System.exit(2);
	}

	// Do not change!
	private void setVictorious() {
		gameStatus = GameStatus.VICTORIOUS;
		System.err.println("Player(s) have won.");
		System.err.println("Result: " + gameStatus);
		printGameStats();
		System.exit(0);
	}

	// Do not change!
	private void printGameStats() {
		Map<Disease, Integer> blocks = new HashMap<>();
		for (String city : allCityNames()) {
			for (Disease d : Disease.values()) {
				blocks.put(d, blocks.getOrDefault(d, 0) + infectionLevel(city, d));
			}
		}
		System.err.println(blocks);
		System.err.println("Infection-rate:" + infectionRate());
		for (Disease d : Disease.values()) {
			System.err.println("Cured-" + d + ":" + isCured(d));
		}
		System.err.println("Nb-outbreaks:" + getNbOutbreaks());
		System.err.println("Nb-player-cards-left:" + getNbPlayerCardsLeft());
	}

	/**
	 * Constructor for solo Game
	 * 
	 * @param cityGraphFilename
	 * @param aiJar
	 * @param difficulty
	 * @param handSize
	 * @param turnDuration
	 * @throws FileNotFoundException
	 * @throws IllegalArgumentException
	 * @throws XMLStreamException
	 * @throws ParseException
	 * @throws UnauthorizedActionException
	 */
	public GameEngine(String cityGraphFilename, String aiJar, int difficulty, int handSize, int turnDuration,int nbPlayer)
			throws FileNotFoundException, IllegalArgumentException, XMLStreamException, ParseException,
			UnauthorizedActionException {
		if (difficulty < 0 || difficulty>2) 
			throw new IllegalArgumentException("difficulty must be 0,1 or 2");
		if( handSize < 5)
			throw new IllegalArgumentException("handSize must be superior to 4 ");
		if(turnDuration < 0)
			throw new IllegalArgumentException("turnDuration must be positive");
		if(nbPlayer<1 ||nbPlayer>4)
			throw new IllegalArgumentException("nbPlayer must be between 1 and 4");

		this.cityGraphFilename = cityGraphFilename;
		this.aiJar = aiJar;
		this.gameStatus = GameStatus.ONGOING;
		this.board = new Board(cityGraphFilename, difficulty);
		this.turnDuration = turnDuration;

		this.handSize = handSize;
		// Initial draw for players.get(0)
		System.out.println("Drawing your first cards...");
		System.out.println("--------------");
		
		this.players = new ArrayList<>();
		this.nbPlayer=nbPlayer;
		for(int i=0;i<nbPlayer;i++) {
			this.players.add(new Player(getBoard().getCities().get("Atlanta"), getBoard()));
			
			// Draw  card to initialize hand
			for (int j= 0; j < 6-nbPlayer; j++)
				draw(players.get(i));
			
			System.out.println(players.get(i).playerHand());
		}
	}

	/**
	 * Standard game (one player)
	 * 
	 * @param cityGraphFilename
	 * @param aiJar
	 * @param difficulty
	 * @param handSize
	 * @param turnDuration
	 * @throws FileNotFoundException
	 * @throws IllegalArgumentException
	 * @throws XMLStreamException
	 * @throws ParseException
	 * @throws UnauthorizedActionException
	 */
	public GameEngine(String cityGraphFilename, String aiJar, int difficulty, int handSize, int turnDuration) throws FileNotFoundException, IllegalArgumentException, XMLStreamException, ParseException, UnauthorizedActionException {
		this(cityGraphFilename,aiJar,difficulty,handSize,turnDuration,1);
	}

	/**
	 * Standard (one player) loop
	 * 
	 * 
	 * 
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 * @throws ParseException
	 * @throws UnauthorizedActionException
	 */
	public void loopPlayer() throws FileNotFoundException, XMLStreamException, ParseException, UnauthorizedActionException {
		System.out.println("default game is started");

		while (gameStatus == GameStatus.ONGOING) {

			((Player) players.get(0)).playTurn();
			gameStatus = getBoard().updateGameStatus();

			System.out.println("--------------");
			System.out.println("Drawing infector cards...");
			System.out.println("--------------");
			for (int j = 0; j < getBoard().getInfectionRate(); ++j) {
				getBoard().drawInfectorCard();
				gameStatus = getBoard().updateGameStatus();
			}
			
			System.out.println("--------------");
			System.out.println("Drawing your cards...");
			System.out.println("--------------");
			draw(players.get(0));
			draw(players.get(0));
			
			if (players.get(0).playerHand().size() > this.handSize)
				((Player) players.get(0)).discard(players.get(0).playerHand().size() - this.handSize - 1);
			gameStatus = getBoard().updateGameStatus();
			
		}
		if (gameStatus == GameStatus.DEFEATED)
			setDefeated(nbTurns + " turns", getBoard().getDr());
		else
			setVictorious();
	}
	
	
	/**
	 * Standard AI loop
	 * Start a game where turn are played by the AI
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 * @throws ParseException
	 */
	public void loopAI() throws FileNotFoundException, XMLStreamException, ParseException {
		// Load Ai from Jar file
		System.out.println("--------------");
		System.out.println("Loading AI Jar file " + aiJar);
		System.out.println("--------------");
		System.out.println("");
		System.out.println("================");
		System.out.println("");
		AiInterface ai = AiLoader.loadAi(aiJar);

		while (gameStatus == GameStatus.ONGOING) {

			System.out.println(players.get(0).playerHand());
			ai.playTurn(this, players.get(0));
			gameStatus = getBoard().updateGameStatus();

			System.out.println("--------------");
			System.out.println("Drawing infector cards...");
			System.out.println("--------------");

			for (int j = 0; j < getBoard().getInfectionRate(); ++j) {
				getBoard().drawInfectorCard();
				gameStatus = getBoard().updateGameStatus();

			}
			System.out.println("--------------");
			System.out.println("Drawing your cards...");
			System.out.println("--------------");
			int nbEpidemicCards = 0;
			try {
				nbEpidemicCards += draw(players.get(0));
				nbEpidemicCards += draw(players.get(0));

			} catch (UnauthorizedActionException e) {
				System.out.println(e.getMessage());
			}

			players.get(0).playerHand().removeAll(ai.discard(this, players.get(0), 9, nbEpidemicCards));
			gameStatus = getBoard().updateGameStatus();

			++nbTurns;

		}
		System.out.println(this.getBoard().getCities());

		if (gameStatus == GameStatus.DEFEATED)

			setDefeated(nbTurns + " turns", getBoard().getDr());
		else
			setVictorious();
	}

	/**
	 * This loop is used to test the AI on the game without printing results statistics.
	 * Every turn is played by the AI
	 * 
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 * @throws ParseException
	 */
	public void loopWithoutGameResults() throws FileNotFoundException, XMLStreamException, ParseException {
		// Load Ai from Jar file
		System.out.println("--------------");
		System.out.println("Loading AI Jar file " + aiJar);
		System.out.println("--------------");
		System.out.println("");
		System.out.println("================");
		System.out.println("");

		AiInterface ai = AiLoader.loadAi(aiJar);

		while (gameStatus == GameStatus.ONGOING) {

			System.out.println("AI Play");
			ai.playTurn(this, players.get(0));
			System.out.println("AI played");
			gameStatus = getBoard().updateGameStatus();

			if (gameStatus == GameStatus.ONGOING) {
				System.out.println("--------------");
				System.out.println("Drawing infector cards...");
				System.out.println("--------------");
				
				for (int j = 0; j < getBoard().getInfectionRate(); ++j) {
					if (getBoard().getInfectorCardsSize() > 0) {
						getBoard().drawInfectorCard();

						gameStatus = getBoard().updateGameStatus();
					} else {
						gameStatus = GameStatus.DEFEATED;
					}

				}
				if (gameStatus == GameStatus.ONGOING) {
					System.out.println("--------------");
					System.out.println("Drawing your cards...");
					System.out.println("--------------");
					int nbEpidemicCards = 0;

					try {
						nbEpidemicCards += draw(players.get(0));
						nbEpidemicCards += draw(players.get(0));
						players.get(0).playerHand().removeAll(ai.discard(this, players.get(0), 9, nbEpidemicCards));

					} catch (UnauthorizedActionException e) {
						this.gameStatus = GameStatus.DEFEATED;

					}
				}

				gameStatus = getBoard().updateGameStatus();

				++nbTurns;

			}
		}
		System.out.println(this.gameStatus);
	}
	
	
	/**
	 * Loop for playing in multiplayer mode.
	 * 
	 * @throws UnauthorizedActionException
	 */
	public void multiPlayerLoop() throws UnauthorizedActionException {
		
		System.out.println("default game is started");
		
		int currentPlayer=0;
		while (gameStatus == GameStatus.ONGOING) {

			((Player) players.get(currentPlayer)).playTurn();
			gameStatus = getBoard().updateGameStatus();

			System.out.println("--------------");
			System.out.println("Drawing infector cards...");
			System.out.println("--------------");
			
			for (int j = 0; j < getBoard().getInfectionRate(); ++j) {
				getBoard().drawInfectorCard();
				gameStatus = getBoard().updateGameStatus();
			}
			
			System.out.println("--------------");
			System.out.println("Drawing your cards...");
			System.out.println("--------------");
			draw(players.get(currentPlayer));
			draw(players.get(currentPlayer));
			
			if (players.get(currentPlayer).playerHand().size() > this.handSize)
				((Player) players.get(currentPlayer)).discard(players.get(currentPlayer).playerHand().size() - this.handSize - 1);
			gameStatus = getBoard().updateGameStatus();
			currentPlayer++;
			if(currentPlayer==nbPlayer)
				currentPlayer=0;
		}
		if (gameStatus == GameStatus.DEFEATED)
			setDefeated(nbTurns + " turns", getBoard().getDr());
		else
			setVictorious();
	}
	
	/**
	 * Method used to display the player location on the GUI.
	 *
	 * @param player not null
	 * @return
	 * @throws UnauthorizedActionException
	 */
	public int draw(PlayerInterface player) throws UnauthorizedActionException {
		Objects.requireNonNull(player);

		PlayerCardInterface pc = getBoard().draw();
		
		if (pc.getCityName().equalsIgnoreCase("EPIDEMY")) {
			getBoard().applyPlayerCard(pc);
			return 1;
		}

		player.playerHand().add(pc);

		return 0;
	}

	/**
	 * Get the city the player is located in. Currently works only with
	 * players.get(0).
	 * 
	 * @param p the player for which we need the location
	 * @return the city the player is located in
	 */
	public String getPlayerLocation() {
		return players.get(0).playerLocation();

	}

	@Override
	public List<String> getDiscardedInfectionCards() {
		List<PlayerCardInterface> l = getBoard().getDiscardedInfectorCards();
		List<String> result = new ArrayList<>();
		for (PlayerCardInterface ic : l) {
			result.add(ic.getCityName());
		}
		return result;
	}

	@Override
	public List<String> allCityNames() {

		return new ArrayList<>(this.getBoard().getCities().keySet());

	}

	@Override
	public List<String> neighbours(String cityName) {
		return new ArrayList<>(getBoard().getCities().get(Objects.requireNonNull(cityName)).getNeighbours().keySet());
	}

	@Override
	public int infectionLevel(String cityName, Disease d) {
		return this.getBoard().getCities().get(cityName).getDiseases().get(d);
	}

	@Override
	public boolean isCured(Disease disease) {
		return this.getBoard().getVaccine(disease);

	}

	@Override
	public int infectionRate() {
		return this.getBoard().getInfectionRate();
	}

	@Override
	public GameStatus gameStatus() {

		return this.gameStatus;
	}

	@Override
	public int turnDuration() {
		return this.turnDuration;
	}

	@Override
	public boolean isEradicated(Disease disease) {
		return this.getBoard().getEradicated().containsKey(disease);
	}

	@Override
	public int getNbOutbreaks() {
		return this.getBoard().getOutbreakCounter();

	}

	@Override
	public int getNbPlayerCardsLeft() {
		return getBoard().getPlayerCardsSize();
	}

	/**
	 * Get number of cured diseases in the game.
	 * 
	 * @return the number of cured diseases
	 */
	public int getCuredPerf() {
		int countCured = 0;
		for (Disease d : Disease.values()) {
			if (isCured(d))
				++countCured;
		}
		return countCured;
	}

	public List<PlayerCardInterface> getHandPlayer() {
		return Collections.unmodifiableList(players.get(0).playerHand());
	}

	public PlayerInterface getPlayer() {
		return this.players.get(0);
	}

	public Board getBoard() {
		return board;
	}

	public boolean getlackOfNeededDiseaseCubeFlag() {

		return board.GetlackOfNeededDiseaseCubeFlag();
	}

	public boolean getLackOfCard() {
		return board.GetLackOfCar();
	}

}
