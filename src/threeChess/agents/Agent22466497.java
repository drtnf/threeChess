package threeChess.agents;

import threeChess.Agent;
import threeChess.Board;
import threeChess.Position;

import java.util.ArrayList;


/**
 * A Q-Learning agent for ThreeChess.
 * To Do:
 * 
 * 
 * Current Issues:
 * 
 * 
 */
public class Agent22466497 extends Agent {

  private final String name = "Agent22466497";

  /* Private Helper Methods */

  /**
   * Given a board position, returns a 2D array of all the valid moves that can be performed
   * from the current position by the player whose turn it is to move.
   * @param board the current state of the game.
   * @return a 2D array, where the second dimension is 2 elements long, indicating all the valid moves for the current player.
   */
  private Position[][] validMoves(Board board) {
    Position[] pieces = board.getPositions(board.getTurn()).toArray(new Position[0]);
    Position[] spaces = Position.values();
    ArrayList<Position[]> valid_moves = new ArrayList<>();
    // Enumerate over all possible move spaces for all pieces
    for (Position piece : pieces) {
      for (Position space : spaces) {
        if (board.isLegalMove(piece, space) && !valid_moves.contains(new Position[] {piece, space})) valid_moves.add(new Position[] {piece, space});
      }
    }

    return valid_moves.toArray(new Position[0][0]);
  }

  /* Public Methods */

  /**
   * Play a move in the game. 
   * The agent is given a Board Object representing the position of all pieces, the history of the game and whose turn it is. 
   * They respond with a move represented by a pair (two element array) of positions: [start, end]
   * @param board The representation of the game state.
   * @return a two element array of Position objects, where the first element is the current position of the piece to be moved,
   * and the second element is the position to move that piece to.
   */
  public Position[] playMove(Board board) {
    // TODO Auto-generated method stub
    return null;
  }

  public String toString() {return name;}

  public void finalBoard(Board finalBoard) {}
  
}
