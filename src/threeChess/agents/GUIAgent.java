package threeChess.agents;

import threeChess.Agent;
import threeChess.Board;
import threeChess.Position;
import threeChess.ThreeChessDisplay;

/**
 * An agent that allows the user to select its moves through the display.
 */
public class GUIAgent extends Agent {

  public static ThreeChessDisplay currentDisplay = null;
  private final String name;

  /** A no argument constructor, required for tournament management. **/
  public GUIAgent() {
    this("GUI Agent");
  }

  public GUIAgent(String name) {
    this.name = name;
    System.out.println(
        name + " is a manually controlled agent.\n"
        + " To make a move click the piece you want to move in the display, "
        + "and then click the square where you wish to move it to."
    );
  }

  @Override
  public boolean isAutonomous() {
    return false;
  }

  /** Gets and returns a move from the display for this agent. **/
  public Position[] playMove(Board board){
    try {
      return currentDisplay.askForMove().get();
    } catch (InterruptedException e) {
      throw new RuntimeException("Exception waiting for user to enter move on display", e);
    }
  }

  /** @return the Agent's name, for annotating game description. **/
  public String toString(){return name;}

  /** Unused for the GUI agent. **/
  public void finalBoard(Board finalBoard){}
}
