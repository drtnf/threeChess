package threeChess;

import java.io.Serializable;
import java.util.*;

/**
 * Main class for representing game state.
 * The board maps each position to the piece at that posiiton, 
 * or null is free. It also records previous moves, 
 * as well as whose move it is, and which pieces have been 
 * captured by which player.
 * **/
public class Board implements Cloneable, Serializable {
  
  /** Serial version UID for Board serialization and storage**/
  private static final long serialVersionUID = -8547775276050612530L;
  /** A map from board positions to the pieces at that position **/
  private HashMap<Position,Piece> board;
  /**A flag that is true if and only if a King has been captured**/
  private boolean gameOver = false;
  /**The player whose turn it is**/
  private Colour turn = Colour.BLUE;//Blue goes first
  /**The moves taken so far, represented as an array of two positions, the start and end of the move**/
  private ArrayList<Position[]> history;//can only be changed by taking moves
  /**A map indicating which player has taken which piece, to support alternative scoring methods**/
  private HashMap<Colour,ArrayList<Piece>> captured;
  /**A Map representing the remaining time allowed for each player, in milliseconds**/
  private HashMap<Colour,Integer> timeLeft;

  /**
   * Initialises the board, placing all pieces at their initial position.
   * Note, unlike two person chess, the Queen is always on the left, and the King is always on his own colour.
   * @param time the number of milliseconds each player has in total for the entire game.
   * **/
  public Board(int time){
    board = new HashMap<Position,Piece>();
    try{
      for(Colour c: Colour.values()){
        board.put(Position.get(c,0,0),new Piece(PieceType.ROOK,c)); board.put(Position.get(c,0,7), new Piece(PieceType.ROOK,c));
        board.put(Position.get(c,0,1),new Piece(PieceType.KNIGHT,c)); board.put(Position.get(c,0,6), new Piece(PieceType.KNIGHT,c));
        board.put(Position.get(c,0,2),new Piece(PieceType.BISHOP,c)); board.put(Position.get(c,0,5), new Piece(PieceType.BISHOP,c));
        board.put(Position.get(c,0,3),new Piece(PieceType.QUEEN,c)); board.put(Position.get(c,0,4), new Piece(PieceType.KING,c));
        for(int i = 0; i<8; i++){
          board.put(Position.get(c,1,i), new Piece(PieceType.PAWN,c));
        }
      }
    }catch(ImpossiblePositionException e){}//no impossible positions in this code
    history = new ArrayList<Position[]>();
    captured = new HashMap<Colour,ArrayList<Piece>>();
    timeLeft = new HashMap<Colour,Integer>();
    for(Colour c: Colour.values()){
      captured.put(c,new ArrayList<>());
      timeLeft.put(c,time);
    }
  }

  /** @return whether in manual mode, the legal moves should be displayed on the board. **/
  public boolean displayLegalMoves() {
    return true;
  }

  /**
   * Return a set of all the positions of pieces belonging to a player.
   * This is a method of convenience, but is not very fast. 
   * Time concious players may prefer to maintain their own data structure that persists between moves.
   * @param player the Colour of the player owing the pieces
   * @return a Set of the positions that are occupied by a piece of the given colour.
   * **/
  public Set<Position> getPositions(Colour player){
    HashSet<Position> positions = new HashSet<Position>();
    for(Position p : Position.values()){
      if(board.containsKey(p) && board.get(p).getColour()==player)
        positions.add(p);
    }
    return positions;
  }

  /** @return a set of all the pieces captured by {@param player}. **/
  public List<Piece> getCaptured(Colour player) {
    return new ArrayList<>(captured.get(player));
  }

  /**
   * Gets the piece at a specified position.
   * @param position the position of the piece,
   * @return the piece at that position or null, if the position is vacant.
   * **/
  public Piece getPiece(Position position){
    return board.get(position);
  }
  
  /**
   * Performs one step of a move such as the L shaped move of a knight, or a diagonal step of a Bishop.
   * Rooks, Bishops and Queens may iterate one step repeatedly, but all other pieces can only move one step per move.
   * Note the colour of the piece is relevant as moving forward past the 4th row is actually moving backwards relative to the board.
   * It does not check whether the move is legal or possible. 
   * @param piece the piece being moved
   * @param step an array of the direction sequence in the step
   * @param current the starting position of the step.
   * @return the position at the end of the step.
   * @throws ImpossiblePositionException if the step takes piece off the board.
   * **/
  public Position step(Piece piece, Direction[] step, Position current) throws ImpossiblePositionException{
    boolean reverse = false;
    for(Direction d: step){
      if((piece.getColour()!=current.getColour() && piece.getType() == PieceType.PAWN) || reverse){//reverse directions for knights
        switch(d){
          case FORWARD: d = Direction.BACKWARD; break;
          case BACKWARD: d = Direction.FORWARD; break;
          case LEFT: d = Direction.RIGHT; break;
          case RIGHT: d = Direction.LEFT; break;
        }
      }
      Position next = current.neighbour(d);
      if(next.getColour()!= current.getColour()){//need to reverse directions when switching between sections of the board
        reverse=true;
      }
      current = next;
    }
    return current;
  }

  /**
   * Performs one step of a move such as the L shaped move of a knight, or a diagonal step of a Bishop.
   * Rooks, Bishops and Queens may iterate one step repeatedly, but all other pieces can only move one step per move.
   * Note the colour of the piece is relevant as moving forward past the 4th row is actually moving backwards relative to the board.
   * It does not check whether the move is legal or possible.
   * Overloaded operation with an extra parameter to allow for checking if an iterated move needs to be reversed. 
   * @param piece the piece being moved
   * @param step an array of the direction sequence in the step
   * @param current the starting position of the step.
   * @param reverse whether the steps out to be reversed (if the piece crosses board section).
   * @return the position at the end of the step.
   * @throws ImpossiblePositionException if the step takes piece off the board.
   * **/
  public Position step(Piece piece, Direction[] step, Position current, boolean reverse) throws ImpossiblePositionException{
    for(Direction d: step){
      if((piece.getColour()!=current.getColour() && piece.getType() == PieceType.PAWN) || reverse){//reverse directions for knights
        switch(d){
          case FORWARD: d = Direction.BACKWARD; break;
          case BACKWARD: d = Direction.FORWARD; break;
          case LEFT: d = Direction.RIGHT; break;
          case RIGHT: d = Direction.LEFT; break;
        }
      }
      Position next = current.neighbour(d);
      if(next.getColour()!= current.getColour()){//need to reverse directions when switching between sections of the board
        reverse=true;
      }
      current = next;
    }
    return current;
  }
  
  /**
   * Checks if a move is legal. 
   * The move is specified by the start position (where the moving piece begins),
   * and the end position, where the piece intends to move to.
   * The conditions checked are: 
   * there is a piece at the start position; 
   * the colour of that piece correspond to the player whose turn it is;
   * if there is a piece at the end position, it cannot be the same as the moving piece;
   * the moving piece must be executing one or more steps allowed for their type, including
   * two steps forward for initial pawn moves and castling left and right;
   * pieces that can make iterated moves must iterate a single step type and cannot pass through any other piece.
   * Note, en passant is not allowed, you can castle after King or rook have moved 
   * but they must have returned to their initial position, all pawns reaching the back row are promoted to Queen,
   * you may move into check, and you may leave your king in check, and you may castle across check.
   * @param start the starting position of the piece
   * @param end the end position the piece intends to move to
   * @return true if and only if the move is legal in the rules of the game.
   * **/
  public boolean isLegalMove(Position start, Position end){
    Piece mover = getPiece(start);
    Piece target = getPiece(end);
    if(mover==null) return false;//you must move a piece
    Colour mCol =mover.getColour();
    if(mCol!=turn) return false;//it must be your turn
    if(target!= null && mCol==target.getColour())return false; //you can't take your own piece
    Direction[][] steps = mover.getType().getSteps();
    switch(mover.getType()){
      case PAWN://note, there is no two step first move
        for(int i = 0; i<steps.length; i++){
          try{
            if(end == step(mover,steps[i],start) && 
                ((target==null && i==0) // 1 step forward, not taking
                 || (target==null && i==1 // 2 steps forward, 
                   && start.getColour()==mCol && start.getRow()==1 //must be in initial position
                   && board.get(Position.get(mCol,2,start.getColumn()))==null)//and can't jump a piece 
                 || (target!=null && i>1)//or taking diagonally
                )
              )
              return true;
          }catch(ImpossiblePositionException e){}//do nothing, steps went off board.
        }
        break;
      case KNIGHT:
        for(int i = 0; i<steps.length; i++){
          try{
            if(end == step(mover, steps[i],start))
              return true;
          }catch(ImpossiblePositionException e){}//do nothing, steps went off board.
        }
        break;
      case KING://note, you can move into check or remain in check. You may also castle across check
        for(int i = 0; i<steps.length; i++){
          try{
            if(end == step(mover, steps[i],start))
              return true;
          }catch(ImpossiblePositionException e){}//do nothing, steps went off board.
        }
        //castling: Must have king and rook in their original positions, although they may have moved
        try{
          if(start==Position.get(mCol,0,4)){
            if(end==Position.get(mCol,0,6)){
              Piece castle = board.get(Position.get(mCol,0,7));
              Piece empty1 = board.get(Position.get(mCol,0,5));
              Piece empty2 = board.get(Position.get(mCol,0,6));
              if(castle!=null && castle.getType()==PieceType.ROOK && castle.getColour()==mover.getColour()
                  && empty1==null && empty2==null)
                return true;
            }
            if(end==Position.get(mCol,0,2)){
              Piece castle = board.get(Position.get(mCol,0,0));
              Piece empty1 = board.get(Position.get(mCol,0,1));
              Piece empty2 = board.get(Position.get(mCol,0,2));
              Piece empty3 = board.get(Position.get(mCol,0,3));
              if(castle!=null && castle.getType()==PieceType.ROOK && castle.getColour()==mover.getColour()
                  && empty1==null && empty2==null && empty3==null)
                return true;
            }
          }
        }catch(ImpossiblePositionException e){}//do nothing, all positions possible here.
        break;
      default://rook, bishop, queen, just need to check that one of their steps is iterated.
        for(int i = 0; i<steps.length; i++){
          Direction[] step = steps[i];
          try{
            Position tmp = step(mover,step,start);
            while(end != tmp && board.get(tmp)==null){
              tmp = step(mover, step, tmp, tmp.getColour()!=start.getColour());
            }
            if(end==tmp) return true;
          }catch(ImpossiblePositionException e){}//do nothing, steps went off board.
        }
        break;
    }
    return false;//move did not match any legal option.
  }

  /**
   * Executes a legal move. 
   * If a piece is taken it is replaced at that position by the taking piece.
   * The game ends when a King is taken. 
   * When a pawn reaches the back rank it is automatically promoted to Queen.
   * @param start the starting position of the move
   * @param end the ending position of the move
   * @param time the number of milliseconds taken to play the move
   * @throws ImpossiblePositionException if the move is not legal
   * **/ 
  public void move(Position start, Position end, int time) throws ImpossiblePositionException{
    if(isLegalMove(start,end)){
      Piece mover = board.get(start);
      Piece taken = board.get(end);
      timeLeft.put(mover.getColour(),timeLeft.get(mover.getColour())-time);
      if(timeLeft.get(mover.getColour())<0) gameOver=true;
      else{
        board.remove(start);//empty start square
        if(mover.getType()==PieceType.PAWN && end.getRow()==0 && end.getColour()!=mover.getColour())
          board.put(end, new Piece(PieceType.QUEEN, mover.getColour()));//promote pawn if back rank
        else board.put(end,mover);//move piece
        if(mover.getType()==PieceType.KING && start.getColumn()==4 && start.getRow()==0){
          if(end.getColumn()==2){//castle left, update rook
            Position rookPos = Position.get(mover.getColour(),0,0);
            board.put(Position.get(mover.getColour(),0,3),board.get(rookPos));
            board.remove(rookPos);
          }else if(end.getColumn()==6){//castle right, update rook
            Position rookPos = Position.get(mover.getColour(),0,7);
            board.put(Position.get(mover.getColour(),0,5),board.get(rookPos));
            board.remove(rookPos);
         }
        }
        history.add(new Position[]{start,end});
        if(taken !=null){
          captured.get(mover.getColour()).add(taken);
          if(taken.getType()==PieceType.KING) gameOver=true;
        }
        turn = Colour.values()[(turn.ordinal()+1)%3];
      }
    }
    else throw new ImpossiblePositionException("Illegal Move: "+start+"-"+end);
  }

  /**
   * Executes a legal move. 
   * If a piece is taken it is replaced at that position by the taking piece.
   * The game ends when a King is taken. 
   * When a pawn reaches the back rank it is automatically promoted to Queen.
   * Method overloaded to allow for untimed games.
   * @param start the starting position of the move
   * @param end the ending position of the move
   * @throws ImpossiblePositionException if the move is not legal
   * **/ 
  public void move(Position start, Position end) throws ImpossiblePositionException{
    move(start,end,0);
  }
  
  /**
   * Gets the player whose turn it currently is
   * @return the colour of the player whose turn it is.
   * **/
  public Colour getTurn(){
    return turn;
  }

  /**
   * Returns the number of moves made so far.
   * @return the number of moves made in the game.
   * **/
  public int getMoveCount(){
    return history.size();
  }

  /**
   * returns the move made at the corresponding index (starting from 1).
   * @param index the index of the move
   * @return an array containing the start position and the end position of the move, in that order
   * @throws ArrayIndexOutOfBoundsException if the index does not correspond to a move.
   * **/
  public Position[] getMove(int index){
    if(0<=index && index<getMoveCount()){
      return history.get(index).clone();
    }
    else throw new ArrayIndexOutOfBoundsException("Index out of bounds.");
  }

  /**
   * Calculates a players score, used for some variants of the game.
   * The score is the combined piece values of the players pieces on the board,
   * plus the value of the pieces taken by that player.
   * This is a convenience method which gives a basic utility value.
   * It can be used to encourage more aggressive play in agents, 
   * but the traditional scoring is +1 for taking a King, and -1 for losing a King, 
   * @param player the colour of the player
   * @return the score of the player.
   * **/
  public int score(Colour player){
    int score = 0;
    for(Position p: Position.values()){
      Piece piece = board.get(p);
      if(piece!=null && piece.getColour()==player) score+=piece.getValue();
    }
    for(Piece piece: captured.get(player)) score+=piece.getValue();  
    return score;
  }

  /** 
   * @return true if the game has ended
   * **/
  public boolean gameOver(){
    return gameOver;
  }

  /**
   * The winner of the game is the player who takes another player's King,
   * or the player with the highest score when another player runs out of time.
   * @return the winner of the game or null if it's a draw or not yet decided.
   * **/
  public Colour getWinner(){
    if(gameOver){
      for(Colour c: Colour.values()){
        for(Piece taken: captured.get(c)){
          if(taken.getType()==PieceType.KING) return c;
        }
        if(timeLeft.get(c)<0){
          Colour winner = null; int max = Integer.MIN_VALUE;
          for(Colour d: Colour.values()){
            int score = score(d);
            if(d!=c && score>max){
              winner = d; max = score;
            }
          }
          return winner;
        }
      }
    }
    return null;
  }

  /**
   * The loser of the game is the player who had their King taken,
   * or the player who ran out of time.
   * @return the loser of the game or null if its a draw or not yet decided.
   * **/
  public Colour getLoser(){
    if(gameOver){
      for(Colour c: Colour.values()){
        for(Piece taken: captured.get(c)){
          if(taken.getType()==PieceType.KING) return taken.getColour();
        }
        if(timeLeft.get(c)<0) return c;
      }
    }
    return null;
  }

  /**
   * Get the time left for the specified player.
   * @return the time remaining, in milliseconds.
   * **/
  public int getTimeLeft(Colour colour){
    return timeLeft.get(colour);
  }

  /**
   * Return a copy of map representing the current board state
   *
   * @return The copy of the board position/piece state map
   */
  public HashMap<Position, Piece> getPositionPieceMap() {
    return new HashMap<>(board);
  }

  /**
   * Returns a deep clone of the board state, 
   * such that no operations will affect the original board instance.
   * @return a deep clone of the board state.
   * **/ 
  public Object clone() throws CloneNotSupportedException{
    Board clone = (Board) super.clone();
    clone.board = (HashMap<Position,Piece>)board.clone();
    clone.history = new ArrayList<Position[]>();
    for(Position[] move: history) clone.history.add(move.clone());
    clone.timeLeft = (HashMap<Colour,Integer>) timeLeft.clone();
    clone.captured = new HashMap<Colour,ArrayList<Piece>>();
    for(Colour c: Colour.values()) clone.captured.put(c, (ArrayList<Piece>) captured.get(c).clone());
    return clone;
  }
}
