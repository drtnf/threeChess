# threeChess
An AI framework for playing three player chess, as part of the CITS3001 unit at UWA

# Project

## Three Chess

![](init.png)

The project will require you to research, implement and validate artifical intelligence for the board game ThreeChess. ThreeChess is a variation of chess played on a special board between three players, with the colours Blue, Green and Red. Each player takes turns moving their pieces, where the available moves depend on the type of piece. If your piece lands on a square occupied by an opponent's piece, the opponent's piece is removed from the board (captured) and the goal is to take one of your opponents' King. When a King is taken the game ends, and the person who took the King is the winner, the person who lost the King is the loser, and the third player neither wins nor loses.

* * *

## Rules

The offical rules, and basic hints are available [here](https://www.threechess.com/en/three-player-chess-rules-3.html), as well as an online game you can play (it seems a bit buggy). The project will work with a variation of the rules described below:

*   Players takes turns moving pieces. Blue always goes first, followed by Green, and the Red.
*   The board is set up as in the picture.
*   The piece moves are as follows:

    <dl>

    <dt>♚ King</dt>

    <dd>The King may move one square in any direction, including diagonally. If the King and the Rook are in their original position, and there are no pieces in between them, the King may move two squares towards the Rook, and the rook is moved to the square on the outside of the King. This is called _castling_. Unlike normal chess you can castle across check, and it does not matter if the King and the Rook have moved before.</dd>

    <dt>♛ Queen</dt>

    <dd>The Queen may move in a straight line in any direction (including diagonally), as many squares as are free but may not move through another piece.</dd>

    <dt>♜ Rook</dt>

    <dd>The Rook may move in a straight line forwards, backwards, left or right, but not diagonally, and may not move through another piece.</dd>

    <dt>♝ Bishop</dt>

    <dd>The Bishop may move in a straight line in any diagonal direction, but may not move through another piece.</dd>

    <dt>♞ Knight</dt>

    <dd>The Knight moves in a L-shape (one square forwards, backwards left or right, and then 2 squares in any perpendicular direction). The Knight may jump over other pieces.</dd>

    <dt>♟ Pawn</dt>

    <dd>The Pawn may only move 1 square forward at a time, with the following exceptions: one its first move, it may move two squares forwards; to take a piece it _must_ move diagonally forwards; and if it reaches the back rank, it is automatically promoted to a Queen (unlike normal chess, where the player can choose which piece to promote to). There is _no_ en passant in this version of the game</dd>

    </dl>

*   If your piece lands on an opponents piece that piece is taken or captured. This means it is removed from the board.
*   You may not capture your own piece.
*   The game ends when a King is captured. In the case, the person who took the King is the winner (+1), the person who lost the King is the loser (-1), and the thrid player neither wins nor loses (0).
*   When a piece is threatening an opponent's King, the King is said to be in _check_. Unlike normal chess, you can move into check, and are not required to take evasive action if you are in check. The King must be taken for the game to end, and it is not enough to trap a King in checkmate.
*   It is possible for a game to reach a position where the same position is repeated and no player can force the end of the game. In this case the game is a draw.
*   In a time version of the game, each player is given an amount of time that accumulates while they are considering their move (i.e. from the time their opponent to the right completes a move, until they move a piece). Once this time reaches a limit, the player who ran out of time is the loser. in this case, the opponent with the most remaining pieces, and who took the most pieces is the winner.

For this project you should in pairs. You will be required to research, implement and validate agents to play ThreeChess. You will be provided with a Java interface to implement an agent, some very basic agents, and a basic class to run a game. These will be made available on [github](https://github.com/drtnf/threeChess) and will be regularly updated.

Files are provided for your agents to use including:

*   Board.java which gives a representation of the current state of the game
*   ThreeChessDisplay.java which generates a JFrame displaying the board.
*   ThreeChess which contains methods for running games and tournaments between different agents.
*   Agent.java which is an abstract class that you must subclass and provide logic for the agent to play moves given a board position. To make running tournaments easier, your agent must be in the package threeChess.agents, it must have the name Agent########.java (where the hashes are your student number), and it must have a 0 parameter constructror.

There are a range of other classes as well, but those are the important one. 

To clone this repo use git clone https://github.com/drtnf/threeChess.git

To compile the files, in the root directory use javac -d bin src/threeChess/\*.java src/threeChess/agents/\*.java

To run a basic game use java -cp bin/ threeChess.ThreeChess 

* * *

## Getting started

There are many references for Chess AI and perfect information deterministic turn based board games in general. ThreeChess adds the complication of a third player. This makes techniques such as minimax and alpha-beta pruning less practical, since it assumes the worst of your opponents, and it is unlikely both opponents will target you (and if they do, there's not much you can doi about it.) The first step in designing a AI for the game is to play some games yourself and identify useful tactics. Once you have an idea of what makes a good position in the game, you can start work on an evaluation function, to give an approximate utility to a board position. You can then use that in a minimax search, a Monte Carlo tree search or a sequential decision problem. Some interesting references are included below, and we will add to this throughout semester.

*   a [paper on Monte Carlo tree-search](http://teaching.csse.uwa.edu.au/units/CITS3001/project/2017/paper1.pdf).
*   A Very good [project report](http://teaching.csse.uwa.edu.au/units/CITS3001/project/sampleReport.pdf) from a student in 2016 is also available
