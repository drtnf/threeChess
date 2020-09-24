package threeChess;

/**
 * Enumeration of the rectilinear directions
 * used as the basis for all moves.
 **/
public enum Direction {
  FORWARD,
  BACKWARD,
  LEFT,
  RIGHT;

  public Direction reverse() {
    switch (this) {
      case FORWARD: return BACKWARD;
      case BACKWARD: return FORWARD;
      case LEFT: return RIGHT;
      case RIGHT: return LEFT;
      default: throw new IllegalStateException("Unsupported direction " + this);
    }
  }

  /**
   * @return a copy of {@param step} with all the directions reversed.
   */
  public static Direction[] reverse(Direction[] step) {
    Direction[] reversed = new Direction[step.length];
    for (int index = 0; index < step.length; ++index) {
      reversed[index] = step[index].reverse();
    }
    return reversed;
  }
}

