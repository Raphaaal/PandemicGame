package fr.dauphine.ja.student.pandemiage.gameengine;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.cli.ParseException;
import fr.dauphine.ja.pandemiage.common.*;

public class Board {
	
	private final Map<String, City> cities;
	private final Map<Disease, Integer> diseasesCubes = new HashMap<>();
	private final Map<Disease, Boolean> vaccines = new HashMap<>();
	private final Map<Disease, Boolean> eradicated = new HashMap<>();
	final LinkedList<PlayerCardInterface> playerCardsDeck;
	private DefeatReason dr;
	private List<PlayerCardInterface> discardedInfectorCards;
	private LinkedList<City> copieOfOutbrokeCity=new LinkedList<>(); 
	private final LinkedList<PlayerCardInterface> infectorCardsDeck;
	private int nbEpidemy;
	private int outbreakCounter;
	private List<City> lastCitiesOutbroken;
	private boolean lackOfNeededDiseaseCubeFlag;
	private boolean lackOfNeededPlayerCard;
	private int infectionRate;

	/**
	 * Constructor for a ready-to-play Board. Initializes a Board with cities,
	 * player cards deck and infection cards deck. Finally, puts the first diseases
	 * on cities to start the game.
	 * 
	 * @param String cityGraphFileName : the name of the xml city graph to be parsed
	 * @param int difficulty : the number of Epidemy cards to be inserted into the playerCardsDeck
	 * @throw FileNotFoundException : the location of the xml file to parse the cities from
	 * @throw XMLStreamException, ParseException : from parsing
	 * @throw IllegalArgumentException : if a city RGB doesnt match the pattern for diseases coloration
	 */
	Board(String cityGraphFilename, int difficulty)throws FileNotFoundException, XMLStreamException, ParseException, IllegalArgumentException {
		
		if (difficulty < 0 || difficulty > 2)
			throw new IllegalArgumentException("difficulty must 0  1 or 2");
		
		this.outbreakCounter = 0;
		this.nbEpidemy = 0;
		this.playerCardsDeck = new LinkedList<>();
		this.infectorCardsDeck = new LinkedList<>();
		this.lackOfNeededDiseaseCubeFlag = false;
		this.lackOfNeededPlayerCard = false;
		this.infectionRate = 2;
		this.lastCitiesOutbroken = new ArrayList<>();
		this.discardedInfectorCards = new ArrayList<>();
		
		diseasesCubes.put(Disease.RED, 24);
		diseasesCubes.put(Disease.BLACK, 24);
		diseasesCubes.put(Disease.YELLOW, 24);
		diseasesCubes.put(Disease.BLUE, 24);
		vaccines.put(Disease.RED, false);
		vaccines.put(Disease.BLACK, false);
		vaccines.put(Disease.YELLOW, false);
		vaccines.put(Disease.BLUE, false);
		eradicated.put(Disease.RED, false);
		eradicated.put(Disease.BLACK, false);
		eradicated.put(Disease.YELLOW, false);
		eradicated.put(Disease.BLUE, false);

		// Parse cities from graph
		PandemicParser pp = new PandemicParser(cityGraphFilename);
		this.cities = pp.ParseCities();

		// Create PlayerCards and InfectorCards
		for (Entry<String, City> city : cities.entrySet()) {
			playerCardsDeck.add(createPlayerCardFromCity(city));
			infectorCardsDeck.add(createInfectorCardFromCity(city));
		}

		Collections.shuffle(playerCardsDeck);
		Collections.shuffle(infectorCardsDeck);

		// Insert Epidemy cards
		insertEpidemyCardsIntoPlayerCardsDeck(difficulty);

		System.out.println("--------------");
		System.out.println("Finding the first cities to be infected for initialization... ");
		System.out.println("--------------");

		// Draw the first 3 Infector cards to put 3 cubes for each city represented.
		for (int i = 0; i < 3; ++i) {
			PlayerCardInterface ic = infectorCardsDeckPoll();
			for (int j = 0; j < 3; ++j) {
				applyInfectorCard(ic);
			}
		}

		// Draw the second 3 Infector cards to put 2 cubes for each city represented.
		for (int i = 0; i < 3; ++i) {
			PlayerCardInterface ic = infectorCardsDeckPoll();
			for (int j = 0; j < 2; ++j) {
				applyInfectorCard(ic);
			}
		}

		// Draw the third 3 Infector cards to put 1 cube for each city represented.
		for (int i = 0; i < 3; ++i) {
			PlayerCardInterface ic = infectorCardsDeckPoll();

			applyInfectorCard(ic);
		}
		System.out.println("--------------");

	}

	public boolean GetlackOfNeededDiseaseCubeFlag() {
		return lackOfNeededDiseaseCubeFlag;
	}

	public boolean GetLackOfCar() {
		return lackOfNeededPlayerCard;
	}

	/**
	 * Returns the current game status. Method used by the game engine to control the game flow.
	 * 
	 * @return GameStatus : A value of the GameStatus enum : VICTORIOUS, ONGOING or DEFEATED
	 */
	public GameStatus updateGameStatus() {
		if (!vaccines.containsValue(false))
			return GameStatus.VICTORIOUS;
		else if (lackOfNeededDiseaseCubeFlag) {
			dr = DefeatReason.NO_MORE_BLOCKS;
			return GameStatus.DEFEATED;
		} else if (getOutbreakCounter() >= 8) {
			dr = DefeatReason.TOO_MANY_OUTBREAKS;
			return GameStatus.DEFEATED;

		} else if (lackOfNeededPlayerCard) {
			dr = DefeatReason.NO_MORE_PLAYER_CARDS;
			return GameStatus.DEFEATED;
		}
		return GameStatus.ONGOING;
	}

	/**
	 * Get the discarded infection cards
	 * 
	 * @return a list of the discarded infection cards.
	 */
	public List<PlayerCardInterface> getDiscardedInfectorCards() {
		return Collections.unmodifiableList(discardedInfectorCards);
	}

	/**
	 * Create a player card from an entry of the final cities Map produced by the graph parser.
	 * 
	 * @param city : the Map entry with city name as the key and City object as the value
	 * @return : the new player card corresponding to the city
	 * @throw IllegalArgumentException : if the City RGB did not match any pattern for disease coloration
	 */
	private PlayerCardInterface createPlayerCardFromCity(Entry<String, City> city) throws IllegalArgumentException {
		if (city.getValue().getR() == 153 && city.getValue().getG() == 153 && city.getValue().getB() == 153)
			return new PlayerCard(city.getKey(), Disease.BLACK);
		else if (city.getValue().getR() == 107 && city.getValue().getG() == 112 && city.getValue().getB() == 184)
			return new PlayerCard(city.getKey(), Disease.BLUE);
		else if (city.getValue().getR() == 153 && city.getValue().getG() == 18 && city.getValue().getB() == 21)
			return new PlayerCard(city.getKey(), Disease.RED);
		else if (city.getValue().getR() == 242 && city.getValue().getG() == 255 && city.getValue().getB() == 0)
			return new PlayerCard(city.getKey(), Disease.YELLOW);
		else
			throw new IllegalArgumentException("City RGB did not match patterns");
	}

	/**
	 * Create an infection card from an entry of the final cities Map produced by the graph parser.
	 * 
	 * @param city: the Map entry with city name as the key and City object as the value
	 * @return : the new infection card corresponding to the city
	 * @throw IllegalArgumentException : if the City RGB did not match any pattern for disease coloration
	 */
	private InfectorCard createInfectorCardFromCity(Entry<String, City> city) throws IllegalArgumentException {
		if (city.getValue().getR() == 153 && city.getValue().getG() == 153 && city.getValue().getB() == 153)
			return new InfectorCard(city.getKey(), Disease.BLACK);
		else if (city.getValue().getR() == 107 && city.getValue().getG() == 112 && city.getValue().getB() == 184)
			return new InfectorCard(city.getKey(), Disease.BLUE);
		else if (city.getValue().getR() == 153 && city.getValue().getG() == 18 && city.getValue().getB() == 21)
			return new InfectorCard(city.getKey(), Disease.RED);
		else if (city.getValue().getR() == 242 && city.getValue().getG() == 255 && city.getValue().getB() == 0)
			return new InfectorCard(city.getKey(), Disease.YELLOW);
		else
			throw new IllegalArgumentException("City RGB did not match patterns");
	}

	/**
	 * Insert precisely the number of Epidemy cards that corresponds to the difficulty into the pcl (Player Cards LIST)
	 * 
	 * @param difficulty
	 * 
	 */
	public void insertEpidemyCardsIntoPlayerCardsDeck(int difficulty) {

		int startIndex = 0;
		int gap = playerCardsDeck.size() / (difficulty + 4);
		for (int i = 0; i < difficulty + 4; i++) {
			Random r = new Random();

			int k = r.nextInt(gap - 1) + startIndex;
			playerCardsDeck.add(k, new PlayerCard("EPIDEMY", null));
			startIndex = startIndex + gap;
		}
	}

	/**
	 * Return the current infection rate value. The infection rate impacts the number of infection cards to be drawn after each turn.
	 * 
	 * @return : the int value of the current infection rate
	 */
	public int getInfectionRate() {
		return this.infectionRate;
	}

	/**
	 * Increase the infection rate value progressively, from TWO to FOUR, by using a counter.
	 */
	private void increaseInfectionRate() {
		if (nbEpidemy < 3)
			this.infectionRate = 2;
		if (nbEpidemy >= 3 && nbEpidemy < 5)
			this.infectionRate = 3;
		if (nbEpidemy >= 5)
			this.infectionRate = 4;

	}

	/**
	 * To get the number of outbreaks that have appeared in the game.
	 * 
	 * @return the current number of outbreaks that have appeared
	 */
	public int getOutbreakCounter() {
		return outbreakCounter;
	}

	/**
	 * Return a view of all the boards' cities Map, accessible by their city name.
	 */
	public Map<String, City> getCities() {
		return Collections.unmodifiableMap(cities);
	}

	/**
	 * Return a specific city of the board given its city name.
	 */
	public City getCity(String cityName) {
		return cities.get(Objects.requireNonNull(cityName));
	}

	/**
	 * Indicate that a vaccine for a specific disease has been found.
	 * 
	 * @param disease : the disease whose vaccine has been found
	 */
	public void setVaccine(Disease disease) {
		vaccines.put(Objects.requireNonNull(disease), true);
	}

	/**
	 * Return the current state of all vaccines
	 */
	public Map<Disease, Boolean> getVaccines() {
		return Collections.unmodifiableMap(vaccines);
	}

	/**
	 * Return the current state of a vaccine for a specific disease (true if found / false if not found).
	 * 
	 * @param disease : the disease whose vaccine state needs to be checked
	 */
	public boolean getVaccine(Disease disease) {
		return vaccines.get(Objects.requireNonNull(disease));
	}

	/**
	 * Indicate that a specific disease has been eradicated.
	 * 
	 * @param disease : the disease which is eradicated
	 */
	public void setEradicated(Disease disease) {
		eradicated.put(Objects.requireNonNull(disease), true);
	}

	/**
	 * Return the current state of all diseases (eradicated or not)
	 * 
	 * @return the Map containing the information for each disease
	 */
	public Map<Disease, Boolean> getEradicated() {
		return Collections.unmodifiableMap(eradicated);
	}

	// PLAYER RELATED METHODS \\

	public int getPlayerCardsSize() {
		return playerCardsDeck.size();
	}


	public PlayerCardInterface playerCardsDeckPoll() {
		if (playerCardsDeck.size() == 0) {
			lackOfNeededPlayerCard = true;
		}

		return playerCardsDeck.poll();
	}

	public void applyPlayerCard(PlayerCardInterface tmp) {
		// If Epidemy card
		if (tmp.getCityName().equalsIgnoreCase("EPIDEMY")) {
			System.out.println("You drew an Epidemy Card");
			infectEpidemy();
		}
	}

	/**
	 * Apply an epidemy (when an Epidemy card has been drawm by a player) by drawing
	 * and applying 3 times the last Infection Card from the infectorCardsDeck and
	 * finally recomposing the infectorCardsDeck.
	 */
	public void infectEpidemy() {
		nbEpidemy++;

		increaseInfectionRate();
		PlayerCardInterface ic = infectorCardsDeck.pollLast();
		// Add 3 diseases on corresponding city
		for (int i = 0; i < 3; ++i)
			applyInfectorCard(ic);
		// Add the shuffled discardedInfectorCards on top of the infectorCardsDeck
		Collections.shuffle(discardedInfectorCards);

		for (PlayerCardInterface c : discardedInfectorCards)
			infectorCardsDeck.offerFirst(c);
		discardedInfectorCards.clear();
	}

	// INFECTOR RELATED METHODS \\

	public int getInfectorCardsSize() {
		return infectorCardsDeck.size();
	}

	public PlayerCardInterface infectorCardsDeckPoll() {
		return infectorCardsDeck.poll();
	}

	/**
	 * Add disease cube to a specific city, taking care of possible outbreaks.
	 * 
	 * @param city : the city that receives the disease
	 * @param disease : the disease to be added to the city
	 */
	private void addDisease(City city, Disease disease) {
		if (!eradicated.get(disease)) {
			if (diseasesCubes.get(disease) > 0) {
				// if outbreak
				if (city.getDiseases().get(disease) >= 3) {
					++outbreakCounter;
					System.out.println("Outbreak detected!");
					applyChainReaction(city, disease);
					
					// Not thread-safe 
					copieOfOutbrokeCity.addAll(lastCitiesOutbroken);
					lastCitiesOutbroken.clear();
				} else {
					city.addDisease(disease);
					System.out.println("Disease " + disease + " infected " + city.getLabel());
					diseasesCubes.put(disease, diseasesCubes.get(disease) - 1);
				}
			} else
				lackOfNeededDiseaseCubeFlag = true;
		}
	}

	/**
	 * Add the disease corresponding to a specific infection card to the corresponding city.
	 * 
	 * @param ic : the infection card to be applied.
	 */
	public void applyInfectorCard(PlayerCardInterface ic) {
		if (!discardedInfectorCards.contains(ic)) {
			discardedInfectorCards.add(ic);
		}
		City city = cities.get(ic.getCityName());
		Disease disease = ic.getDisease();
		addDisease(city, disease);
	}

	/**
	 * Propagate the outbreak of a disease in a city, in chain reaction.
	 * 
	 * @param city : the city from which the chain reaction starts
	 * @param disease : the disease to be propagated
	 */
	public void applyChainReaction(City city, Disease disease) {
		lastCitiesOutbroken.add(city);
		System.out.println(lastCitiesOutbroken);
		System.out.println(city.getNeighbours().values());
		for (City c : city.getNeighbours().values()) {
			if (diseasesCubes.get(disease) > 0)
				if (!lastCitiesOutbroken.contains(c)) {
					if (c.getDiseases().get(disease) >= 3) {
						++outbreakCounter;
						System.out.println("Outbreak detected!");
						applyChainReaction(c, disease);
					} else {
						c.addDisease(disease);
						this.diseasesCubes.put(disease, diseasesCubes.get(disease) - 1);

						System.out.println("Disease " + disease + " infected " + c.getLabel());
					}
				}
		}
	}

	public void drawInfectorCard() {
		PlayerCardInterface card=infectorCardsDeckPoll();
		applyInfectorCard(card);
	}

	/**
	 * @return the card on top of the deck
	 * @throws UnauthorizedActionException
	 */
	public PlayerCardInterface draw() throws UnauthorizedActionException {
		if (this.playerCardsDeck.size() < 1) {
			this.lackOfNeededPlayerCard = true;
			throw new UnauthorizedActionException("the deck is empty");
		}
		return this.playerCardsDeckPoll();
	}

	public DefeatReason getDr() {
		return dr;
	}

	public void removeDisease(String cityName, Disease d) {
		this.diseasesCubes.put(d, diseasesCubes.get(d) + 1);
		cities.get(cityName).removeDisease(d);
	}

	/**
	 * This function puts every disease at 0 in every city that is used for Board unit test (chain reaction)
	 */
	public void treatAllDiseases() {
		for (String cityName : cities.keySet())
			for (Disease d : Disease.values())
				while (cities.get(cityName).getDiseases().get(d) > 0)
					removeDisease(cityName, d);
	}

	public Map<Disease, Integer> getDiseasesCubes() {
		return Collections.unmodifiableMap(this.diseasesCubes);
	}
	
	public Collection<City> getLastCitiesOutbroken() {
		return Collections.unmodifiableCollection(this.copieOfOutbrokeCity);
	}

}
