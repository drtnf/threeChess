package threeChess;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import javax.swing.*;

public class ThreeChessDisplay extends JFrame {

  private static final Color DARKRED = new Color(127,0,0);
  private static final Color RED = new Color(255,102,102);
  private static final Color LIGHTRED = new Color(255,204,204);
  private static final Color DARKGREEN = new Color(0,127,0);
  private static final Color GREEN = new Color(102,255,102);
  private static final Color LIGHTGREEN = new Color(204,255,204);
  private static final Color DARKBLUE = new Color(0,0,127);
  private static final Color BLUE = new Color(102,102,255);
  private static final Color LIGHTBLUE = new Color(204,204,255);
  private static final int LABELS_FONTSIZE = 16;
  private static final int AGENTS_FONTSIZE = 24;
  private static final int PIECE_FONTSIZE = 32;
  private static final int AGENT_NAME_MAX_LENGTH = 20;
  private final Square[] squares;
  private final String[] players;
  private final Canvas canvas;
  private final Board board;
  private final int size = 800;
  private static int[][][] flanks;
  
  //To represent each square of the board.
  private static class Square{
    int[] xs;//4 x-coords
    int[] ys;//4 y-coords
    Piece piece; //piece in the square or null if empty
    boolean coloured;// for white or not
    Colour colour;

    /**
     * Constructs a square corresponding to the position given.
     * This calculates the cartesian coordinates of the corners of the position, 
     * and records the squares colour and parity
     * **/
    public Square(Position pos){
      colour = pos.getColour();
      coloured = pos.evenParity();
      //calculate coords for poly point and center
      int r = pos.getRow();
      int c = pos.getColumn();
      xs = new int[4]; ys = new int[4]; //lower left, lower right, upper right, upper left.
      int[] left = flanks[colour.ordinal()][(c<4?0:1)];//coords of left margin
      int[] right = flanks[colour.ordinal()][(c<4?1:2)];//coords of right margin.
      double[] baseLine = new double[4];
      double[] topLine = new double[4];
      baseLine[0] = left[0]+((left[2]-left[0])/4.0d)*r;
      baseLine[1] = left[1]+((left[3]-left[1])/4.0d)*r;
      baseLine[2] = right[0]+((right[2]-right[0])/4.0d)*r;
      baseLine[3] = right[1]+((right[3]-right[1])/4.0d)*r;
      topLine[0] = left[0]+((left[2]-left[0])/4.0d)*(r+1);
      topLine[1] = left[1]+((left[3]-left[1])/4.0d)*(r+1);
      topLine[2] = right[0]+((right[2]-right[0])/4.0d)*(r+1);
      topLine[3] = right[1]+((right[3]-right[1])/4.0d)*(r+1);
      //bottom left
      xs[0] = (int) (baseLine[0]+((baseLine[2]-baseLine[0])/4.0d)*(c%4));
      ys[0] = (int) (baseLine[1]+((baseLine[3]-baseLine[1])/4.0d)*(c%4));
      //bottom right
      xs[1] = (int) (baseLine[0]+((baseLine[2]-baseLine[0])/4.0d)*(c%4+1));
      ys[1] = (int) (baseLine[1]+((baseLine[3]-baseLine[1])/4.0d)*(c%4+1));
      //top right
      xs[2] = (int) (topLine[0]+((topLine[2]-topLine[0])/4.0d)*(c%4+1));
      ys[2] = (int) (topLine[1]+((topLine[3]-topLine[1])/4.0d)*(c%4+1));
      //top left
      xs[3] = (int) (topLine[0]+((topLine[2]-topLine[0])/4.0d)*(c%4));
      ys[3] = (int) (topLine[1]+((topLine[3]-topLine[1])/4.0d)*(c%4));
    }

    // just returns the mean of all the coordinates
    private int[] getCentre(){
      int[] centre = new int[2];
      for(int i = 0; i<4; i++){
        centre[0]+=xs[i];
        centre[1]+=ys[i];
      }
      centre[0] = centre[0]/4 - PIECE_FONTSIZE/2;
      centre[1] = centre[1]/4 + PIECE_FONTSIZE*2/5;
      return centre;
    }

    /**Sets the piece of the square to the specified piece, or null if square is unoccupied**/
    public void setPiece(Piece piece){this.piece = piece;}

    //creates a colour for pieces and boards squares
    private Color getColour(Colour col, boolean piece, boolean coloured){
      switch(col){
        case RED: return piece? DARKRED: coloured? RED: LIGHTRED;
        case GREEN: return piece? DARKGREEN: coloured? GREEN: LIGHTGREEN;
        case BLUE: return piece? DARKBLUE: coloured? BLUE: LIGHTBLUE;
        default: return null;
      }
    }      

    /**
     * Renders the square with a black border, 
     * and a light colour if parity is even, 
     * and the piece in the board.
     * The graphics object should be updated after this method is called.
     * **/  
    public void draw(Graphics g){
      g.setColor(getColour(colour, false, coloured));
      g.fillPolygon(xs,ys,4);
      g.setColor(Color.BLACK);
      g.drawPolygon(xs, ys, 4);
      if(piece!=null){
        int[] pos = getCentre();
        g.setColor(getColour(piece.getColour(), true, true));
        g.drawString(""+piece.getType().getChar(),pos[0],pos[1]);
      }
    }


  }

  /**
   * Creates a graphical representation of a threeChess board
   * @param board the game state to be represented
   * @param bluePlayer the name of the blue player
   * @param greenPlayer the name of the green player
   * @param redPlayer the name of the red player
   * **/  
  public ThreeChessDisplay(Board board, String bluePlayer, String greenPlayer, String redPlayer){
    super("ThreeChess");
    this.board = board;

    canvas = new Canvas();
    setBounds(0, 0, size, size);
    setPreferredSize(new Dimension(size, size));
    add(canvas);
    canvas.setPreferredSize(new Dimension(size, size));
    canvas.setIgnoreRepaint(true);

    pack();
    setLocationRelativeTo(null);
    setResizable(false);// true seems to make it work in Ubuntu????
    setVisible(true);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    canvas.createBufferStrategy(2);

    setFlanks(size);
    players = new String[3];
    squares = new Square[96];
    for(int i=0; i<96; i++) squares[i] = new Square(Position.values()[i]);
    players[0] = truncateBelowLength(bluePlayer, AGENT_NAME_MAX_LENGTH);
    players[1] = truncateBelowLength(greenPlayer, AGENT_NAME_MAX_LENGTH);
    players[2] = truncateBelowLength(redPlayer, AGENT_NAME_MAX_LENGTH);
    repaintCanvas();
  }

  private Graphics2D getCanvasGraphics() {
    Graphics2D g = (Graphics2D) canvas.getBufferStrategy().getDrawGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    return g;
  }

  /**
   * delegate to the repaintCanvas method, when paint() is called.
   * @param g A graphics context for the Frame.
   * **/
  public void paint(Graphics g){
    if (canvas.getBufferStrategy() != null) {
      repaintCanvas();
    }
  }

  /** Repaints the board to the canvas. **/
  public void repaintCanvas(){
    Graphics2D g = getCanvasGraphics();
    try {
      drawToCanvas(g);
    }
    finally {
      g.dispose();
    }
    canvas.getBufferStrategy().show();
  }

  public void drawToCanvas(Graphics2D g) {
    g.setColor(Color.LIGHT_GRAY);
    g.fillRect(0, 0, getWidth(), getHeight());
    g.setStroke(new BasicStroke(3));

    g.setFont(new Font(g.getFont().getFontName(), Font.PLAIN, LABELS_FONTSIZE));
    int h_unit = size/20;
    int v_unit =(int) (Math.sqrt(3)*h_unit);
    for(int i=0; i<8; i++){
      String label = ""+((char)(65+i));
      g.setColor(DARKBLUE);
      g.drawString(label, (27-2*i)*h_unit/2,15*v_unit/8);
      g.setColor(DARKRED);
      g.drawString(label,(7+2*i)*h_unit/4,(13+i)*v_unit/2);
      g.setColor(DARKGREEN);
      g.drawString(label,(29+i)*h_unit/2,(20-i)*v_unit/2);
    }
    for(int i = 0; i<4; i++){
      g.setColor(DARKBLUE);
      g.drawString(""+(i+1), (21-2*i)*h_unit/4, (9+2*i)*v_unit/4);
      g.drawString(""+(i+1), (29+i)*h_unit/2, (9+2*i)*v_unit/4);
      g.setColor(DARKRED);
      g.drawString(""+(i+1),(7+2*i)*h_unit/4, (23-2*i)*v_unit/4);
      g.drawString(""+(i+1),(13+2*i)*h_unit/2,83*v_unit/8);
      g.setColor(DARKGREEN);
      g.drawString(""+(i+1),(27-2*i)*h_unit/2, 83*v_unit/8);
      g.drawString(""+(i+1),(36-i)*h_unit/2,(23-2*i)*v_unit/4);
    }

    g.setFont(new Font(g.getFont().getFontName(), Font.PLAIN, AGENTS_FONTSIZE));
    g.setColor(DARKBLUE);
    boolean blueActive = (board.getTurn() == Colour.BLUE);
    String blueText = players[0] + ": " + (board.getTimeLeft(Colour.BLUE) / 1000);
    drawAgentLabel(g, getWidth() / 2.0, v_unit, 0, blueText, blueActive);

    g.setColor(DARKGREEN);
    boolean greenActive = (board.getTurn() == Colour.GREEN);
    String greenText = players[1] + ": " + (board.getTimeLeft(Colour.GREEN) / 1000);
    drawAgentLabel(g, 17.5*h_unit, 8.5*v_unit, -Math.PI/3, greenText, greenActive);

    g.setColor(DARKRED);
    boolean redActive = (board.getTurn() == Colour.RED);
    String redText = players[2] + ": " + (board.getTimeLeft(Colour.RED) / 1000);
    drawAgentLabel(g, 2.5*h_unit, 8.5*v_unit, Math.PI/3, redText, redActive);

    g.setFont(new Font(g.getFont().getFontName(), Font.PLAIN, PIECE_FONTSIZE));
    for(Position pos: Position.values())squares[pos.ordinal()].setPiece(board.getPiece(pos));
    for(Square sq: squares) sq.draw(g);
  }

  private static void drawAgentLabel(Graphics2D g, double x, double y, double angleRads, String string, boolean active) {
    AffineTransform orig = g.getTransform();
    g.translate(x, y);
    g.rotate(angleRads);
    FontMetrics metrics = g.getFontMetrics();
    int width = metrics.stringWidth(string);
    int drawX = -width / 2;
    int drawY = metrics.getAscent() - metrics.getHeight() / 2;
    g.drawString(string, drawX, drawY);
    if (active) {
      g.drawString("*", drawX - metrics.stringWidth("*"), 0);
    }
    g.setTransform(orig);
  }

  //calculates the coordinates of flanks for computing square coordinates.
  private static void setFlanks(int size){
    int h_unit = size/10;
    int v_unit = (int) (h_unit*Math.sqrt(3));
    flanks = new int[3][3][4];
    flanks[0][0][0] = 7*h_unit; flanks[0][0][1] = v_unit; flanks[0][0][2] = 8*h_unit; flanks[0][0][3] = 2*v_unit;// (x1,y1,x2,y2) coords of the left flank of the blue section of board
    flanks[0][1][0] = 5*h_unit; flanks[0][1][1] = v_unit; flanks[0][1][2] = 5*h_unit; flanks[0][1][3] = 3*v_unit;// (x1,y1,x2,y2) coords of the middle line of the blue section of board
    flanks[0][2][0] = 3*h_unit; flanks[0][2][1] = v_unit; flanks[0][2][2] = 2*h_unit; flanks[0][2][3] = 2*v_unit;// (x1,y1,x2,y2) coords of the right flank of the blue section of board
    flanks[1][0][0] = 7*h_unit; flanks[1][0][1] = 5*v_unit; flanks[1][0][2] = 5*h_unit; flanks[1][0][3] = 5*v_unit;// (x1,y1,x2,y2) coords of the left flank of the green section of board
    flanks[1][1][0] = 8*h_unit; flanks[1][1][1] = 4*v_unit; flanks[1][1][2] = 5*h_unit; flanks[1][1][3] = 3*v_unit;// (x1,y1,x2,y2) coords of the middle line of the green section of board
    flanks[1][2][0] = 9*h_unit; flanks[1][2][1] = 3*v_unit; flanks[1][2][2] = 8*h_unit; flanks[1][2][3] = 2*v_unit;// (x1,y1,x2,y2) coords of the right flank of the green section of board
    flanks[2][0][0] = h_unit; flanks[2][0][1] = 3*v_unit; flanks[2][0][2] = 2*h_unit; flanks[2][0][3] = 2*v_unit;// (x1,y1,x2,y2) coords of the left flank of the red section of board
    flanks[2][1][0] = 2*h_unit; flanks[2][1][1] = 4*v_unit; flanks[2][1][2] = 5*h_unit; flanks[2][1][3] = 3*v_unit;// (x1,y1,x2,y2) coords of the middle line of the red section of board
    flanks[2][2][0] = 3*h_unit; flanks[2][2][1] = 5*v_unit; flanks[2][2][2] = 5*h_unit; flanks[2][2][3] = 5*v_unit;// (x1,y1,x2,y2) coords of the right flank of the red section of board
  }

  /** @return {@param string} if its length is below {@param length}, or else its first {@param length} characters. **/
  private static String truncateBelowLength(String string, int length) {
    return string.length() > length ? string.substring(0, length) : string;
  }
}
