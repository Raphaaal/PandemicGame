package fr.dauphine.ja.student.pandemiage.gameengine;

import java.util.Objects;
import fr.dauphine.ja.pandemiage.common.Disease;
import fr.dauphine.ja.pandemiage.common.PlayerCardInterface;

public class PlayerCard implements PlayerCardInterface {
	
	private final String cityName;
	private final Disease disease;

	/**
	 * Constructor for PlayerCard
	 * 
	 * @param cityName not null
	 * @param disease
	 */
	public PlayerCard(String cityName, Disease disease) {
		this.cityName = Objects.requireNonNull(cityName);
		this.disease = disease;
	}

	@Override
	public String getCityName() {
		return this.cityName;
	}

	@Override
	public Disease getDisease() {
		return this.disease;
	}

	@Override
	public String toString() {
		return getCityName() + " / " + getDisease();
	}
}
