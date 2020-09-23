package threeChess;

/**
 * Enumeration of the different piece types.
 * Values are included for scoring purposes,
 * using the typical values, but 40 for the King.
 * **/
public enum PieceType{
  PAWN(1),
  KNIGHT(3),
  BISHOP(3),
  ROOK(5),
  QUEEN(9),
  KING(40);//worth one more than all the other pieces combined.

  /**the utility associated with taking this piece**/
  private final int value;
  //return arrays of moves for pieceTypes
  private static Direction[][] pawnSteps(){
    return new Direction[][] {{Direction.FORWARD},{Direction.FORWARD,Direction.FORWARD},
    {Direction.FORWARD,Direction.LEFT},{Direction.LEFT,Direction.FORWARD},{Direction.FORWARD,Direction.RIGHT},
    {Direction.RIGHT,Direction.FORWARD}};
  }

  private static Direction[][] knightSteps(){
    return new Direction[][] {{Direction.FORWARD,Direction.FORWARD,Direction.LEFT},
    {Direction.FORWARD,Direction.FORWARD,Direction.RIGHT},{Direction.FORWARD,Direction.LEFT,Direction.LEFT},
    {Direction.FORWARD,Direction.RIGHT,Direction.RIGHT},{Direction.BACKWARD,Direction.BACKWARD,Direction.LEFT},
    {Direction.BACKWARD,Direction.BACKWARD,Direction.RIGHT},{Direction.BACKWARD,Direction.LEFT,Direction.LEFT},
    {Direction.BACKWARD,Direction.RIGHT,Direction.RIGHT},{Direction.LEFT,Direction.LEFT,Direction.FORWARD},
    {Direction.LEFT,Direction.LEFT,Direction.BACKWARD},{Direction.LEFT,Direction.FORWARD,Direction.FORWARD},
    {Direction.LEFT,Direction.BACKWARD,Direction.BACKWARD},{Direction.RIGHT,Direction.RIGHT,Direction.FORWARD},
    {Direction.RIGHT,Direction.RIGHT,Direction.BACKWARD},{Direction.RIGHT,Direction.FORWARD,Direction.FORWARD},
    {Direction.RIGHT,Direction.BACKWARD,Direction.BACKWARD}};
  }

  private static Direction[][] bishopSteps(){
    return new Direction[][] {{Direction.FORWARD,Direction.LEFT},{Direction.FORWARD,Direction.RIGHT},
    {Direction.LEFT,Direction.FORWARD},{Direction.RIGHT,Direction.FORWARD},{Direction.BACKWARD,Direction.LEFT},
    {Direction.BACKWARD,Direction.RIGHT},{Direction.LEFT,Direction.BACKWARD},{Direction.RIGHT,Direction.BACKWARD}};
  }

  private static Direction[][] rookSteps(){
    return new Direction[][] {{Direction.FORWARD},{Direction.BACKWARD},{Direction.LEFT},{Direction.RIGHT}};
  }

  private static Direction[][] kingSteps(){
   return new Direction[][] {{Direction.FORWARD,Direction.LEFT},{Direction.FORWARD,Direction.RIGHT},
    {Direction.LEFT,Direction.FORWARD},{Direction.RIGHT,Direction.FORWARD},{Direction.BACKWARD,Direction.LEFT},
    {Direction.BACKWARD,Direction.RIGHT},{Direction.LEFT,Direction.BACKWARD},{Direction.RIGHT,Direction.BACKWARD},
    {Direction.FORWARD},{Direction.BACKWARD},{Direction.LEFT},{Direction.RIGHT}}; //kings and queens
  }

  /**Sets the value of the piece**/
  private PieceType(int value){
    this.value = value;
  }

  /**@return the value of the piece**/
  public int getValue(){
    return value;
  }
  
  /**
   * Returns the array of steps that can make legitimate moves.
   * Rooks, Bishops and Queens may iterate one step type in a move.
   * All other pieces may only make one step per move.
   * @return an array or arrays of directions where each inner array is a legitimate step.
   * **/
  public Direction[][] getSteps(){
    switch(this){
    case PAWN: return pawnSteps();
    case KNIGHT: return knightSteps();
    case BISHOP: return bishopSteps();
    case ROOK: return rookSteps();
    default: return kingSteps();//Kings and queens have the same steps, but queens may repeat the one step.
    }
  }

  /**
   * Returns the number of repetitions of a step are allowed.
   * Rooks, Bishops and Queens may iterate one step type in a move.
   * All other pieces may only make one step per move.
   * @return the number of repetitions allowed.
   */
  public int getStepReps() {
    switch(this){
      case ROOK:
      case QUEEN:
      case BISHOP:
        return 8;
      default:
        // Kings, pawns, and knights cannot repeat their moves.
        return 1;
    }
  }

  /**
   * Returns the unicode character corresponding to a 
   * black version of this piece.
   * **/
  public char getChar(){
    switch(this){
      case KING: return '\u265A';
      case QUEEN: return '\u265B';
      case ROOK: return '\u265C'; 
      case BISHOP: return '\u265D';
      case KNIGHT: return '\u265E';
      case PAWN: return '\u265F';
      default: return '?';           
    }
  }
}

