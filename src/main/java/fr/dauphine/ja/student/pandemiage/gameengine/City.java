package fr.dauphine.ja.student.pandemiage.gameengine;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import fr.dauphine.ja.pandemiage.common.Disease;

/**
 * 
 * This object represents the differents city in the game
 *
 */
public class City {
	private final int id;
	private String label;
	private double eigencentrality;
	private int degree;
	private double size;
	private int r;
	private int g;
	private int b;
	private double x;
	private double y;
	private final Map<String, City> neighbours;
	private final Map<Disease, Integer> diseases;

	/**
	 * A constructor for a city.
	 * 
	 * @param id must be >= 0
	 */
	public City(int id) {
		if (id < 0) {
			throw new IllegalArgumentException();
		}
		this.id = id;
		this.neighbours = new HashMap<>();
		this.diseases = new HashMap<>();
		this.diseases.put(Disease.RED, 0);
		this.diseases.put(Disease.BLUE, 0);
		this.diseases.put(Disease.YELLOW, 0);
		this.diseases.put(Disease.BLACK, 0);
	}

	/**
	 * Decreases the disease count
	 * 
	 * @param disease
	 */
	public void removeDisease(Disease disease) {
		if (diseases.get(disease) <= 0)
			throw new IllegalArgumentException();
		this.diseases.put(disease, diseases.get(disease) - 1);
	}

	/**
	 * Decreases the disease count
	 * 
	 * @param disease
	 */
	public void addDisease(Disease disease) {
		
		this.diseases.put(disease, diseases.get(disease) + 1);
		//if (this.diseases.get(Disease.BLACK) > 3)
		//	this.diseases.put(Disease.BLACK, this.diseases.get(Disease.BLACK) + 1);

	}

	/**
	 * Adds neighbor to a city.
	 * 
	 * @param City  neighbour not null
	 */
	public void addNeighbour(City neighbour) {
		this.neighbours.put(neighbour.getLabel(), Objects.requireNonNull(neighbour));
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return the diseases
	 */
	public Map<Disease, Integer> getDiseases() {
		return diseases;
	}

	/**
	 * @param String label not null
	 */
	public void setLabel(String label) {
		this.label = Objects.requireNonNull(label);
	}

	/**
	 * @return d eigencentrality
	 */
	public double getEigencentrality() {
		return eigencentrality;
	}

	/**
	 * @param eigencentrality the eigencentrality to set
	 */
	public void setEigencentrality(double eigencentrality) {
		this.eigencentrality = eigencentrality;
	}

	/**
	 * @return the degree
	 */
	public int getDegree() {
		return degree;
	}

	/**
	 * @param degree can't be negative
	 */
	public void setDegree(int degree) {
		if (degree < 0)
			throw new IllegalArgumentException("degree can't be negative");
		this.degree = degree;
	}

	/**
	 * @return the size
	 */
	public double getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(double size) {
		if (size < 0) {
			throw new IllegalArgumentException();
		}
		this.size = size;
	}

	/**
	 * @return the r
	 */
	public int getR() {

		return r;
	}

	/**
	 * @param int  r can't be negative
	 * 
	 */
	public void setR(int r) {
		if (r < 0)
			throw new IllegalArgumentException();
		this.r = r;
	}

	/**
	 * @return the g
	 */
	public int getG() {

		return g;
	}

	/**
	 * @param g can't be negative
	 * 
	 */
	public void setG(int g) {
		if (g < 0)
			throw new IllegalArgumentException();
		this.g = g;
	}

	/**
	 * @return the b
	 */
	public int getB() {
		return b;
	}

	/**
	 * @param b can't be negative
	 * 
	 */
	public void setB(int b) {
		if (b < 0)
			throw new IllegalArgumentException();
		this.b = b;
	}

	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @param double  x the x to set
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @return double y
	 */
	public double getY() {
		return y;
	}

	/**
	 * Returns the view of the Neighbours list
	 * 
	 * @return List<City>
	 */
	public Map<String, City> getNeighbours() {
		return Collections.unmodifiableMap(neighbours);
	}

	/**
	 * @param double  y
	 * 
	 */
	public void setY(double y) {
		if (y < 0)
			throw new IllegalArgumentException();
		this.y = y;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Return every field of a city in a String
	 */

	@Override
	public String toString() {
		return "[CityName=" + label + "x=" + x + ", y=" + y +"diseases:"+diseases.entrySet()+"neihbours: "+neighbours.keySet()+"]";
	}

	@Override
	public boolean equals(Object obj) {
		if (Objects.isNull(obj))
			return false;
		if (obj == this) {
			return true;
		}

		if (obj instanceof City) {
			return this.label.equals(((City) obj).label) && this.x == ((City) obj).x && this.y == ((City) obj).y
					&& this.degree == ((City) obj).degree && this.size == ((City) obj).size && this.r == ((City) obj).r
					&& this.g == ((City) obj).g && this.b == ((City) obj).b;

		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.label, this.x, this.y, this.eigencentrality, this.size, this.degree,
				this.neighbours);

	}
}
