package threeChess.agents;

import threeChess.Agent;
import threeChess.Board;
import threeChess.Position;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a state-action pair.
 */
class SAPair {
  Board state;
  Position[] action;
  SAPair(Board s, Position[] a) {state = s; action = a;}
}

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

  // Q-Learning Parameters
  Board previousState; int previousReward; Position[] previousAction;
  Board currentState; int currentReward;
  double γ; // Discount Factor

  // Q-Learning Storage
  HashMap<SAPair, Double> Qvalues; // The table of Q-values, i.e. the table of state-action pair utilities.
  HashMap<SAPair, Integer> N_sa; // A 2D table, where <s, a>  keeps track of the number of times action a was performed while in state s.

  // Constructor
  public Agent22466497() {
    previousState = null; previousReward = 0; previousAction = new Position[2];
    currentState = null; currentReward = 0;
    γ = 0.95;
    Qvalues = new HashMap<>();
    N_sa = new HashMap<>();
  }

  /* Private Helper Methods */

  /**
   * Given a board position, returns a 2D array of all the valid moves that can be performed
   * from the current position by the player whose turn it is to move.
   * @param board the current state of the game.
   * @return a 2D array, where the second dimension is 2 elements long, indicating all the valid moves for the current player.
   */
  private Position[][] validMoves(Board board) {
    // Find all of our piece positions and all of the board spaces
    Position[] pieces = board.getPositions(board.getTurn()).toArray(new Position[0]);
    Position[] spaces = Position.values();
    ArrayList<Position[]> valid_moves = new ArrayList<>();
    // Enumerate over all possible move spaces for all pieces
    for (Position piece : pieces) {
      for (Position space : spaces) {
        // Start Position -> End Position, Piece -> Space
        Position[] currMove = new Position[] {piece, space};
        if (board.isLegalMove(piece, space) && !valid_moves.contains(currMove)) valid_moves.add(currMove);
      }
    }
    return valid_moves.toArray(new Position[0][0]);
  }

  /**
   * The learning function, η. This function specifies a learning rate that decreases over time.
   * @param numVisited the number of times that the current state-action pair has been visited.
   * @return a learning parameter acting as a decreasing factor over time as the value of the input increases.
   */
  private double η(int numVisited) {
    return (20.0 / (19.0 + numVisited));
  }

  /**
   * Calculates the highest possible utility associated with the best action from the current state.
   * @param state the current state.
   * @return the maximum utility achieveable from this state.
   */
  private double max_Q(Board state) throws Error {
    Position[][] moves = validMoves(state);
    if (moves.length == 0) throw new Error("No moves reachable from the current board position.");
    Position[] bestAction = new Position[2];
    double bestUtility = Double.MIN_VALUE;
    for (Position[] action : moves) {
      SAPair currSA = new SAPair(state, action);
      if (Qvalues.getOrDefault(currSA, 0.0) > bestUtility) {
        bestAction = action;
        bestUtility = Qvalues.getOrDefault(currSA, 0.0);
      }
    }
    return bestUtility;
  }

  /**
   * The Q-Learning storage update function. This function is called at the start of move to update utilities and visit counts
   * for various state-action pairs. The implicit arguments to this function exist in the class. The default utility for
   * state-action pairs that have not yet been visited is 0.
   */
  private void Q_Learning_Update() {
    if (currentState.gameOver()) Qvalues.put(new SAPair(currentState, null), (double) currentReward);
    if (!previousState.equals(null)) { // previousState is null if no states have been visited before
      SAPair currSA = new SAPair(previousState, previousAction);
      N_sa.put(currSA, N_sa.get(currSA).equals(null) ? 1 : N_sa.get(currSA) + 1);
      double c = η(N_sa.get(currSA));
      double currQ = Qvalues.getOrDefault(currSA, 0.0);
      Qvalues.put(currSA, (1 - c) * currQ + c * (previousReward + γ * max_Q(currentState)));
    }
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
