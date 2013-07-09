package graphtea.extensions.algorithms.structures;

import graphtea.graph.graph.GraphPoint;
import java.util.ArrayList;

/**
 * <p>
 * <b>Circle</b>
 * </p>
 * <p>
 * Circle est la classe implémentant le cercle
 * </p>
 * @author Depoyant Guillaume & Ludmann Michaël
 *
 */
@SuppressWarnings("serial")
public class Intersection {
	
	private double radius;
	protected double RADIUS = 10F;
	private double x, y;
	private ArrayList<Segment> segments;
	/**
	 * <p>
	 * Constructeur de la classe
	 * </p>
	 * <p>
	 * Instancie un cercle
	 * </p>
	 */
	public Intersection(double x, double y, float radius) {
		super();
		this.x = x;
		this.y = y;
//		setFrame(x, y, radius);
		segments = new ArrayList<Segment>();
	}
	
	public Intersection(double x, double y) {
		super();
		this.x = x;
		this.y = y;
//		setFrame(x, y, RADIUS);
		segments = new ArrayList<Segment>();
	}
	
	/**
	 * Getter du rayon
	 * @return le rayon
	 */
	public double getRadius() {
		return radius;
	}
	
	public String toString()
	{
		return "Intersection : {"+this.x+", "+this.y+"}";
	}
	
	public void printIntersection()
	{
		System.out.println("Intersection : "+this.x+" ; "+this.y);
	}	
	/**
	 * Set the list of segments that are part of the intersection
	 * @param list
	 */
	public void setSegments(ArrayList<Segment> list)
	{
		this.segments = list;
	}
	
	public ArrayList<Segment> getSegments()
	{
		return this.segments;
	}
	
	public GraphPoint getPoint(){
		return new GraphPoint((double) this.x, (double) this.y);
	}
}
