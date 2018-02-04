
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Snake {
	
	private List<Point> coords;//
	private Direction direction;//
	private List<Direction> moveQ;//
	private int prevTailX, prevTailY;//
	private int id;//
	private Color color;
	private boolean alive;//
	
	public Snake(int id, int x, int y, Direction dir, Color clr) {
		// randomize placement, but have the snake always start 'pointing' to the middle ?
		this.id = id;
		moveQ = new ArrayList<Direction>();
		coords = new ArrayList<Point>();
		coords.add(new Point(x,y));
		prevTailX = x;
		prevTailY = y;
		direction = dir;
		if (dir == Direction.Right) {
			coords.add(new Point(x-1,y));
			prevTailX = x-2;
		} else if (dir == Direction.Left) {
			coords.add(new Point(x+1,y));
			prevTailX = x+2;
		} else if (dir == Direction.Up) {
			coords.add(new Point(x,y+1));
			prevTailY = y+2;
		} else {
			coords.add(new Point(x,y-1));
			prevTailY = y-2;
		}
		color = clr;
		alive = true;
	}
	
	public Snake() {
		moveQ = new ArrayList<Direction>();
		coords = new ArrayList<Point>();
	}
	
	public Snake clone() {
		// deep copy so that this new snake won't share reference with original snake
		Snake newSnake = new Snake();
		if (!alive) {
			newSnake.setAliveStatus(false);
			newSnake.setID(id);
			return newSnake;
		}
		newSnake.setAliveStatus(true);
		newSnake.setID(id);
		newSnake.setCoords(coords);
		newSnake.setPrevTail(prevTailX, prevTailY);
		newSnake.setDirection(direction);
		newSnake.setMoveQ(moveQ);
		newSnake.setColor(color);
		return newSnake;
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color clr) {
		color = clr;
	}
	
	public int getID() {
		return id;
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public boolean isAlive() {
		return alive;
	}
	
	public void setAliveStatus(boolean b) {
		alive = b;
	}
	
	public int getHeadX() {
		return coords.get(0).x;
	}
	
	public int getHeadY() {
		return coords.get(0).y;
	}
	
	public int getTailX() {
		return coords.get(coords.size()-1).x;
	}
	
	public int getTailY() {
		return coords.get(coords.size()-1).y;
	}
	
	public void setCoords(List<Point> newCoords) {
		// deep clone so doesn't share reference with another snake's points
		coords.clear();
		for (int i = 0; i < newCoords.size(); i++) {
			coords.add(i, new Point(newCoords.get(i).x, newCoords.get(i).y));
		}
	}
	
	public List<Point> getCoords() {
		return coords;
	}
	
	public int getPrevTailX() {
		return prevTailX;
	}
	
	public int getPrevTailY() {
		return prevTailY;
	}
	
	public Direction getDirection() {
		return direction;
	}
	
	public void setDirection(Direction dir) {
		if (dir == null) {
			return;
		}
		direction = dir;
	}
	
	public void moveDirection(Direction dir) {
		// the moveQ is useful for keyboard controlled snakes to be able to register more than 1 keystroke when 
		// multiple keys are hit in a short span of time (ex. going up and you want to go right then instantly down so you press right then down quickly)
		// also finding a route once then executing that route is much more efficient than finding a route before each move.
		// however this efficiency isn't at all reliable when there is more than 1 snake on the board.
		if (moveQ.size() == 0) {
			if (!direction.equals(dir))
				// wondering if I should also check to make sure dir != opposite(direction)
				// (ex. snake travelling up, but it attempts to switch to down which is guaranteed invalid)
				moveQ.add(dir);
			return;
		}
		if (!moveQ.get(moveQ.size()-1).equals(dir) && !moveQ.get(moveQ.size()-1).equals(opposite(dir))) {
			moveQ.add(dir);
		}
	}
	
	public List<Direction> getMoveQ() {
		return moveQ;
	}
	
	public void setMoveQ(List<Direction> directions) {
		// this usage of the moveQ is useful for when only 1 snake is on the board since the AI can account for the future of the board
		if (directions != null) {
			for (int i = 0, n = directions.size(); i < n; i++) {
				moveQ.add(directions.get(i));
			}
		}
		else {
			moveQ.clear();
		}
	}
	
	public void setPrevTail(int x, int y) {
		prevTailX = x;
		prevTailY = y;
	}
	
	private Direction opposite(Direction dir) {
		if (dir.equals(Direction.Right))
			return Direction.Left;
		else if (dir.equals(Direction.Left))
			return Direction.Right;
		else if (dir.equals(Direction.Up))
			return Direction.Down;
		else return Direction.Up;
	}
	
	public int getLength() {
		return coords.size();
	}
	
	public void eat() {
		coords.add(new Point(prevTailX, prevTailY));
	}
	
	public void moveForward() {
		// instead of moving each body cell forwards, could just remove the tail while adding onto the front.@
		int headX = getHeadX();
		int headY = getHeadY();
		prevTailX = coords.get(coords.size()-1).x;
		prevTailY = coords.get(coords.size()-1).y;
		for (int i = coords.size()-1; i >= 1; i--) {
			coords.set(i, coords.get(i-1));
		}
		if (moveQ.size() > 0) {
			direction = moveQ.get(0);
			moveQ.remove(0);
		}
		switch (direction) {
			case Up:
				headY--;
				coords.set(0, new Point(headX,headY));
				break;
			case Down:
				headY++;
				coords.set(0, new Point(headX,headY));
				break;
			case Left:
				headX--;
				coords.set(0, new Point(headX,headY));
				break;
			case Right:
				headX++;
				coords.set(0, new Point(headX,headY));
				break;	
		}
	}
}

enum Direction {
	Up, Down, Left, Right
}
