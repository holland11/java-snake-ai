import java.awt.Point;


public class GameState {
	
	/*
		Game is represented by a grid of height * width cells,
		a variable number of snakes (array of coordinates making up the head/body of the snake + its ID + its status (alive/dead) )
		a variable number of food (x and y coordinate for each food).
		For each game tick, the game will bundle all of this information and send it to a class 
		which will then send that information off to each snake's 'brain'.
		This brain will take in the game state and the ID of the snake it needs to decide for then return
		a direction that it decides that snake should go.
		One brain can be my default routefinder, one brain can be for the actual manual player which is controlled by the keyboard,
		and another brain can be a shell class for other programmers to build their own AI out of.
		The bundled information should be a copy of the game's actual variables so that the 'brain' class can't just change 
		'server side' variables.
		The bundled gamestate could be a class where getSnakes() is the method which returns an object where 'getId() returns 
		the id, getDirection() returns the current direction and getCoordinates() returns the list of coordinates ordered from head to tail.
		Other methods in the gamestate class could include getHeight(), getWidth(), getFood() (a list of Point objects representing 
		the coordinates of each piece of food).
	*/
	
	private int width;
	private int height;
	private Snake[] snakes;
	private Point[] food;
	private int[] scores;
	private int snakes_alive;
	
	public GameState(int width, int height, Snake[] orig_snakes, Point[] orig_food) {
		snakes = new Snake[orig_snakes.length];
		food = new Point[orig_food.length];
		for (int i = 0, n = snakes.length; i < n; i++) {
			snakes[i] = orig_snakes[i].clone();
		}
		for (int i = 0, n = food.length; i < n; i++) {
			food[i] = new Point(orig_food[i].x, orig_food[i].y);
		}
		this.width = width;
		this.height = height;
	}
	
	public GameState(int width, int height, Snake[] orig_snakes, Point[] orig_food, int[] scores, int snakes_alive) {
		this(width, height, orig_snakes, orig_food);
		this.snakes_alive = snakes_alive;
		this.scores = new int[scores.length];
		for (int i = 0; i < scores.length; i++) {
			this.scores[i] = scores[i];
		}
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getSnakesAlive() {
		return snakes_alive;
	}
	
	public int[] getScores() {
		return scores;
	}
	
	public int getHeight() {
		return height;
	}
	
	public Snake[] getSnakes() {
		return snakes;
	}
	
	public Point[] getFood() {
		return food;
	}
}