package fr.dauphine.ja.student.pandemiage.gameengine;

import java.util.Objects;

import fr.dauphine.ja.pandemiage.common.*;

public class InfectorCard implements PlayerCardInterface {
	
	private String cityName;
	private Disease disease;
	
	/**
	 * A constructor for InfectorCard
	 * @param cityName not null
	 * @param disease
	 */
	InfectorCard(String cityName, Disease disease) {
		Objects.requireNonNull(cityName);
		Objects.requireNonNull(disease);
		this.cityName = cityName;
		this.disease = disease;
	}

	@Override
	public String getCityName() {
		return cityName;
	}

	@Override
	public Disease getDisease() {
		return disease;
	}
	

}
