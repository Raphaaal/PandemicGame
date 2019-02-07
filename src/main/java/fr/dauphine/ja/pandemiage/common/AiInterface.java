 package fr.dauphine.ja.pandemiage.common;

import java.util.List; 

/**
 * Interface that every AI must implements in order to be called by a game.
 * Engine will invoke an AI via reflection and then call the AI via these methods.
 *
 */
public interface AiInterface {

	/**
	 * Engines call this method. AI should play the 4 actions in this method within the time fixed in TurnDuration.
	 * Engines put the interrupt flag to true after this time. AI should check this flag.
	 * If AI didn't finished this method 1 second after the first timeout, the game is lost.
	 * @param g	Interface with the engine to get information
	 * @param p	Interface with the player to make actions
	 */
	public void playTurn(GameInterface g, PlayerInterface p);
	
	/**
	 * Once the player finished its actions and received Player Cards, he must discards to fit to the maximum hand size 
	 * @param g	Interface with the game
	 * @param p Interface with the actions
	 * @param maxHandSize maximum size of the player hand
	 * @param nbEpidemicCards number of epidemic cards that were picked by the player during this turn.
	 * @return The list of discarded cards
	 */
	public List<PlayerCardInterface> discard(GameInterface g, PlayerInterface p, int maxHandSize, int nbEpidemicCards);
}
