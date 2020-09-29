package threeChess;

import java.io.Serializable;

/**
 * A specific piece on the board. 
 * Each piece has a Colour and a Type,
 * and is immutable. 
 * Not an enum so we can have identical, but non-equal pieces, such as pawns. 
 * **/
public class Piece implements Serializable {
  private static final long serialVersionUID = 8757415399259946465L; // Serial version UID for serialization and storage
  private final PieceType type;// the piece's type
  private final Colour colour;//the pieces colour

  /**
   * Constructs a piece of the given type and colour.
   * @param type the type of the piece
   * @param colour the colour of the piece
   * **/ 
  public Piece(PieceType type, Colour colour){
    this.type = type; this.colour = colour;
  }

  /**@return the type of the piece**/
  public PieceType getType(){return type;}

  /**@return the value of the piece**/
  public int getValue(){return type.getValue();}
  
  /**@return the colour of the piece**/
  public Colour getColour(){return colour;}

  /**@return a String representation of the piece**/
  public String toString(){
    return colour.toString()+" "+type.toString();
  }
}

