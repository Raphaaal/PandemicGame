package fr.dauphine.ja.pandemiage.common;

import java.util.*;

/**
 * Interface to player move given by the engine to the AI s.t. the AI can do actions
 *
 */

public interface PlayerInterface {
	

	/**
	 * Allows to move the current player from the current city to a connected city. Cost 1 action.
	 * @param cityName the target city
	 * @throws UnauthorizedActionException if cityName is not a neighbor of the current city or if the city doesn't exist, or no more actions left
	 */
	public void moveTo(String cityName) throws UnauthorizedActionException;
	
	/**
	 * Allows to (directly) fly the current player from the current city to a city, with the cost of a PlayerCard of cityName. Cost 1 action.
	 * @param cityName the target city
	 * @throws UnauthorizedActionException if the player doesn't have the corresponding PlayerCard of CityName (or if the city doesn't exist), or no more actions left
	 */
	public void flyTo(String cityName) throws UnauthorizedActionException;
	

	/**
	 * Allows to (charter) fly the current player from the current city to a city, with the cost of a PlayerCard of the current city. Cost 1 action.
	 * @param cityName the target city
	 * @throws UnauthorizedActionException if the player doesn't have the corresponding PlayerCard of the current city (or if the city doesn't exist), or no more actions left
	 */
	public void flyToCharter(String cityName) throws UnauthorizedActionException;
	
	/**
	 * Allows the player to skip his turn
	 */
	public void skipTurn();
	
		
	/**
	 * Allows the current player to treat the 1 block of the disease d on the current city. Cost 1 action.
	 * If a cure was discovered for this disease, all the blocks of this disease are removed on the current city  
	 * @param d the disease to treat
	 * @throws UnauthorizedActionException if there is no more action left
	 */
	public void treatDisease(Disease d) throws UnauthorizedActionException; 
	
	/**
	 * Allows the current player to discover a cure for the disease in the cards given in the list of cards. Cost 1 action.
	 * All cards must be of the same color (i.e. concerning the same disease).
	 * @param cardNames List of cards of the player hand to find a cure.
	 * @throws UnauthorizedActionException if the list is not of the correct size, or correct colors, of if it cards are not valid, or if more actions left
	 */
	public void discoverCure(List<PlayerCardInterface> cardNames) throws UnauthorizedActionException;		
	
	/**
	 * 
	 * @return	the current city where the current player is. Doesn't cost an action.
	 */
	public String playerLocation();


	/**
	 * 
	 * @return the list of PlayerCard the current player got in his hand. Doesn't cost an action.
	 */
	public List<PlayerCardInterface> playerHand(); 


}
