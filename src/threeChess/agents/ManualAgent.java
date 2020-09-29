package threeChess.agents;

import java.util.*;
import threeChess.*;

/**
 * An interface for AI bots to implement.
 * They are simply given a Board object indicating the positions of all pieces, 
 * the history of the game and whose turn it is, and they respond with a move, 
 * expressed as a pair of positions.
 * **/ 
public class ManualAgent extends Agent{
  
  private String name = "Manual Agent";


  /**
   * A no argument constructor, 
   * required for tournament management.
   * **/
  public ManualAgent(){
  }

  public ManualAgent(String name){
    this.name = name;
    System.out.println(name+" is a manually controlled agent.\n To make a move enter the satring position followed by a spec and then the end position of your move.\n For example,\nBD2 BD4\n will specify the blue pawn in front of the queen should move 2 squares forward.");
  }

  @Override
  public boolean isAutonomous() {
    return false;
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
    System.out.println(board.getTurn()+"'s move:");
    Scanner sc = new Scanner(System.in);
    while(true){
      try{
        String moveString = sc.nextLine();
        String[] pos = moveString.split("\\s");
        Position start = Position.valueOf(pos[0]);
        Position end = Position.valueOf(pos[1]);
        if(board.isLegalMove(start, end)) return new Position[]{start,end};
        else System.out.println("Illegal move, try again.");
      }
      catch(IllegalArgumentException e){
        System.out.println("Invalid move format. Use style \"BA1 RA1\".");
      }
    } 
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


