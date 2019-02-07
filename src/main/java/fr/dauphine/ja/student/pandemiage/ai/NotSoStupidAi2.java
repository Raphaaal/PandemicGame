package fr.dauphine.ja.student.pandemiage.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import fr.dauphine.ja.pandemiage.common.AiInterface;
import fr.dauphine.ja.pandemiage.common.Disease;
import fr.dauphine.ja.pandemiage.common.GameInterface;
import fr.dauphine.ja.pandemiage.common.PlayerCardInterface;
import fr.dauphine.ja.pandemiage.common.PlayerInterface;
import fr.dauphine.ja.pandemiage.common.UnauthorizedActionException;

public class NotSoStupidAi2 implements AiInterface {

	private int nbActions;
	private final List<PlayerCardInterface> used;
	private final Map<Disease, Integer> usedDisease;

	/**
	 * Default constructor
	 */

	public NotSoStupidAi2() {
		this.nbActions = 0;
		this.used = new ArrayList<>();
		this.usedDisease = new HashMap<>();
		for (Disease d : Disease.values()) {
			usedDisease.put(d, 0);
		}
	}

	/**
	 * apply treat action if the infectionRate on currentLocation is superior or
	 * equal to danger value
	 * 
	 * @param g
	 * @param p
	 * @param danger
	 * @return
	 * @throws UnauthorizedActionException
	 */
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

	/**
	 * search if theire a neightbor with an infectionrate superior or equal to
	 * danger value
	 * 
	 * @param g
	 * @param p
	 * @param danger
	 * @return
	 * @throws UnauthorizedActionException
	 */
	public boolean moveToDanger(GameInterface g, PlayerInterface p, int danger) throws UnauthorizedActionException {
		for (String city : g.neighbours(p.playerLocation())) {
			for (Disease d : Disease.values()) {

				if (g.infectionLevel(city, d) > danger && g.isCured(d)) {
					p.moveTo(city);
					this.nbActions--;
					return true;
				}

				if (g.infectionLevel(city, d) > danger) {
					p.moveTo(city);
					this.nbActions--;
					return true;
				}

			}
		}
		return false;

	}

	/**
	 * create a vaccine if the player have 5 cards with same disease
	 * 
	 * @param p
	 * @param g
	 * @return
	 * @throws UnauthorizedActionException
	 */
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
	 * 1. Discover the cure of an uncured disease 2. Treat as many diseases as
	 * possible in its location 3. Move to a random neighbour
	 * 
	 * Print Exception stack in last resort
	 * 
	 * @param g
	 *            the gameEmgine for the AI to play on
	 * @param p
	 *            the player for the AI to play
	 */
	@Override
	public void playTurn(GameInterface g, PlayerInterface p) {

		this.nbActions = 4;
		try {
			while (nbActions > 0) {

				if (tryCure(p, g))
					this.nbActions--;

				else if (treatDanger(g, p, 0))
					this.nbActions--;
				else if (ManageCubeDanger(g, 21, p))
					this.nbActions--;

				else if (moveToDanger(g, p, 2))
					this.nbActions--;

				else if (goToDanger(g, p, 2))
					this.nbActions--;
				else if (moveToDanger(g, p, 0))
					this.nbActions--;

				else {
					// Attempt to move to random neighbor
					Random r = new Random();
					int nextMove = r.nextInt(g.neighbours(p.playerLocation()).size());
					p.moveTo(g.neighbours(p.playerLocation()).get(nextMove));
					nbActions = nbActions - 1;
				}
			}

			// Redo attempts from Treating a disease
		} catch (

		UnauthorizedActionException e) {
			p.skipTurn();
			this.nbActions = 0;
		}
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

	/**
	 * return a cured disease or the disease which is in minority in player hand
	 * 
	 * @param cards
	 * @param g
	 * @param discarded
	 * @return
	 */
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

	/**
	 * for a disease if the number of cube on the board is superior or equal to
	 * dangerCube value focus this disease
	 * 
	 * @param g
	 * @param dangerCube
	 * @param p
	 * @return
	 */
	public boolean ManageCubeDanger(GameInterface g, int dangerCube, PlayerInterface p) {
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

	/**
	 * the AI try every action available to treat the disease d
	 * 
	 * @param g
	 * @param d
	 * @param p
	 * @return
	 */
	private boolean FocusDisease(GameInterface g, Disease d, PlayerInterface p) {
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
	 * the AI try every action to go the city with cityName label
	 * 
	 * @param cityName
	 * @param g
	 * @param p
	 * @return false if the AI can't go to cityName
	 */
	public boolean focusCity(String cityName, GameInterface g, PlayerInterface p) {
		PlayerCardInterface pc = null;

		try {
			p.moveTo(cityName);
			return true;
		} catch (UnauthorizedActionException e) {

		}
		try {

			p.flyTo(cityName);
			return true;

		} catch (UnauthorizedActionException e) {

		}
		try {
			p.flyToCharter(cityName);
			return true;
		} catch (UnauthorizedActionException e) {

		}
		return false;
	}

	/**
	 * the ai search a city with an infectionRate superior or equal to danger
	 * 
	 * @param g
	 * @param p
	 * @param danger
	 * @return false if
	 */
	public boolean goToDanger(GameInterface g, PlayerInterface p, int danger) {
		List<String> citiesDanger = searchDanger(g, danger);
		String target = "";
		while (!citiesDanger.isEmpty()) {
			target = theMostDangerous(citiesDanger, g);
			try {
				p.flyTo(target);

				return true;
			} catch (UnauthorizedActionException e) {

			}

			try {
				p.flyToCharter(target);

			} catch (UnauthorizedActionException e) {
				return true;
			}
			citiesDanger.remove(target);
		}

		return false;
	}

	/**
	 * return the city where the risk is more important
	 * 
	 * @param cities
	 * @param g
	 * @return
	 */
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

	/**
	 * search cities with an infectionRate superior to danger
	 * 
	 * @param g
	 * @param danger
	 * @return liste of city
	 */
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

}
