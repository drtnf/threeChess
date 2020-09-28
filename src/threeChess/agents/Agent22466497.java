package threeChess.agents;

import threeChess.Agent;
import threeChess.Board;
import threeChess.Position;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a state-action pair.
 */
class SAPair implements Serializable {
  private static final long serialVersionUID = 7753905743550444452L;
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
  private final String QPersistance = "QPersistance";
  private final String NPersistance = "NPersistance";

  // Q-Learning Parameters
  Board s; int r; Position[] a; // Previous state-action-reward "tuple"
  Board currentState; int currentReward;
  double γ; // Discount Factor

  // Q-Learning Storage
  HashMap<SAPair, Double> Qvalues; // The table of Q-values, i.e. the table of state-action pair utilities.
  HashMap<SAPair, Integer> N_sa; // A 2D table, where <s, a>  keeps track of the number of times action a was performed while in state s.

  // Constructor
  public Agent22466497() {
    s = null; r = 0; a = new Position[2];
    currentState = null; currentReward = 0;
    γ = 0.95;
    Qvalues = new HashMap<>();
    N_sa = new HashMap<>();

    // Load storage objects from file, if they exist
    try {
      FileInputStream q_in = new FileInputStream(QPersistance);
      ObjectInputStream q_obj = new ObjectInputStream(q_in);
      Qvalues = (HashMap<SAPair, Double>) q_obj.readObject();
      q_obj.close(); q_in.close();

      FileInputStream N_in = new FileInputStream(NPersistance);


    } catch (Exception e) {
      System.out.println(e);
      System.out.println("Persistance files cannot be located. Beginning with empty storage...");
    }

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
    if (s != null) { // s is null if no states have been visited before
      SAPair currSA = new SAPair(s, a);
      N_sa.put(currSA, N_sa.get(currSA) == null ? 1 : N_sa.get(currSA) + 1);
      double c = η(N_sa.get(currSA));
      double currQ = Qvalues.getOrDefault(currSA, 0.0);
      Qvalues.put(currSA, (1 - c) * currQ + c * (r + γ * max_Q(currentState)));
    }
  }

  /**
   * The f-function is responsible for artificially inflating the utility values
   * of state-action pairs that have not yet been visited enough.
   * The function encourages exploration of new state-action pairs.
   * @param u the utility of a state-action pair.
   * @param n the number of times that state-action pair has been visited.
   * @return a possibly inflated utility value.
   */
  private double f(double u, int n) { return (n < 10 ? Double.MAX_VALUE : u); }

  /**
   * The board object tracks the number of moves (getMoveCount) and a list of all the moves so far (getMove).
   * We can define a reward function for our agent's previous move (not just the last move, as that was not made by our agent).
   * The reward function takes into account not just the state of the board directly after our agent has played,
   * but also after the other 2 agents have played as well. Thus, the "previous" state is the state directly before the last move
   * played by our agent.
   * @param current
   * @return
   */
  private int reward(Board current) {
    int numMoves = current.getMoveCount();
    if (numMoves <= 2) return 0; // Our agent hasn't moved yet.
    // Reconstruct the board from the start to the last position
    Board previous = new Board(500);
    for (int i = 0; i < numMoves - 2; i++) { // The moves are indexed from 1
      Position[] move = current.getMove(i);
      try {
        previous.move(move[0], move[1]);
      } catch (Exception e) { System.out.println("Reached a block that should not be reached: " + e); } // We should NEVER end up here.
    }
    // Here we have 2 boards, previous and current.
    // We generate the reward value for a board position.
    int diffPieces = current.getPositions(current.getTurn()).size() - previous.getPositions(current.getTurn()).size();
    int diffCaptures = current.getCaptured(current.getTurn()).size() - previous.getCaptured(current.getTurn()).size();

    return (diffPieces + diffCaptures);
  }

  /**
   * Writes the Q-storage objects to file.
   * @return true on successful write, and false if otherwise.
   */
  private boolean writeStorage() {
    boolean success = true;
    try {
      FileOutputStream q_out = new FileOutputStream(QPersistance);
      ObjectOutputStream q_obj = new ObjectOutputStream(q_out);
      q_obj.writeObject(Qvalues);
      q_obj.close(); q_out.close();

      FileOutputStream N_out = new FileOutputStream(NPersistance);
      ObjectOutputStream N_obj = new ObjectOutputStream(N_out);
      N_obj.writeObject(N_sa);
      N_obj.close(); N_out.close();

    } catch (Exception e) {
      System.out.println(e);
      success = false;
    }

    return success;
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
    currentState = board; currentReward = reward(board);
    Q_Learning_Update();
    if (board.gameOver()) return null;

    // Get the best possible action with the highest f-utility
    Position[] bestAction = new Position[2]; double bestUtility = Double.MIN_VALUE;
    Position[][] actions = validMoves(board);
    for (Position[] action : actions) {
      SAPair currPair = new SAPair(board, action);
      double currentUtility = f(Qvalues.getOrDefault(currPair, 0.0), N_sa.getOrDefault(currPair, 0));
      if (currentUtility > bestUtility) {
        bestUtility = currentUtility;
        bestAction = action;
      }
    }
    s = currentState; r = currentReward;
    writeStorage();
    return bestAction;

  }

  public String toString() {return name;}

  public void finalBoard(Board finalBoard) {}
  
}
