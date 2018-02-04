This is a program I built in preparation for BattleSnake 2017.
BattleSnake is a programming competition where each team creates their own AI to play the classic game Snake.
Around 8 of those snakes (each controlled by one team's AI code) is placed on a board together with multiple pieces of food.
At each turn, each AI receives the game board information (location of each snake + each piece of food) and has to reply with
	the direction they want to move next.
At each turn, each snake loses 1 health.
To gain health, snakes must eat food. (This stops snakes from doing circles for the whole game).

I wanted to try different ideas for the AI before the competition so I built my own Snake game.
I then attempted to make it modular so that anyone without hassle could test their own AI with it. (Although their code would have to be in Java).

At the moment, I have a fairly simple AI which I created (inside the SnakeAI1.java file).
The SnakeAI2.java file is a template for anyone else that wants to try creating their own AI.

The game can be configured (in the Board.java) file to edit the number of snakes on the board,
	how each snake is controlled (at most two snakes can be controlled by humans using the WASD
	or arrows keys), the game speed, and the board size.
There is also a 'rewind' / 'step back/forward' feature built in. I felt that this was an important feature to have because
	it makes it a lot easier to analyze the decisions that a snake makes. For example, after your snake 'dies', you can 
	pause the game with 'P', then step back/forward using the Left/Right arrows keys to see the previous moves that led
	to that death. You can then think about how to program your AI to avoid that scenario in the future.
While  the game is running, the curly brace keys ( { and } ) can be used to slow down or speed up the game. If the game gets too
	fast though, the AI might not be able to make its computations fast enough on each turn.

This is the biggest project that I have done and I've learned a lot throughout it.

To compile and run the code:
	javac *.java
	java Frame