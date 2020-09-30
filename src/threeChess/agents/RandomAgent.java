package threeChess.agents;

import threeChess.*;

import java.util.Random;

/**
 * An interface for AI bots to implement.
 * They are simply given a Board object indicating the positions of all pieces, 
 * the history of the game and whose turn it is, and they respond with a move, 
 * expressed as a pair of positions.
 * **/ 
public class RandomAgent extends Agent{
  
  private static final String name = "Random";
  private static final Random random = new Random();


  /**
   * A no argument constructor, 
   * required for tournament management.
   * **/
  public RandomAgent(){
  }

  /**
   * Play a move in the game. 
   * The agent is given a Board Object representing the position of all pieces, 
   * the history of the game and whose turn it is. 
   * They respond with a move represented by a pair (two element array) of positions: 
   * the start and the end position of the move.
   * @param board The representation of the game state.
   * @return a two element array of Position objects, where the first element is the 
   * current position of the piece to be moved, and the second element is the 
   * position to move that piece to.
   * **/
  public Position[] playMove(Board board){
    Position[] pieces = board.getPositions(board.getTurn()).toArray(new Position[0]);
    Position start = pieces[0];
    Position end = pieces[0]; //dummy illegal move
    while (!board.isLegalMove(start, end)){
      start = pieces[random.nextInt(pieces.length)];
      Piece mover = board.getPiece(start);
      Direction[][] steps = mover.getType().getSteps();
      Direction[] step = steps[random.nextInt(steps.length)];
      int reps = 1 + random.nextInt(mover.getType().getStepReps());
      end = start;
      try{
        for(int i = 0; i<reps; i++)
          end = board.step(mover, step, end, start.getColour()!=end.getColour());
      }catch(ImpossiblePositionException e){}
    }
    return new Position[] {start,end};
  }

  /**
   * @return the Agent's name, for annotating game description.
   * **/ 
  public String toString(){return name;}

  /**
   * Displays the final board position to the agent, 
   * if required for learning purposes. 
   * Other a default implementation may be given.
   * @param finalBoard the end position of the board
   * **/
  public void finalBoard(Board finalBoard){}

}


