
public class Node {

	private Node previous;
	private int x;
	private int y;
	private int depth;
	private double estimated_distance; // used in A* pathfinding
	
	public Node(int x, int y, Node prev) {
		this.x = x;
		this.y = y;
		previous = prev;
	}
	
	public Node(int x, int y, Node prev, int depth) {
		this(x,y,prev);
		this.depth = depth;
	}
	
	public Node(int x, int y, Node prev, int depth, double est_distance) {
		this(x,y,prev,depth);
		this.estimated_distance = est_distance;
	}
	
	public int getDepth() { return depth; }
	public int getX() { return x; }
	public int getY() { return y; }
	public Node getPrev() { return previous; }
	public double getDistance() { return estimated_distance; }
	
}
