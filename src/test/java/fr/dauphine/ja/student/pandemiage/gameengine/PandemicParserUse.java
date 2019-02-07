package fr.dauphine.ja.student.pandemiage.gameengine;

import java.io.FileNotFoundException;
import java.util.HashMap;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.cli.ParseException;


public class PandemicParserUse {
	public static void main(String[] args) throws FileNotFoundException, XMLStreamException, ParseException {
		PandemicParser parser=new PandemicParser("pandemic.graphml");
		HashMap <String,City> cities= parser.ParseCities();
		System.out.println(cities.get("Paris").getLabel());
		System.out.println(cities.get("Paris").getNeighbours().get(0).getLabel());
		System.out.println(cities.get("Paris"));
	}
}
