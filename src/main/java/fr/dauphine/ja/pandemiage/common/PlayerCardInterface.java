package fr.dauphine.ja.pandemiage.common;

/**
 * Interface for the Player Cards.
 *
 */

public interface PlayerCardInterface {
	
	/**
	 * 
	 * @return Name of the city, in String of this card
	 */
	public String getCityName(); 
	
	/**
	 * 
	 * @return Type of disease associated to this card
	 */
	public Disease getDisease(); 	

}
