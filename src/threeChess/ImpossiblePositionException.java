package threeChess;

/**
 * Exception class for reacting to impossible positions
 * **/
public class ImpossiblePositionException extends Exception{
  public ImpossiblePositionException(String msg){
    super("Impossible Position: "+msg);
  }
}


