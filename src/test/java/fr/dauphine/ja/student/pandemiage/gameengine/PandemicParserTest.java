package fr.dauphine.ja.student.pandemiage.gameengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.FileNotFoundException;
import java.util.HashMap;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

public class PandemicParserTest {

	/**
	 * Tests if the first city and the last city in neightbours are the good one
	 * 
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 * @throws ParseException
	 */
	@Test
	public void testPandemicParser() throws FileNotFoundException, XMLStreamException, ParseException {
		PandemicParser parser = new PandemicParser("pandemic.graphml");
		HashMap<String, City> cities = parser.ParseCities();
		City FirstCity = new City(1);
		FirstCity.setLabel("San Francisco");
		FirstCity.setEigencentrality(0.4266021110134155);
		FirstCity.setDegree(4);
		FirstCity.setSize(10.0);
		FirstCity.setR(107);
		FirstCity.setG(112);
		FirstCity.setB(184);
		FirstCity.setX(-959.0986);
		FirstCity.setY(974.0585);

		City LastCity = new City(51);

		LastCity.setLabel("Miami");
		LastCity.setEigencentrality(0.2774486898899163);
		LastCity.setDegree(4);
		LastCity.setSize(10.0);
		LastCity.setR(242);
		LastCity.setG(255);
		LastCity.setB(0);
		LastCity.setX(-706.2949);
		LastCity.setY(650.15753);
		assertTrue(cities.get("San Francisco").equals(FirstCity));
		assertTrue(cities.get("Miami").equals(LastCity));
	}

	@Test
	public void citiesSize() throws FileNotFoundException, XMLStreamException, ParseException {
		PandemicParser parser = new PandemicParser("pandemic.graphml");
		HashMap<String, City> cities = parser.ParseCities();
		assertEquals(48, cities.size());
	}

	/**
	 * Tests if the size of neighbours is the one expected
	 * 
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 * @throws ParseException
	 */

	@Test
	public void testNeighboursSize() throws FileNotFoundException, XMLStreamException, ParseException {
		PandemicParser parser = new PandemicParser("pandemic.graphml");
		HashMap<String, City> cities = parser.ParseCities();
		assertEquals(cities.get("Paris").getNeighbours().size(), cities.get("Paris").getDegree());
	}
}