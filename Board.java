
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;

import javax.swing.JPanel;


public class Board extends JPanel implements Runnable, KeyListener {
	
	/*
	Setup the ability to tell it to play multiple games in a row and keep score till the end where it displays. (good idea for comparing algorithms / ai's
	
	Setup the ability to 'rewind' a few moves while game is paused / game is finished to get a better look at what happened
	*/
	 
	// Game settings (these can be modified)
	// Several arrays were hardcoded to only account for a maximum of 6 snakes. If you want to include more, you'll have to add values to
	// the arrays or think of another way to do it without hardcoding values.
	private final int num_snakes = 6; // how many snakes there should be (max 6).
	private int[] snake_brains = { 1, 1, 1, 1, 1, 1 }; // 0 is human (keyboard) controlled, 1 is my default AI, 2 is the AI shell for you to program
	// this int array must be at least num_snakes in length and must only contain a 0, 1, or 2 
	// (unless you want another AI in which case you can add the code for it and represent it as a 3)
	private final int num_food = 2; // how many pieces of food there should be
	private final int boardW = 22; // how many cells wide the board will be 
	private final int boardH = 22; // how many cells tall the board will be 
	private final int blockSize = 19; // size of each cell in the GUI
	private long updateTime = 60; // how many milliseconds between each gametick (lower = faster, higher = slower)
								  // must give enough time for each snake's AI to make a decision
	private final Color[] colors = { Color.blue, Color.yellow, Color.magenta, Color.pink, Color.green, Color.orange };
	// colors for the (max of 6) snakes
	// next 3 arrays are for snake starting position and orientation
	// (positioned facing the middle in this order: bottom left, top right, top left, bottom right, top middle, bottom middle)
	private final int[] startX = { (int)(1.0*boardW/4.0), (int)(3.0*boardW/4.0), (int)(1.0*boardW/4.0), (int)(3.0*boardW/4.0), (int)(2.0*boardW/4.0), (int)(2.0*boardW/4.0) };
	private final int[] startY = { (int)(3.0*boardH/4.0), (int)(1.0*boardH/4.0), (int)(1.0*boardH/4.0), (int)(3.0*boardH/4.0), (int)(1.0*boardH/4.0), (int)(3.0*boardH/4.0) };
	private final Direction[] startDirection = { Direction.Right, Direction.Left, Direction.Right, Direction.Left, Direction.Down, Direction.Up };
	enum Tile { Empty, Food, Snake1, Snake1Head, Snake2, Snake2Head, Snake3, Snake3Head, Snake4, Snake4Head, Snake5, Snake5Head, Snake6, Snake6Head };
	Tile[] headTiles = { Tile.Snake1Head, Tile.Snake2Head, Tile.Snake3Head, Tile.Snake4Head, Tile.Snake5Head, Tile.Snake6Head };
	Tile[] bodyTiles = { Tile.Snake1, Tile.Snake2, Tile.Snake3, Tile.Snake4, Tile.Snake5, Tile.Snake6 };
	
	// don't modify these
	private Tile[][] board;
	private boolean gameOn, paused, playable;
	private Snake[] snakes;
	private Thread thread;
	private String statusbar;
	private long time1, time2;
	private Point[] food;
	private int scores[];
	private int snakesAlive;
	private LinkedList<GameState> previous_turns;
	private final int max_turns = 40;
	
	private Direction fetchMove(int ai, GameState gs, int index) {
		if (ai == 1) {
			Direction dir = SnakeAI1.getSingleMove(gs, index);
			return dir;
		} 
		else if (ai == 2) {
			return SnakeAI2.getSingleMove(gs, index);
		}
		return null;
	}

	public Board() {
		setPreferredSize(new Dimension(boardW * blockSize, boardH * blockSize + 17));
		setFocusable(true);
		addKeyListener(this);
		board = new Tile[boardH][boardW];
		previous_turns = new LinkedList<>();
		initBoard();
		playable = true;
		time1 = System.currentTimeMillis();
	}
	
	public void initBoard() {
		for (int i = 0; i < boardH; i++) {
			for (int j = 0; j < boardW; j++) {
				board[i][j] = Tile.Empty;
			}
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (gameOn) {
			updateBoard();
			drawBoard(g);
		}
		else {
			drawBoard(g);
		}
	}
	
	public void drawBoard(Graphics g) {
		g.setColor(Color.lightGray);
		g.fillRect(0, 0, getSize().width, getSize().height);
		for (int i = 0; i < boardH; i++) {
			for (int j = 0; j < boardW; j++) {
				Tile cell = board[i][j];
				if (cell == Tile.Empty) {
					g.setColor(Color.white);
				}
				else if (cell == Tile.Food) {
					g.setColor(Color.red);
				}
				else {
					int index = indexHeadCell(cell);
					if (index != -1) {
						// give head cells a black outline instead of grey
						g.setColor(Color.black);
						g.fillRect((j * blockSize), (i * blockSize), blockSize+2, blockSize+2);
						g.setColor(snakes[index].getColor());
					}
					else {
						index = indexBodyCell(cell);
						if (index == -1) {
							System.out.println("Error fetching color of body tiles");
							System.exit(9);
						}
						g.setColor(snakes[index].getColor());
					}
				}
				g.fillRect((j * blockSize) + 1, (i * blockSize) + 1, blockSize-1, blockSize-1);
			}
		}
		g.setColor(Color.black);
		g.drawString(statusbar, 5, getSize().height-3);
	}
	
	private int indexBodyCell(Tile cell) {
		for (int i = 0; i < num_snakes; i++) {
			if (cell == bodyTiles[i]) {
				return i;
			}
		}
		return -1;
	}
	
	private int indexHeadCell(Tile cell) {
		for (int i = 0; i < num_snakes; i++) {
			if (cell == headTiles[i]) {
				return i;
			}
		}
		return -1;
	}
	
	private void updateBoard() {
		initBoard();
		for (int i = 0; i < snakes.length; i++) {
			// put all alive snakes bodies onto the board
			// there should be no collisions here because every turn the heads are checked for collisions
			if (!snakes[i].isAlive()) {
				continue;
			}
			List<Point> coords = snakes[i].getCoords();
			// skip head so start j = 1
			for (int j = 1; j < coords.size(); j++) {
				int x = coords.get(j).x;
				int y = coords.get(j).y;
				board[y][x] = bodyTiles[i];
			}
		}
		for (int i = 0; i < snakes.length; i++) {
			// now add heads while checking for collisions
			if (!snakes[i].isAlive()) {
				continue;
			}
			int headX = snakes[i].getHeadX();
			int headY = snakes[i].getHeadY();
			if (headX >= boardW || headX < 0 || headY >= boardH || headY < 0) {
				snakesAlive--;
				snakes[i].setAliveStatus(false);
				continue;
			}
			Tile cell = board[headY][headX];
			if (cell == Tile.Empty) {
				board[headY][headX] = headTiles[i];
			}
			else {
				collision(headX,headY,i);
			}
		}
		for (int i = 0; i < num_food; i++) {
			// place food on board. food should either be on a head or on an empty tile
			// if it is on a head, call method for that snake to eat it + spawn a new apple
			int foodX = food[i].x;
			int foodY = food[i].y;
			if (board[foodY][foodX] == Tile.Empty) {
				board[foodY][foodX] = Tile.Food;
			}
			else {
				switch (board[foodY][foodX]) {
					case Snake1Head:
						snakes[0].eat();
						scores[0]++;
						break;
					case Snake2Head:
						snakes[1].eat();
						scores[1]++;
						break;
					case Snake3Head:
						snakes[2].eat();
						scores[2]++;
						break;
					case Snake4Head:
						snakes[3].eat();
						scores[3]++;
						break;
					case Snake5Head:
						snakes[4].eat();
						scores[4]++;
						break;
					case Snake6Head:
						snakes[5].eat();
						scores[5]++;
						break;
					default:
						System.out.println("Error: Food is on a tile which is not empty or a snake's head.");
						System.exit(8);
				}
				spawnFood(i);
			}
		}
		if (snakesAlive <= 0 || (num_snakes > 1 && snakesAlive < 2)) {
			gameOn = false;
		}
		setStatusBar();
	}
	
	public void collision(int x, int y, int index) {
		Tile cell = board[y][x];
		int bodyIndex = indexBodyCell(cell);
		if (bodyIndex == -1) {
			int headIndex = indexHeadCell(cell);
			if (snakes[headIndex].getLength() == snakes[index].getLength()) {
				snakes[headIndex].setAliveStatus(false);
				snakes[index].setAliveStatus(false);
				snakesAlive -= 2;
			}
			else if (snakes[headIndex].getLength() > snakes[index].getLength()) {
				snakes[index].setAliveStatus(false);
				snakesAlive--;
			}
			else {
				snakes[headIndex].setAliveStatus(false);
				snakesAlive--;
			}
		}
		else {
			snakes[index].setAliveStatus(false);
			snakesAlive--;
		}
		return;
	}
	
	public void updateSnakes() {
		GameState gs = new GameState(boardW, boardH, snakes, food, scores, snakesAlive);
		add_turn(gs);
		for (int i = 0; i < num_snakes; i++) {
			if (!snakes[i].isAlive()) {
				continue;
			}
			if (snake_brains[i] == 1 || snake_brains[i] == 2) {
				gs = new GameState(boardW, boardH, snakes, food);
				System.out.println("Getting move for snake "+i);
				Direction dir = fetchMove(snake_brains[i], gs, i);
				System.out.println(dir_to_string(dir));
				if (dir != null) {
					snakes[i].moveDirection(dir);
				}
			}
		}
		for (int i = 0; i < num_snakes; i++) {
			if (!snakes[i].isAlive()) {
				continue;
			}
			snakes[i].moveForward();
		}
	}
	
	public String dir_to_string(Direction dir) {
		if (dir == null) {
			return "Null";
		}
		switch (dir) {
			case Up:
				return "Up";
			case Down:
				return "Down";
			case Right:
				return "Right";
			case Left:
				return "Left";
		}
		return "Unexpected";
	}
	
	public void restart() {
		board = new Tile[boardH][boardW];
		initBoard();
		time1 = System.currentTimeMillis();
		start();
	}
	
	public void start() {
		if (num_snakes > 6) {
			// the max is 6 because I am hardcoding in 6 different colour and starting position/orientation
			System.out.println("num_snakes max is 6, but current value is greater.");
			System.exit(7);
		}
		snakes = new Snake[num_snakes];
		food = new Point[num_food];
		scores = new int[num_snakes];
		for (int i = 0; i < num_snakes; i++) {
			snakes[i] = new Snake(i, startX[i], startY[i], startDirection[i], colors[i]);
		}
		for (int i = 0; i < num_food; i++) {
			spawnFood(i);
		}
		snakesAlive = num_snakes;
		setStatusBar();
		
		gameOn = true;
		paused = false;
		thread = new Thread(this);
		thread.start();
	}
	
	private void setStatusBar() {
		statusbar = "speed: "+updateTime;
		for (int i = 0; i < num_snakes; i++) {
			statusbar += " | s"+(i+1)+":"+scores[i];
			if (!snakes[i].isAlive()) {
				statusbar += "(x)";
			}
		}
		return;
	}
	
	public void spawnFood(int index) {
		Tile[][] temp = new Tile[boardH][boardW];
		for (int i = 0, n = food.length; i < n; i++) {
			if (food[i] == null) {
				continue;
			}
			temp[food[i].y][food[i].x] = Tile.Food;
		}
		List<Integer> shuffledX = new ArrayList<Integer>();
		List<Integer> shuffledY = new ArrayList<Integer>();
		for (int i = 0; i < boardW; i++) {
			shuffledX.add(i);
		}
		for (int i = 0; i < boardH; i++) {
			shuffledY.add(i);
		}
		Collections.shuffle(shuffledX);
		Collections.shuffle(shuffledY);
		
		for (int i = 0; i < boardH; i++) {
			for (int j = 0; j < boardW; j++) {
				if (temp[shuffledY.get(i)][shuffledX.get(j)] == Tile.Food) {
					continue;
				}
				if (board[shuffledY.get(i)][shuffledX.get(j)] == Tile.Empty) {
					food[index] = new Point(shuffledX.get(j), shuffledY.get(i));
					return;
				}
			}
		}
	}
	
	public void add_turn(GameState gs) {
		previous_turns.addFirst(gs);
		if (previous_turns.size() > max_turns) {
			gs = previous_turns.removeLast();
		}
	}
	
	public void previous_turn() {
		if (previous_turns.size() > 0) {
			GameState gs = previous_turns.removeFirst();
			snakes = gs.getSnakes();
			food = gs.getFood();
			scores = gs.getScores();
			snakesAlive = gs.getSnakesAlive();
		}
	}
	
	public void print_snakes() {
		for (int i = 0; i < snakes.length; i++) {
			List<Point> coords = snakes[i].getCoords();
			System.out.print("Snake "+snakes[i].getID()+":");
			for (int j = 0; j < coords.size(); j++) {
				System.out.print(" ("+coords.get(j).x+","+coords.get(j).y+")");
			}
			System.out.println();
		}
	}
	
	public void run() {
		while (playable) {
			if (!paused) {
				time2 = System.currentTimeMillis();
				if (time2 - time1 >= updateTime) {
					if (gameOn) {
						updateSnakes();
						//updateBoard();
					}
					repaint();
					time1 = time2;
				}
			}
		}
		
		playable = true;
	}
	
	public void keyTyped(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
			case (KeyEvent.VK_P):
				paused = !paused;
				break;
			case (KeyEvent.VK_SPACE):
				if (!gameOn) {
					playable = false;
					restart();
					break;
				}
			case (KeyEvent.VK_OPEN_BRACKET):
				if (gameOn) {
					updateTime += 10;
				}
				break;
			case (KeyEvent.VK_CLOSE_BRACKET):
				if (gameOn) {
					if (updateTime > 19) 
						updateTime -= 10;
					else updateTime = 10;
				}
				break;
			case (KeyEvent.VK_LEFT):
				if (!gameOn || paused) {
					gameOn = true;
					paused = true;
					previous_turn();
					repaint();
				}
				break;
			case (KeyEvent.VK_RIGHT):
				if (!gameOn || paused) {
					gameOn = true;
					paused = true;
					updateSnakes();
					repaint();
				}
		}
	}

	public void keyPressed(KeyEvent e) {
		int WASDindex = -1;
		int arrowIndex = -1;
		for (int i = 0; i < num_snakes; i++) {
			if (snake_brains[i] == 0) {
				if (WASDindex == -1) {
					WASDindex = i;
				}
				else if (arrowIndex == -1) {
					System.out.println("Cannot have more than 2 snakes being controlled by the keyboard.");
					System.exit(10);
				}
				else {
					arrowIndex = i;
				}
			}
		}
		if (WASDindex != -1) { 
			Direction tmp = snakes[WASDindex].getDirection();
			switch (e.getKeyCode()) {
				case (KeyEvent.VK_W):
					if (tmp != Direction.Down) {
						snakes[WASDindex].moveDirection(Direction.Up);
					}
					break;
				case (KeyEvent.VK_S):
					if (tmp != Direction.Up) {
						snakes[WASDindex].moveDirection(Direction.Down);
					}
					break;
				case (KeyEvent.VK_D):
					if (tmp != Direction.Left) {
						snakes[WASDindex].moveDirection(Direction.Right);
					}
					break;
				case (KeyEvent.VK_A):
					if (tmp != Direction.Right) {
						snakes[WASDindex].moveDirection(Direction.Left);
					}
					break;
			}
		}
		if (arrowIndex != -1) { 
			Direction tmp = snakes[arrowIndex].getDirection();
			switch (e.getKeyCode()) {
				case (KeyEvent.VK_UP):
					if (tmp != Direction.Down) {
						snakes[arrowIndex].moveDirection(Direction.Up);
					}
					break;
				case (KeyEvent.VK_DOWN):
					if (tmp != Direction.Up) {
						snakes[arrowIndex].moveDirection(Direction.Down);
					}
					break;
				case (KeyEvent.VK_RIGHT):
					if (tmp != Direction.Left) {
						snakes[arrowIndex].moveDirection(Direction.Right);
					}
					break;
				case (KeyEvent.VK_LEFT):
					if (tmp != Direction.Right) {
						snakes[arrowIndex].moveDirection(Direction.Left);
					}
					break;
			}
		}
	}
}
