
import java.awt.EventQueue;

import javax.swing.JFrame;

public class Frame extends JFrame {

	/*
	 * Ideas:
	 * 		Add second snake. Make one snake playable by human.
	 * 		Add obstacles
	 * 		Create UI with menu screen.
	 * 		Write code to analyze the algorithm data such as: 
	 * 				win %, win time variance, loop %, error %, average score
	 */
	
	private Board board;
	
	public Frame() {
		initFrame();
	}
	
	public void initFrame() {
		board = new Board();
		add(board);
		board.start();
		
		setResizable(false);
		setTitle("Snake vs Snake");
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				Frame game = new Frame();
				game.setVisible(true);
			}
		});
	}
}
