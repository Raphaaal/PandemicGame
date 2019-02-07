package fr.dauphine.ja.student.pandemiage.gameengine;

import java.io.FileNotFoundException;

import java.io.FileReader;
import java.util.HashMap;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.cli.ParseException;

/**
 * This class provides a parser to parse data from the pandemic.graphml file.
 * This file provide information for each city in the game (name, location, etc.)
 *
 */

public class PandemicParser {
	private final String cityGraphFilename;

	public PandemicParser(String cityGraphFilename) {
		this.cityGraphFilename = cityGraphFilename;

	}

	/**
	 * The function ParseCities parse the file pandemic.graphml provided with this
	 * project and return a HashMap<String,City> which contains cities. In the map,
	 * the String key is the name of the city
	 * 
	 * @return A Hashmap<String,City> <code> not null</code>
	 * 
	 * @throws XMLStreamException
	 * @throws FileNotFoundException
	 * @throws ParseException
	 */
	public HashMap<String, City> ParseCities() throws XMLStreamException, FileNotFoundException, ParseException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		HashMap<String, City> citiesByLabel = new HashMap<>();
		XMLStreamReader xmler = factory.createXMLStreamReader(new FileReader(cityGraphFilename));
		HashMap<String, City> citiesById = new HashMap<>();
		try {
			while (xmler.hasNext()) {
				xmler.next();
				if (xmler.isStartElement() && xmler.getLocalName().equals("node")) {

					int id = Integer.parseInt(xmler.getAttributeValue(0));
					City city = new City(id);
					citiesById.put(xmler.getAttributeValue(0), city);
					xmler.next();
					while (xmler.next() == XMLStreamConstants.START_ELEMENT && xmler.getLocalName().equals("data")) {
						String data = xmler.getAttributeValue(0);
						xmler.next();
						switch (data) {

						case "label":
							citiesByLabel.put(xmler.getText(), city);
							city.setLabel(xmler.getText());
							break;

						case "eigencentrality":
							city.setEigencentrality(Double.parseDouble(xmler.getText()));

							break;

						case "degree":
							city.setDegree(Integer.parseInt(xmler.getText()));

							break;

						case "size":

							city.setSize(Double.parseDouble(xmler.getText()));
							break;

						case "r":
							city.setR(Integer.parseInt(xmler.getText()));

							break;

						case "g":
							city.setG(Integer.parseInt(xmler.getText()));
							break;
						case "b":
							city.setB(Integer.parseInt(xmler.getText()));

							break;
						case "x":
							city.setX(Double.parseDouble(xmler.getText()));
							break;

						case "y":
							city.setY(Double.parseDouble(xmler.getText()));
							break;

						default:
							break;

						}
						xmler.next();

						if (!(xmler.isEndElement() && xmler.getLocalName().equals("data"))) {
							throw new ParseException("</data> expected");
						}
						xmler.next();

					}

					if (!(xmler.isEndElement() && xmler.getLocalName().equals("node"))) {
						throw new ParseException("</node> expected ");
					}

				}
				if (xmler.isStartElement() && xmler.getLocalName().equals("edge")) {
					citiesById.get(xmler.getAttributeValue(0)).addNeighbour(citiesById.get(xmler.getAttributeValue(1)));
					citiesById.get(xmler.getAttributeValue(1)).addNeighbour(citiesById.get(xmler.getAttributeValue(0)));

				}

			}

		} finally

		{
			xmler.close();
		}
		return citiesByLabel;
	}

}
