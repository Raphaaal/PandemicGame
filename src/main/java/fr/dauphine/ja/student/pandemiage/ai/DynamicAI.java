package fr.dauphine.ja.student.pandemiage.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import fr.dauphine.ja.pandemiage.common.AiInterface;
import fr.dauphine.ja.pandemiage.common.Disease;
import fr.dauphine.ja.pandemiage.common.GameInterface;
import fr.dauphine.ja.pandemiage.common.PlayerCardInterface;
import fr.dauphine.ja.pandemiage.common.PlayerInterface;
import fr.dauphine.ja.pandemiage.common.UnauthorizedActionException;

public class DynamicAI implements AiInterface {

	/**
	 * Get all the legal actions a player can perform in a specified GameInterface.
	 * 
	 * @param g the GameEngine to play on
	 * @param p the Player to play with
	 * @return List<List<String>> Returns a list of lists, each containing the name of a possible action (and the city associated if needed)
	 */
	private List<List<String>> getPossibleActions(GameInterface g, PlayerInterface p) {
		List<List<String>> actions = new ArrayList<>();

		// treat actions
		for (Disease d : Disease.values()) {
			if (g.infectionLevel(p.playerLocation(), d) > 0) {
				List<String> action = new ArrayList<>();
				action.add("treat");
				action.add(Objects.requireNonNull(p.playerLocation(), "Location cannot be null"));
				action.add(Objects.requireNonNull(d.toString(), "Disease to treat cannot be null"));
				actions.add(action);
			}
		}

		// flyActions
		for (PlayerCardInterface pc : p.playerHand()) {
			if (!pc.getCityName().equals(p.playerLocation()) && !(usedDisease.get(pc.getDisease())>5) ) {
				List<String> action = new ArrayList<>();
				action.add("fly");
				action.add(pc.getCityName());
				actions.add(action);
			}
		}

		// charterFy actions
		if (canCharterFly(p)) {
			for (String city : g.allCityNames()) {
				if (!city.equals(p.playerLocation()) && !g.neighbours(p.playerLocation()).contains(city) ) {
					List<String> action = new ArrayList<>();
					action.add("charter");
					action.add(city);
					actions.add(action);
				}
			}
		}

		// move actions
		for (String city : g.neighbours(p.playerLocation())) {
			List<String> action = new ArrayList<>();
			action.add("move");
			action.add(city);
			actions.add(action);
		}
		return actions;
	}

	/**
	 * Get the best next action (according to the eval() subroutine) a player can perform on a given game interface.
	 * For this, each possible action will be simulated on a copy of the game engine (given all accessible information on it).
	 * Each of these new state is evaluated to identify the best one, thus the best action.
	 * 
	 * @param g the game interface to perform the action on
	 * @param p the player that have to perform the action
	 * @return
	 */
	public List<String> getBestAction(GameInterface g, PlayerInterface p) {
		
		List<List<String>> actions = getPossibleActions(g, p);
		int minEval = 9999;
		List<String> bestAction = null;
		
		for (List<String> action : actions) {
			FakeGame fg = new FakeGame(g, p);
			fg.applyAction(action);
			int eval = fg.eval();
			if (eval < minEval) {
				bestAction = action;
				minEval = eval;
			}

		}
		return bestAction;
	}



	private int nbActions;
	private final List<PlayerCardInterface> used;
	private final Map<Disease, Integer> usedDisease;

	public DynamicAI() {
		this.nbActions = 0;
		this.used = new ArrayList<>();
		this.usedDisease = new HashMap<>();
		for (Disease d : Disease.values()) {
			usedDisease.put(d, 0);
		}
	}

	private List<Disease> getAllDiseases() {
		List<Disease> diseases = new LinkedList<>();
		diseases.add(Disease.BLACK);
		diseases.add(Disease.BLUE);
		diseases.add(Disease.RED);
		diseases.add(Disease.YELLOW);
		return diseases;
	}

	public boolean treatDanger(GameInterface g, PlayerInterface p, int danger) throws UnauthorizedActionException {
		boolean treated = false;
		for (Disease d : Disease.values()) {
			if (g.infectionLevel(p.playerLocation(), d) > danger && this.nbActions > 0) {
				p.treatDisease(d);
				treated = true;
			}
		}
		return treated;
	}

	public boolean moveToDanger(GameInterface g, PlayerInterface p, int danger) throws UnauthorizedActionException {
		for (String city : g.neighbours(p.playerLocation())) {
			for (Disease d : Disease.values()) {

				if (g.infectionLevel(city, d) > danger && g.isCured(d)) {

					if (canMove(p, g, city)) {
						p.moveTo(city);
						return true;
					}
				}

				if (g.infectionLevel(city, d) > danger) {
					if (canMove(p, g, city)) {
						p.moveTo(city);
						return true;
					}

				}

			}
		}
		return false;

	}

	public boolean tryCure(PlayerInterface p, GameInterface g) throws UnauthorizedActionException {
		if (p.playerHand().size() >= 5) {
			List<PlayerCardInterface> blueCards = new ArrayList<>();
			List<PlayerCardInterface> yellowCards = new ArrayList<>();
			List<PlayerCardInterface> blackCards = new ArrayList<>();
			List<PlayerCardInterface> redCards = new ArrayList<>();

			for (PlayerCardInterface card : p.playerHand()) {
				if (card.getDisease() != null && !g.isCured(card.getDisease())) {
					if (card.getDisease() == Disease.BLACK) {
						blackCards.add(card);
						if (blackCards.size() == 5) {
							p.discoverCure(blackCards);
							return true;
						}

					}
					if (card.getDisease() == Disease.RED) {
						redCards.add(card);
						if (redCards.size() == 5) {
							p.discoverCure(redCards);
							return true;
						}

					}
					if (card.getDisease() == Disease.YELLOW) {
						yellowCards.add(card);
						if (yellowCards.size() == 5) {
							p.discoverCure(yellowCards);
							return true;
						}
					}

					if (card.getDisease() == Disease.BLUE) {
						blueCards.add(card);
						if (blueCards.size() == 5) {
							p.discoverCure(blueCards);
							return true;
						}
					}

				}
			}
		}
		return false;

	}

	/**
	 * This basic AI will try to use its 4 actions by trying to :
	 * 
	 *  1. Discover the cure of an uncured disease
	 *  2. Treat as many diseases as possible in its location 
	 *  3. Move to a random neighbour
	 * 
	 * Print Exception stack in last resort
	 * 
	 * @param g  the gameEmgine for the AI to play on
	 * @param p the player for the AI to play
	 */
	@Override
	public void playTurn(GameInterface g, PlayerInterface p) {

		this.nbActions = 4;

		while (nbActions > 0) {
			try {
				if (!tryCure(p, g)) {
					List<String> action = getBestAction(g, p);
					System.out.println("CHOSEN ACTION : " + action);
					try {
						applyAction(action, g, p);

					} catch (UnauthorizedActionException e) {

						throw new IllegalStateException(e);

					}
				}
			} catch (UnauthorizedActionException e) {

				throw new IllegalStateException(e);
			}
			nbActions--;

		}

	}

	/**
	 * Performs a given action on a specific GameInterface for a given Player.
	 * 
	 * @param action a list of action which contains the name of action in index 0,the city in index 1 and (optional) the disease on index 3
	 * @param g the GameEngine to perform the action on
	 * @param p the player
	 * @throws UnauthorizedActionException
	 */
	private void applyAction(List<String> action, GameInterface g, PlayerInterface p) throws UnauthorizedActionException {
		
		// Action treat
		if (action.get(0).equals("treat"))
			p.treatDisease(Disease.valueOf(action.get(2)));
		
		if (action.get(0).equals("fly")) {
			for (PlayerCardInterface card : p.playerHand()) {
				if (card.getCityName().equals(action.get(1))) {
					this.used.add(card);
					this.usedDisease.put(card.getDisease(), this.usedDisease.get(card.getDisease()) + 1);
				}
			}
			p.flyTo(action.get(1));
		}
		
		if (action.get(0).equals("charter")) {
			for (PlayerCardInterface card : p.playerHand()) {
				if (card.getCityName().equals(p.playerLocation())) {
					this.used.add(card);
					this.usedDisease.put(card.getDisease(), this.usedDisease.get(card.getDisease()) + 1);
				}

			}

			p.flyToCharter(action.get(1));
		}

		if (action.get(0).equals("move"))
			p.moveTo(action.get(1));

	}

	@Override
	public List<PlayerCardInterface> discard(GameInterface g, PlayerInterface p, int maxHandSize, int nbEpidemic) {
		List<PlayerCardInterface> discard = new ArrayList<>();
		while (maxHandSize < p.playerHand().size() - discard.size()) {

			Disease minD = minDisease(p.playerHand(), g, discard);
			int search = 0;
			while (search < p.playerHand().size() - 1 && (p.playerHand().get(search).getDisease() != minD
					&& g.allCityNames().contains(p.playerHand().get(search).getCityName())
					|| discard.contains(p.playerHand().get(search)))) {
				search++;
			}
			discard.add(p.playerHand().get(search));
		}
		this.used.addAll(discard);
		for (PlayerCardInterface pc : discard) {
			this.usedDisease.put(pc.getDisease(), usedDisease.get(pc.getDisease()) + 1);
		}
		return discard;
	}

	public Disease minDisease(List<PlayerCardInterface> cards, GameInterface g, List<PlayerCardInterface> discarded) {
		Map<Disease, Integer> diseasehand = new HashMap<>();

		for (PlayerCardInterface pc : cards) {
			if (g.allCityNames().contains(pc.getCityName())) {
				if (!discarded.contains(pc)) {
					if (g.isCured(pc.getDisease())) {
						return pc.getDisease();
					}
					if (!diseasehand.containsKey(pc.getDisease()))
						diseasehand.put(pc.getDisease(), 1);
					else
						diseasehand.put(pc.getDisease(), diseasehand.get(pc.getDisease()) + 1);

				}

			}
		}
		Disease minD = null;

		int count = cards.size() + 1;
		for (Disease d : diseasehand.keySet()) {
			if (diseasehand.get(d) < count && usedDisease.get(d) < 5)
				minD = d;
		}
		if (minD == null) {
			minD = diseasehand.keySet().iterator().next();

		}
		return minD;

	}

	private PlayerCardInterface getMinFreqCard(Map<PlayerCardInterface, Integer> map, Set<PlayerCardInterface> set) {
		PlayerCardInterface minKey = null;
		int minValue = Integer.MAX_VALUE;
		for (PlayerCardInterface key : set) {
			int value = map.get(key);
			if (value < minValue) {
				minValue = value;
				minKey = key;
			}
		}
		return minKey;
	}

	public boolean ManageCubeDanger(GameInterface g, int dangerCube, PlayerInterface p)
			throws UnauthorizedActionException {
		Map<Disease, Integer> cubes = new HashMap<>();
		for (Disease d : Disease.values())
			cubes.put(d, 0);
		for (String cityName : g.allCityNames())
			for (Disease d : Disease.values()) {
				cubes.put(d, cubes.get(d) + g.infectionLevel(cityName, d));
				if (cubes.get(d) >= dangerCube) {
					return FocusDisease(g, d, p);

				}

			}

		return false;
	}

	private boolean FocusDisease(GameInterface g, Disease d, PlayerInterface p) throws UnauthorizedActionException {
		List<String> twooCube = new ArrayList<>();
		List<String> oneCube = new ArrayList<>();

		for (String cityName : g.allCityNames()) {
			if (g.infectionLevel(cityName, d) == 3)
				if (focusCity(cityName, g, p))
					return true;
			if (g.infectionLevel(cityName, d) == 2)
				twooCube.add(cityName);
			if (g.infectionLevel(cityName, d) == 1)
				oneCube.add(cityName);
		}
		for (String cityName : twooCube)
			if (focusCity(cityName, g, p))
				return true;
		for (String cityName : oneCube)
			if (focusCity(cityName, g, p))
				return true;

		return false;

	}

	/**
	 * test if the player can move to the city in param
	 * 
	 * @param p
	 * @param g
	 * @param city
	 * @return
	 */

	public boolean canMove(PlayerInterface p, GameInterface g, String city) {
		return g.neighbours(p.playerLocation()).contains(city);
	}

	/**
	 * test if the player can use charterFly action
	 * 
	 * @param p
	 * @return
	 */
	public boolean canCharterFly(PlayerInterface p) {
		for (PlayerCardInterface pc : p.playerHand())
			//if (pc.getCityName().equals(p.playerLocation()) && this.usedDisease.get(pc.getDisease())>5)
			if (pc.getCityName().equals(p.playerLocation()) && !(this.usedDisease.get(pc.getDisease())>5))

				return true;

		return false;
	}

	/**
	 * test if if canFlyTo
	 * 
	 * @param p
	 * @param city
	 * @return
	 */
	public boolean canFlyTo(PlayerInterface p, String city) {
		for (PlayerCardInterface card : p.playerHand())
			if (card.getCityName().equals(city))
				return true;
		return false;
	}

	public boolean charterFlyDanger(PlayerInterface p, GameInterface g, int danger) throws UnauthorizedActionException {
		for (String city : g.allCityNames()) {
			for (Disease d : Disease.values())
				if (canCharterFly(p) && g.infectionLevel(city, d) > danger) {
					p.flyToCharter(city);

				}
			return true;
		}
		return false;

	}

	public boolean focusCity(String cityName, GameInterface g, PlayerInterface p) throws UnauthorizedActionException {
		PlayerCardInterface pc = null;

		if (canMove(p, g, cityName)) {
			p.moveTo(cityName);
			return true;
		}

		if (canFlyTo(p, cityName)) {

			p.flyTo(cityName);
			return true;
		}

		if (canCharterFly(p)) {
			p.flyToCharter(cityName);

			return true;
		}

		return false;
	}

	public boolean flyToDanger(GameInterface g, PlayerInterface p, int danger) throws UnauthorizedActionException {
		List<String> citiesDanger = searchDanger(g, danger);
		String target = "";
		while (!citiesDanger.isEmpty()) {
			target = theMostDangerous(citiesDanger, g);
			if (canFlyTo(p, target)) {

				p.flyTo(target);

				return true;
			}

			citiesDanger.remove(target);
		}
		return false;
	}

	public String theMostDangerous(List<String> cities, GameInterface g) {
		int max = 0;
		String theDanger = "";
		for (String cityName : cities) {
			if (g.neighbours(cityName).size() > max) {
				max = g.neighbours(cityName).size();
				theDanger = cityName;
			}
		}
		return theDanger;
	}

	public List<String> searchDanger(GameInterface g, int danger) {
		List<String> citiesDanger = new ArrayList<>();

		for (String cityName : g.allCityNames()) {
			for (Disease d : Disease.values())
				if (g.infectionLevel(cityName, d) > danger) {
					citiesDanger.add(cityName);
				}
		}
		return citiesDanger;
	}

	public class FakeGame {
		private Map<String, Map<Disease, Integer>> infectionLevel;
		private Map<Disease, Boolean> diseaseCured;
		private Map<Disease, Boolean> diseasesEradicated;
		private String fakeLocation;
		private int eval;
		private int nbCardUsed = 0;
		private GameInterface game;
		private List<List<String>> actions;
		private Map<Disease, Integer> nbDiseaseBlocksLeft;
		private int nbCloseOutbreaks;
		private int nbCloseOutbreaksAfterAction;
		
		// eval(fakeGame) pour evaluer un etat
		// apply(fakeAction)

		// faire toutes les actions

		/**
		 * ccreate a fake gameeIn
		 * 
		 * @param g
		 */

		public FakeGame(GameInterface g, PlayerInterface p) {
			this.infectionLevel = new HashMap<>();
			this.diseaseCured = new HashMap<>();
			this.diseasesEradicated = new HashMap<>();
			this.fakeLocation = p.playerLocation();
			this.nbDiseaseBlocksLeft = new HashMap<>();
			this.nbCloseOutbreaks = 0;
			this.nbCloseOutbreaksAfterAction = 0;

			this.game = g;
			
			
			for (Disease d : Disease.values()) {
				nbDiseaseBlocksLeft.put(d, 24);
			}
			
			for (String city : g.allCityNames()) {
				this.infectionLevel.put(city, new HashMap<>());
				
				for (Disease d : Disease.values()) {
					
					this.infectionLevel.get(city).put(d, g.infectionLevel(city, d));
					nbDiseaseBlocksLeft.put(d, nbDiseaseBlocksLeft.get(d) - g.infectionLevel(city, d));
					
					// For nbCloseOutbreaks counter initialization
					if (this.infectionLevel.get(city).get(d) >= 3)
							nbCloseOutbreaks++;
				}

			}
			
			nbCloseOutbreaksAfterAction = nbCloseOutbreaks;

			for (Disease d : Disease.values()) {
				this.diseaseCured.put(d, g.isCured(d));
				this.diseasesEradicated.put(d, g.isEradicated(d));
			}

		}

		/**
		 * apply an action on the fake game
		 */
		private void applyAction(List<String> action) {
			
			if (action.size() == 3) {
				
				if (action.get(0).equals("treat")) {
					
					Map<Disease, Integer> treatedDisease = infectionLevel.get(action.get(1));
					if(diseaseCured.get( Disease.valueOf(action.get(2) )) ) {
						while (infectionLevel.get(fakeLocation).get(Disease.valueOf(action.get(2))) > 0) {
							treatedDisease.put(Disease.valueOf(action.get(2)), treatedDisease.get(Disease.valueOf(action.get(2))) - 1);
							nbDiseaseBlocksLeft.put(Disease.valueOf(action.get(2)), nbDiseaseBlocksLeft.get(Disease.valueOf(action.get(2))) + 1 );
							infectionLevel.get(fakeLocation).put( Disease.valueOf(action.get(2)), infectionLevel.get(fakeLocation).get(Disease.valueOf(action.get(2))) - 1 );
						}
					} else {
						infectionLevel.get(fakeLocation).put( Disease.valueOf(action.get(2)), infectionLevel.get(fakeLocation).get(Disease.valueOf(action.get(2))) - 1 );
						nbDiseaseBlocksLeft.put(Disease.valueOf(action.get(2)), nbDiseaseBlocksLeft.get(Disease.valueOf(action.get(2))) + 1 );
					}
					
					// For nbCloseOutbreaks counter update :
					nbCloseOutbreaksAfterAction = 0;
					for (String city : this.infectionLevel.keySet() ) {
						for (Disease d : Disease.values()) {
							if (this.infectionLevel.get(city).get(d) >= 3) {
								nbCloseOutbreaksAfterAction++;
							}
						}
					
					}
				}
			}

			if (action.size() == 2) {
				if (action.get(0).equals("fly")) {
					fakeLocation = action.get(1);
					nbCardUsed += 1;
				}

				if (action.get(0).equals("charter")) {
					fakeLocation = action.get(1);
					nbCardUsed += 1;
				}

				if (action.get(0).equals("move")) {
					fakeLocation = action.get(1);

				}
			}

		}

		/**
		 * return a score aboout the situation on the fakeGame
		 * 
		 * @return
		 */
		private int eval() {
			int score = 0;

			
			
			// point given by state of diseases on the board (general)
			for (String city : infectionLevel.keySet()) {
				for (Disease d : Disease.values()) {

					if (infectionLevel.get(city).get(d) == 1) {
						score += 1;
						score += game.neighbours(city).size();

					}
					if (infectionLevel.get(city).get(d) == 2) {
						score += 4;
						score += game.neighbours(city).size();

					}
					if (infectionLevel.get(city).get(d) == 3) {
						score += 9;
						score += game.neighbours(city).size() * 2;

					}
				}
			}
			
			// points given by diseases on player location and neighbors (ability to be cured easily later on)
			for (Disease d : Disease.values()) {
				
				if (infectionLevel.get(fakeLocation).get(d) > 0) {
					score = score - (infectionLevel.get(fakeLocation).get(d) * 2);
					// Bonus if location is close to outbreak
					if (infectionLevel.get(fakeLocation).get(d) >=3)
						score = score - 500; //-100
				}
					
				for (String city : game.neighbours(fakeLocation)) {
					score = score - infectionLevel.get(city).get(d);
					// Bonus if location is close to outbreak
					if (infectionLevel.get(city).get(d) >=3 )
						score = score - 250; //-10
				}

			}
			
			// points for outbreak probability that has been reduced or not by the action taken
			if (nbCloseOutbreaks - nbCloseOutbreaksAfterAction>0) {
			}
			score = score - (nbCloseOutbreaks - nbCloseOutbreaksAfterAction)*1000;

			
			// points given by disease blocks already used on the board (only if less than 24 cubes for a disease)
			for (Disease d : Disease.values()) {
				if (nbDiseaseBlocksLeft.get(d) < 24)
					score = score - nbDiseaseBlocksLeft.get(d)*10;
			}

			return score  ;
		}

	}
}
