package fr.dauphine.ja.pandemiage.common;

import java.util.List;

/**
 * Interface to the engine given to the AI for getting information to the status of the game
 *
 */

public interface GameInterface {
	
	/**
	 * 
	 * @return A list of all cities names in the game
	 */
	public List<String> allCityNames(); 
	
	/**
	 * Get the information concerning the neighbours of a given city
	 * @param cityName A name of a city, in String
	 * @return The list of all neighbours of this city
	 */
	public List<String> neighbours(String cityName);
	
	/**
	 * Get the number of infections on a given city for a given disease
	 * @param cityName The name of the city 
	 * @param d the type of disease
	 * @return the number of blocks for this disease on this city
	 */
	public int infectionLevel(String cityName, Disease d);
	
	/**
	 * Once played, infection cards are discarded but player can check what was discarded. 
	 * This method allows to get the list of discarded cards that will be back in the deck during an epidemic.
	 * @return the list of played infection cards (city names)
	 */
	public List<String> getDiscardedInfectionCards();
	
	
	/**
	 * Is this disease cured?
	 * @param d the disease
	 * @return true if it is cured, false otherwise
	 */
	public boolean isCured(Disease d);

	/**
	 * Is this disease eradicated? (Cured + no blocks left.)
	 * @param d the disease
	 * @return true if it is eradicated false otherwise
	 */
	public boolean isEradicated(Disease d);
	
	/**
	 * 
	 * @return the current infection rate of this game (2, 3 or 4), depending of the number of epidemies already occured.
	 */
	public int infectionRate();
	
	/**
	 * 
	 * @return The maximum turn duration in seconds before the engine sends a interrupt flag
	 */
	public int turnDuration(); 
	
	/**
	 * 
	 * @return the status of the current game
	 */
	public GameStatus gameStatus();
	
	/**
	 * 
	 * @return the number of OutBreaks in the current game
	 */
	public int getNbOutbreaks();

	/**
	 * 
	 * @return the number of PlayerCards left in the deck
	 */
	public int getNbPlayerCardsLeft();
	
	
	

}
