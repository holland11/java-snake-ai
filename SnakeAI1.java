import java.util.*;
import java.awt.Point;

public class SnakeAI1 {
	
	/*
	When the game is set to only 1 snake and 1 food on the board, I base my AI's decisions around the priority of being able to always
	reach its own tail. Being able to reach your own tail guarantees you aren't trapping yourself. As only one snake, if you find a 
	path to food that is 'safe' (can reach own tail  after), then you can commit to the entire path right away. 
	
	The solo snake mindset is based on being able to completely predict the future state of the board.
	With multiple snakes (and sometimes multiple food), the AI gets a lot trickier as you can't reliably predict the other snakes movements.
	Here are a few ideas / concepts for decision making:
		:: when running pathfinding algorithm's, do you want to estimate+simulate the other snakes' AI decisions as well for each equivalent game turn of the path?
		:: I suspect this would lead to a fairly significant increase in your win rate, however that is a very significant increase in computations per turn.
		:: if you decide to simulate opponents each turn along the BFS:
		:: :: is being able to reach your tail on every move still a priority? I would lean to yes as the extra safety this helps maintain is very powerful.
		:: :: how smart of AI are you going to put into the opponents moves? maybe just a simple bfs (then how do they handle not having a path to food?)
		:: if you decide not to simulate each turn along BFS are you going to treat the snake coordinates as stationary obstacles or what?:
		:: 
		:: how are you deciding which food to target?
		:: :: you could run a bfs on all other snakes first to see their estimated distances to each piece of food,
		:: :: then run yours and see if you are the closest snake to any given food. 
		:: :: if yes, either take your 'path precautions' first or jump right into it?
		:: :: if not closest to any, try to make safe movements that ideally put you in a location either central or specifically look for one that gives
		:: :: the highest odds of being close to the next food spawn.
		::
		:: depending on how high of a priority getting to food is, it could open the door to much more passive playstyles where
		:: a majority of your game is spent buying time and waiting for very optimistic conditions to arise for snatching food.
		:: 
	When your snake is buying time because there is no worthy path available, how you buy that time can result in being much more likely to be the closest snake
	to future food spawns. 
	
	solo_snake_mindset(snakes, food):
		-) look for shortest path to food using BFS
		  -) if found, 'simulate' that path then look for a path to its own tail's position
		    -) if can reach own tail, this means you aren't trapping yourself so the BFS path is safe
		    --
		**	-) if can't reach own tail, BFS route isn't safe so we must find an alternative route
			  -) simulate each (up, down, left, right) move that is valid for current position:
			    -) from those simulations, test whether tail can be reached and if so, how long is the route to the tail?
				-) if tail can be reached from at least one of the moves
				  -) make the move which leads to the longest valid bfs tail route
				-) if tail can't be reached from any of the moves
				  -) aim for something else (maybe different unoccupied cells surrounding the tail, or just the cell with the furthest distance away found in BFS/DFS route)
				    -) make the move that leads to the longest path to whatever you decide to aim at
		  -) if no BFS route to food found
		    -) use the time buying method from above (line marked by **)
			
	Other ideas:
		Run a BFS to closest food for each snake to look at its likely next 3 moves. Compare your route's moves to these moves to help predict danger.
		(If their snake runs into your body, it's still a safe route. If your snake runs into theirs, it's not safe.)
		You could also test to see if your route has been compromised by storing and checking all the cells on your route so that you see you're route won't likely stay open.
	
		Instead of passing in the whole snakes array for each path search, create a visited array once that gets gets copied in for the method to use.
	
		Make the other snakes' "forward" cell also off limits to reduce head on collisions.
	*/
	
	public static Direction getSingleMove(GameState gs, int snakeID) {
		Snake snakes[] = gs.getSnakes();
		Point food[] = gs.getFood();
		int height = gs.getHeight();
		int width = gs.getWidth();
		
		Direction food_route_move = closest_or_tail_chase(snakes, snakeID, food, height, width);
		if (food_route_move != null) {
			return food_route_move;
		}
		return null;
	}
	
	private static void printBoard(Snake[] snakes, Point[] food, int height, int width) {
		char[][] board = new char[height][width];
		for (int i = 0; i < snakes.length; i++) {
			List<Point> coords = snakes[i].getCoords();
			for (int j = 0; j < coords.size(); j++) {
				board[coords.get(j).y][coords.get(j).x] = (char)((int)'a' + i);
			}
		}
		for (int i = 0; i < food.length; i++) {
			board[food[i].y][food[i].x] = '$';
		}
		for (int i = 0; i < width+2; i++) {
			System.out.print("=");
		}
		System.out.println();
		for (int i = 0; i < height; i++) {
			System.out.print("=");
			for (int j = 0; j < width; j++) {
				if (board[i][j] == '\0') {
					System.out.print(".");
				} else {
					System.out.print(board[i][j]);
				}				
			}
			System.out.println("=");
		}
		for (int i = 0; i < width+2; i++) {
			System.out.print("=");
		}
		System.out.println();
	}
	
	private static Node tail_chasing_bfs(Snake[] snakes, int snakeID, Point[] food, int height, int width) {
		return null;
	}
	
	private static Node oracle_tail_chasing(Snake[] snakes, int snakeID, Point[] food, int height, int width) {
		return null;
	}
	
	private static Direction closest_or_tail_chase(Snake[] snakes, int snakeID, Point[] food, int height, int width) {
		int[] closest_snake = new int[food.length];
		Node[] routes_to_food = closest_snake_to_each_food(snakes, snakeID, food, height, width, closest_snake);
		for (int i = 0; i < food.length; i++) {
			if (routes_to_food[i] != null && closest_snake[i] == snakeID) {
				// our snake is the closest snake to this food
				// simulate the BFS route to the food then see if our snake can still reach its tail
				// if it can, use the first move from the route
				// if it cant, skip this food
				// if no route gets accepted, tail chase
				List<Direction> directions = path_ends_safe(routes_to_food[i], snakes, snakeID, height, width);
				if (directions != null && directions.size() > 0) {
					return directions.get(0);
				}
			}
		}
		Direction dir = long_tail_route(snakes, snakeID, height, width);
		if (dir != null) {
			return dir;
		}
		else {
			dir = safe_route(snakes, snakeID, height, width);
			if (dir == null) {
				System.out.println("Can't even find a safe route????");
			}
			return dir;
		}
	}
	
	private static Direction long_tail_route(Snake[] snakes, int snakeID, int height, int width) {
		System.out.println("in long tail route");
		int snake_index = -1;
		boolean[][] board_move_left = new boolean[height][width];
		boolean[][] board_move_right = new boolean[height][width];
		boolean[][] board_move_up = new boolean[height][width];
		boolean[][] board_move_down = new boolean[height][width];
		Point next_tail = new Point(-1,-1);
		System.out.println("Looking for ID: "+snakeID);
		for (int i = 0; i < snakes.length; i++) {
			System.out.println("index: "+i+" id: "+snakes[i].getID());
			if (snakeID == snakes[i].getID()) {
				snake_index = i;
				List<Point> coords = snakes[i].getCoords();
				for (int j = 0; j < coords.size()-1; j++) {
					board_move_left[coords.get(j).y][coords.get(j).x] = true;
					board_move_right[coords.get(j).y][coords.get(j).x] = true;
					board_move_up[coords.get(j).y][coords.get(j).x] = true;
					board_move_down[coords.get(j).y][coords.get(j).x] = true;
				}
				System.out.println("Coords size: "+coords.size());
				next_tail.x = coords.get(coords.size()-1).x;
				next_tail.y = coords.get(coords.size()-1).y;
			}
			else {
				List<Point> coords = snakes[i].getCoords();
				for (int j = 0; j < coords.size()-1; j++) {
					board_move_left[coords.get(j).y][coords.get(j).x] = true;
					board_move_right[coords.get(j).y][coords.get(j).x] = true;
					board_move_up[coords.get(j).y][coords.get(j).x] = true;
					board_move_down[coords.get(j).y][coords.get(j).x] = true;
				}
			}
		}
		int headX = snakes[snake_index].getHeadX();
		int headY = snakes[snake_index].getHeadY();
		Direction dir = null;
		Node end_node = null;
		Node route = null;
		if (headX > 0 && board_move_left[headY][headX-1] == false) {
			route = BFS_Route(board_move_left, new Point(headX-1, headY), next_tail);
			if (route != null) {
				if (end_node == null) {
					end_node = route;
					dir = Direction.Left;
				}
				else if (route.getDepth() > end_node.getDepth()) {
					end_node = route;
					dir = Direction.Left;
				}
			}
		}
		if (headX < width-1 && board_move_right[headY][headX+1] == false) {
			route = BFS_Route(board_move_right, new Point(headX+1, headY), next_tail);
			if (route != null) {
				if (end_node == null) {
					end_node = route;
					dir = Direction.Right;
				}
				else if (route.getDepth() > end_node.getDepth()) {
					end_node = route;
					dir = Direction.Right;
				}
			}
		}
		if (headY < height-1 && board_move_down[headY+1][headX] == false) {
			route = BFS_Route(board_move_right, new Point(headX, headY+1), next_tail);
			if (route != null) {
				if (end_node == null) {
					end_node = route;
					dir = Direction.Down;
				}
				else if (route.getDepth() > end_node.getDepth()) {
					end_node = route;
					dir = Direction.Down;
				}
			}
		}
		if (headY > 0 && board_move_up[headY-1][headX] == false) {
			route = BFS_Route(board_move_right, new Point(headX, headY-1), next_tail);
			if (route != null) {
				if (end_node == null) {
					end_node = route;
					dir = Direction.Up;
				}
				else if (route.getDepth() > end_node.getDepth()) {
					end_node = route;
					dir = Direction.Up;
				}
			}
		}
		return dir;
	}
	
	private static Direction safe_route(Snake[] snakes, int snakeID, int height, int width) {
		System.out.println("in safe route");
		boolean[][] board = new boolean[height][width];
		boolean[][] board_move_left = new boolean[height][width];
		boolean[][] board_move_right = new boolean[height][width];
		boolean[][] board_move_up = new boolean[height][width];
		boolean[][] board_move_down = new boolean[height][width];
		int snake_index = -1;
		for (int i = 0; i < snakes.length; i++) {
			if (snakes[i].getID() == snakeID) {
				snake_index = i;
			}
			List<Point> coords = snakes[i].getCoords();
			for (int j = 0; j < coords.size(); j++) {
				board[coords.get(j).y][coords.get(j).x] = true;
				if (j == coords.size()-1) {
					break;
				}
				board_move_left[coords.get(j).y][coords.get(j).x] = true;
				board_move_right[coords.get(j).y][coords.get(j).x] = true;
				board_move_up[coords.get(j).y][coords.get(j).x] = true;
				board_move_down[coords.get(j).y][coords.get(j).x] = true;
			}
		}
		Node furthest_node = maximum_bfs(board, snakes[snake_index].getHeadX(), snakes[snake_index].getHeadY());
		if (furthest_node == null) {
			System.out.println("Unable to find a furthest node?");
			return null;
		}
		Point target = new Point(furthest_node.getX(),furthest_node.getY());
		int headX = snakes[snake_index].getHeadX();
		int headY = snakes[snake_index].getHeadY();
		Direction dir = null;
		Node end_node = null;
		Node route = null;
		if (headX > 0 && board_move_left[headY][headX-1] == false) {
			route = BFS_Route(board_move_left, new Point(headX-1, headY), target);
			if (route != null) {
				if (end_node == null) {
					end_node = route;
					dir = Direction.Left;
				}
				else if (route.getDepth() > end_node.getDepth()) {
					end_node = route;
					dir = Direction.Left;
				}
			}
		}
		if (headX < width-1 && board_move_right[headY][headX+1] == false) {
			route = BFS_Route(board_move_right, new Point(headX+1, headY), target);
			if (route != null) {
				if (end_node == null) {
					end_node = route;
					dir = Direction.Right;
				}
				else if (route.getDepth() > end_node.getDepth()) {
					end_node = route;
					dir = Direction.Right;
				}
			}
		}
		if (headY < height-1 && board_move_down[headY+1][headX] == false) {
			route = BFS_Route(board_move_right, new Point(headX, headY+1), target);
			if (route != null) {
				if (end_node == null) {
					end_node = route;
					dir = Direction.Down;
				}
				else if (route.getDepth() > end_node.getDepth()) {
					end_node = route;
					dir = Direction.Down;
				}
			}
		}
		if (headY > 0 && board_move_up[headY-1][headX] == false) {
			route = BFS_Route(board_move_right, new Point(headX, headY-1), target);
			if (route != null) {
				if (end_node == null) {
					end_node = route;
					dir = Direction.Up;
				}
				else if (route.getDepth() > end_node.getDepth()) {
					end_node = route;
					dir = Direction.Up;
				}
			}
		}
		return dir;
	}
	
	private static Node maximum_bfs(boolean[][] visited, int startX, int startY) {
		int height = visited.length;
		int width = visited[0].length;
		Queue<Node> q = new LinkedList<>();
		Node current_node = new Node(startX,startY,null,0);
		Node furthest_node = current_node;
		q.add(current_node);
		while (q.size() > 0) {
			current_node = q.remove();
			if (current_node.getDepth() > furthest_node.getDepth()) {
				furthest_node = current_node;
			}
			if (current_node.getX() > 0) {
				if (visited[current_node.getY()][current_node.getX()-1] == false) {
					visited[current_node.getY()][current_node.getX()-1] = true;
					q.add(new Node(current_node.getX()-1, current_node.getY(), current_node, current_node.getDepth()+1));
				}
			}
			if (current_node.getX() < width-1) {
				if (visited[current_node.getY()][current_node.getX()+1] == false) {
					visited[current_node.getY()][current_node.getX()+1] = true;
					q.add(new Node(current_node.getX()+1, current_node.getY(), current_node, current_node.getDepth()+1));
				}
			}
			if (current_node.getY() > 0) {
				if (visited[current_node.getY()-1][current_node.getX()] == false) {
					visited[current_node.getY()-1][current_node.getX()] = true;
					q.add(new Node(current_node.getX(), current_node.getY()-1, current_node, current_node.getDepth()+1));
				}
			}
			if (current_node.getY() < height-1) {
				if (visited[current_node.getY()+1][current_node.getX()] == false) {
					visited[current_node.getY()+1][current_node.getX()] = true;
					q.add(new Node(current_node.getX(), current_node.getY()+1, current_node, current_node.getDepth()+1));
				}
			}
		}
		return furthest_node;
	}
	
	private static List<Direction> path_ends_safe(Node start_node, Snake[] snakes, int snakeID, int height, int width) {
		Snake future_snake = snakes[snakeID].clone();
		List<Direction> directions = directions_from_start_node(start_node);
		future_snake.setMoveQ(directions);
		while (future_snake.getMoveQ().size() != 0 && future_snake.getMoveQ().get(0) != null) {
			future_snake.moveForward();
		}
		Node path_back_to_tail = bfs_to_tail(snakes, snakeID, future_snake, height, width, start_node.getDepth());
		if (path_back_to_tail != null) {
			return directions;
		}
		return null;
	}
	
	private static Node bfs_to_tail(Snake[] snakes, int snakeID, Snake future_snake, int height, int width, int how_far_in_future) {
		// how far in the future is used to remove part of the tails off the other snakes since they will have moved and those spaces should end up unoccupied
		if (how_far_in_future > 3) {
			how_far_in_future = 3;
		}
		Point tail = null;
		Point head = null;
		boolean[][] visited = new boolean[height][width];
		for (int i = 0; i < snakes.length; i++) {
			if (snakes[i].getID() == snakeID) {
				List<Point> coords = future_snake.getCoords();
				for (int j = 0; j < coords.size()-1; j++) {
					visited[coords.get(j).y][coords.get(j).x] = true;
				}
				head = coords.get(0);
				tail = new Point(coords.get(coords.size()-1).x, coords.get(coords.size()-1).y); 
			} else {
				List<Point> coords = snakes[i].getCoords();
				for (int j = 0; j < coords.size()-how_far_in_future; j++) {
					visited[coords.get(j).y][coords.get(j).x] = true;
				}
			}
		}
		return BFS_Route(visited, head, tail);
	}
	
	private static Node BFS_Route(boolean[][] visited, Point start, Point end) {
		int height = visited.length;
		int width = visited[0].length;
		Queue<Node> q = new LinkedList<>();
		Node current_node = new Node(start.x,start.y,null,0);
		q.add(current_node);
		while (q.size() > 0) {
			current_node = q.remove();
			if (current_node.getX() == end.x && current_node.getY() == end.y) {
				return current_node;
			}
			if (current_node.getX() > 0) {
				if (visited[current_node.getY()][current_node.getX()-1] == false) {
					visited[current_node.getY()][current_node.getX()-1] = true;
					q.add(new Node(current_node.getX()-1, current_node.getY(), current_node, current_node.getDepth()+1));
				}
			}
			if (current_node.getX() < width-1) {
				if (visited[current_node.getY()][current_node.getX()+1] == false) {
					visited[current_node.getY()][current_node.getX()+1] = true;
					q.add(new Node(current_node.getX()+1, current_node.getY(), current_node, current_node.getDepth()+1));
				}
			}
			if (current_node.getY() > 0) {
				if (visited[current_node.getY()-1][current_node.getX()] == false) {
					visited[current_node.getY()-1][current_node.getX()] = true;
					q.add(new Node(current_node.getX(), current_node.getY()-1, current_node, current_node.getDepth()+1));
				}
			}
			if (current_node.getY() < height-1) {
				if (visited[current_node.getY()+1][current_node.getX()] == false) {
					visited[current_node.getY()+1][current_node.getX()] = true;
					q.add(new Node(current_node.getX(), current_node.getY()+1, current_node, current_node.getDepth()+1));
				}
			}
		}
		return null;
	}
	
	private static List<Direction> directions_from_start_node(Node node) {
		List<Direction> directions = new ArrayList<Direction>();
		while (node.getPrev() != null) {
			Node next_node = node.getPrev();
			int x = next_node.getX() - node.getX();
			int y = next_node.getY() - node.getY();
			if (x > 0) {
				directions.add(Direction.Right);
			} else if (x < 0) {
				directions.add(Direction.Left);
			} else if (y > 0) {
				directions.add(Direction.Down);
			} else if (y < 0) {
				directions.add(Direction.Up);
			} else {
				System.out.println("No difference in coordinates between node and next_node.");
			}
			node = next_node;
		}
		return directions;
	}
	
	private static Node[] closest_snake_to_each_food(Snake[] snakes, int snakeID, Point[] food, int height, int width, int[] closest_snake) {
		// returns an array of Nodes which contain the shortest paths from each piece of food to a snake head.
		// the Node objects themselves will be located on a snake head, but are the head of a linked list containing the whole path to the food.
		// if one of the Node objects is null, that means no available route to a snake head from that food.
		// originally returned the closest_snake array which represents which snake is closest to each piece of food. 
		// closest_snake[1] = 2 means that the closest snake to food[1] is the snake with ID 2
		// now closest_snake is still updated by the method, but it's done as an array passed as an argument.
		int[][] snake_head_matrix = new int[height][width];
		Node[] end_points = new Node[food.length];
		
		for (int k = 0; k < food.length; k++) {
			Queue<Node> q = new LinkedList<Node>();
			boolean[][] visited = new boolean[height][width];
			for (int i = 0, n = snakes.length; i < n; i++) {
				if (snakes[i].isAlive() == false) {
					continue;
				}
				List<Point> coords = snakes[i].getCoords();
				snake_head_matrix[coords.get(0).y][coords.get(0).x] = i+1;
				for (int j = 1, m = coords.size(); j < m; j++) {
					visited[coords.get(j).y][coords.get(j).x] = true;
				}
			}
			q.add(new Node(food[k].x, food[k].y, null, 0));
			while (q.size() > 0) {
				Node current_node = q.remove();
				if (snake_head_matrix[current_node.getY()][current_node.getX()] != 0) {
					// closest snake head found for food[k]
					// store node (with linked list path) in end_points[k], store snakeID in closest_snake[]
					closest_snake[k] = snake_head_matrix[current_node.getY()][current_node.getX()]-1;
					end_points[k] = current_node;
					break;
				}
				if (current_node.getX() > 0) {
					if (visited[current_node.getY()][current_node.getX()-1] == false) {
						visited[current_node.getY()][current_node.getX()-1] = true;
						q.add(new Node(current_node.getX()-1, current_node.getY(), current_node, current_node.getDepth()+1));
					}
				}
				if (current_node.getX() < width-1) {
					if (visited[current_node.getY()][current_node.getX()+1] == false) {
						visited[current_node.getY()][current_node.getX()+1] = true;
						q.add(new Node(current_node.getX()+1, current_node.getY(), current_node, current_node.getDepth()+1));
					}
				}
				if (current_node.getY() > 0) {
					if (visited[current_node.getY()-1][current_node.getX()] == false) {
						visited[current_node.getY()-1][current_node.getX()] = true;
						q.add(new Node(current_node.getX(), current_node.getY()-1, current_node, current_node.getDepth()+1));
					}
				}
				if (current_node.getY() < height-1) {
					if (visited[current_node.getY()+1][current_node.getX()] == false) {
						visited[current_node.getY()+1][current_node.getX()] = true;
						q.add(new Node(current_node.getX(), current_node.getY()+1, current_node, current_node.getDepth()+1));
					}
				}
			}
		}
		return end_points;
	}
	
	private static Node aStarRoute(Snake[] snakes, int snakeID, Point[] food, int height, int width) {
		// Node returned is the end of the route and has a pointer chain which leads to the start (representing the whole path)
		
		// this pathfinding algorithm is based on A*
		// it is similar to BFS however the Queue will be sorted based on each node's estimated distance to food
		// A* is meant to be more efficient since you are more likely to look at nodes which are on the way to your destination
		// however it forces us to calculate the estimated distance before each Node is added to the queue 
		// which takes some time
		// could figure out the closest piece of food from the start and just focus on that one when calculating distance
		// or could look at each piece of food on each node addition to always find the closest piece of food to that node
		// with many pieces of food and many snakes on the board, the potential paths won't be as open and therefore the estimated distances won't be as accurate
		// so I'm not convinced A* would actually be better than BFS for this particular context
		PriorityQueue<Node> pq = new PriorityQueue<Node>(new Comparator<Node>() {
			public int compare(Node n1, Node n2) {
				if (n1.getDistance() < n2.getDistance()) {
					return -1;
				} else if (n1.getDistance() > n2.getDistance()) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		
		boolean[][] visited = new boolean[height][width];
		updateVisited(visited, snakes);
		boolean[][] foodMatrix = new boolean[height][width];
		for (int i = 0, n = food.length; i < n; i++) {
			foodMatrix[food[i].y][food[i].x] = true;
		}
		for (int i = 0, n = snakes.length; i < n; i++) {
			if (snakes[i].getID() == snakeID) {
				pq.offer(new Node(snakes[i].getHeadX(), snakes[i].getHeadY(), null, 0, 0));
				break;
			}
		}
		
		while (pq.peek() != null) {
			Node current_node = pq.poll();
			if (foodMatrix[current_node.getY()][current_node.getX()]) {
				return current_node;
			}
			if (current_node.getX() > 0) {
				if (visited[current_node.getY()][current_node.getX()-1] == false) {
					// estimate the distance to the closest piece of food
					// this distance will be used as the key in our priority queue
					double min_distance = 0;
					for (int j = 0; j < food.length; j++) {
						int x_dist = food[j].x - current_node.getX()-1;
						int y_dist = food[j].y - current_node.getY();
						double this_dist = Math.sqrt((x_dist*x_dist) + (y_dist*y_dist));
						if (j == 0 || this_dist < min_distance) {
							min_distance = this_dist;
						}
					}
					visited[current_node.getY()][current_node.getX()-1] = true;
					pq.offer(new Node(current_node.getX()-1, current_node.getY(), current_node, current_node.getDepth()+1, min_distance));
				}
			}
			if (current_node.getX() < width-1) {
				if (visited[current_node.getY()][current_node.getX()+1] == false) {
					double min_distance = 0;
					for (int j = 0; j < food.length; j++) {
						int x_dist = food[j].x - current_node.getX()+1;
						int y_dist = food[j].y - current_node.getY();
						double this_dist = Math.sqrt((x_dist*x_dist) + (y_dist*y_dist));
						if (j == 0 || this_dist < min_distance) {
							min_distance = this_dist;
						}
					}
					visited[current_node.getY()][current_node.getX()+1] = true;
					pq.offer(new Node(current_node.getX()+1, current_node.getY(), current_node, current_node.getDepth()+1, min_distance));
				}
			}
			if (current_node.getY() > 0) {
				if (visited[current_node.getY()-1][current_node.getX()] == false) {
					double min_distance = 0;
					for (int j = 0; j < food.length; j++) {
						int x_dist = food[j].x - current_node.getX();
						int y_dist = food[j].y - current_node.getY()-1;
						double this_dist = Math.sqrt((x_dist*x_dist) + (y_dist*y_dist));
						if (j == 0 || this_dist < min_distance) {
							min_distance = this_dist;
						}
					}
					visited[current_node.getY()-1][current_node.getX()] = true;
					pq.offer(new Node(current_node.getX(), current_node.getY()-1, current_node, current_node.getDepth()+1, min_distance));
				}
			}
			if (current_node.getY() < height-1) {
				if (visited[current_node.getY()+1][current_node.getX()] == false) {
					double min_distance = 0;
					for (int j = 0; j < food.length; j++) {
						int x_dist = food[j].x - current_node.getX();
						int y_dist = food[j].y - current_node.getY()+1;
						double this_dist = Math.sqrt((x_dist*x_dist) + (y_dist*y_dist));
						if (j == 0 || this_dist < min_distance) {
							min_distance = this_dist;
						}
					}
					visited[current_node.getY()+1][current_node.getX()] = true;
					pq.offer(new Node(current_node.getX(), current_node.getY()+1, current_node, current_node.getDepth()+1, min_distance));
				}
			}
		}
		return null;
	}
	
	private static Node bfsRoute(Snake[] snakes, int snakeID, Point[] food, int height, int width) {
		// Node returned is the end of the route and has a pointer chain which leads to the start (representing the whole path)
		Queue<Node> q = new LinkedList<Node>();
		boolean[][] visited = new boolean[height][width];
		updateVisited(visited, snakes);
		boolean[][] foodMatrix = new boolean[height][width];
		for (int i = 0, n = food.length; i < n; i++) {
			foodMatrix[food[i].y][food[i].x] = true;
		}
		for (int i = 0, n = snakes.length; i < n; i++) {
			if (snakes[i].getID() == snakeID) {
				q.add(new Node(snakes[i].getHeadX(), snakes[i].getHeadY(), null, 0));
				break;
			}
		}
		
		while (q.size() > 0) {
			Node current_node = q.remove();
			if (foodMatrix[current_node.getY()][current_node.getX()]) {
				return current_node;
			}
			if (current_node.getX() > 0) {
				if (visited[current_node.getY()][current_node.getX()-1] == false) {
					visited[current_node.getY()][current_node.getX()-1] = true;
					q.add(new Node(current_node.getX()-1, current_node.getY(), current_node, current_node.getDepth()+1));
				}
			}
			if (current_node.getX() < width-1) {
				if (visited[current_node.getY()][current_node.getX()+1] == false) {
					visited[current_node.getY()][current_node.getX()+1] = true;
					q.add(new Node(current_node.getX()+1, current_node.getY(), current_node, current_node.getDepth()+1));
				}
			}
			if (current_node.getY() > 0) {
				if (visited[current_node.getY()-1][current_node.getX()] == false) {
					visited[current_node.getY()-1][current_node.getX()] = true;
					q.add(new Node(current_node.getX(), current_node.getY()-1, current_node, current_node.getDepth()+1));
				}
			}
			if (current_node.getY() < height-1) {
				if (visited[current_node.getY()+1][current_node.getX()] == false) {
					visited[current_node.getY()+1][current_node.getX()] = true;
					q.add(new Node(current_node.getX(), current_node.getY()+1, current_node, current_node.getDepth()+1));
				}
			}
		}
		return null;
	}
	
	private static Direction first_move_from_node_list(Node end_node) {
		// walks down the path that led to end_node to determine what direction the first move was
		Node future_node = end_node;
		Node past_node = end_node.getPrev();
		while (past_node.getPrev() != null) {
			future_node = past_node;
			past_node = past_node.getPrev();
		}
		int past_x = past_node.getX();
		int past_y = past_node.getY();
		int future_x = future_node.getX();
		int future_y = future_node.getY();
		if (past_x != future_x && past_y != future_y) {
			System.out.println("Error. future_node and past_node are more than 1 move apart.");
			System.exit(12);
		}
		if (future_x > past_x) {
			return Direction.Right;
		}
		else if (future_x < past_x) {
			return Direction.Left;
		}
		else if (future_y > past_y) {
			return Direction.Down;
		}
		else {
			return Direction.Up;
		}
	}
	
	private static void updateVisited(boolean[][] visited, Snake[] snakes) {
		for (int i = 0, n = snakes.length; i < n; i++) {
			if (snakes[i].isAlive() == false) {
				continue;
			}
			List<Point> coords = snakes[i].getCoords();
			for (int j = 0, m = coords.size(); j < m; j++) {
				visited[coords.get(j).y][coords.get(j).x] = true;
			}
		}
		return;
	}
	
	public static ArrayList<Direction> getMultiMove(GameState gs, int snakeID) {
		return null;
	}

	private static class Snake_State {
		Snake[] snakes;
		public Snake_State(Snake[] original_snakes) {
			snakes = new Snake[original_snakes.length];
			for (int i = 0, n = snakes.length; i < n; i++) {
				snakes[i] = original_snakes[i].clone();
			}
		}
	}
}