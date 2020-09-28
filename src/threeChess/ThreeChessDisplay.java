package threeChess;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
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
  private static final Color[] DARKCOLOURS = {DARKBLUE, DARKGREEN, DARKRED};
  private static final Color[] LIGHTCOLOURS = {LIGHTBLUE, LIGHTGREEN, LIGHTRED};
  private static final int LABELS_FONTSIZE = 16;
  private static final int AGENTS_FONTSIZE = 24;
  private static final int PIECE_FONTSIZE = 32;
  private static final int CAPTURED_FONTSIZE = 24;
  private static final int AGENT_NAME_MAX_LENGTH = 20;
  private static final int CAPTURED_PER_ROW = 11;
  private static final int HISTORY_CYCLE_CORNER_SIZE = 100;
  private static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);
  private final Square[] squares;
  private final String[] players;
  private final Canvas canvas;
  private final Board board;
  private final int size = 800;
  private static int[][][] flanks;
  private MoveFuture manualMoveFuture;
  private Position manualMoveFrom;
  private int lastMouseX = -1;
  private int lastMouseY = -1;
  private int historyMoveIndex = -1;

  /** To represent each square of the board. **/
  private class Square{
    private final Position pos;
    private final int[] xs;//4 x-coords
    private final int[] ys;//4 y-coords
    private final int[] highlightXs;// 4 x-coords for highlighting this square.
    private final int[] highlightYs;// 4 y-coords for highlighting this square.
    private final int[] centre;// x,y coordinates of the centre of this square.
    private final boolean coloured;// for white or not
    private final Colour colour;
    private Piece piece; //piece in the square or null if empty

    /**
     * Constructs a square corresponding to the position given.
     * This calculates the cartesian coordinates of the corners of the position, 
     * and records the squares colour and parity
     * **/
    public Square(Position pos){
      this.pos = pos;
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
      // The centre of this square.
      centre = new int[] {(xs[0]+xs[1]+xs[2]+xs[3]) / 4, (ys[0]+ys[1]+ys[2]+ys[3]) / 4};
      // Coordinates of polygon for highlighting the square.
      highlightXs = new int[4];
      highlightYs = new int[4];
      for (int i=0; i<4; ++i) {
        highlightXs[i] = centre[0] + (3 * (xs[i] - centre[0])) / 4;
        highlightYs[i] = centre[1] + (3 * (ys[i] - centre[1])) / 4;
      }
    }

    /** @return whether the given coordinates fall within this square. **/
    public boolean contains(int x, int y) {
      boolean result = false;
      for (int i=0, j=3; i<4; j=i++) {
        if ((ys[i] > y) != (ys[j] > y) && (x < (xs[j] - xs[i]) * (y - ys[i]) / (ys[j]-ys[i]) + xs[i])) {
          result = !result;
        }
      }
      return result;
    }

    /** Sets the piece of the square to the specified piece, or null if square is unoccupied. **/
    public void setPiece(Piece piece){this.piece = piece;}

    /** @return a colour for pieces and boards squares. **/
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
     */
    public void draw(Graphics g){
      g.setColor(getColour(colour, false, coloured));
      g.fillPolygon(xs,ys,4);
      if (board.displayLegalMoves() && manualMoveFrom != null && board.isLegalMove(manualMoveFrom, pos)) {
        g.setColor(Color.WHITE);
        g.drawPolygon(highlightXs, highlightYs, 4);
      }
      g.setColor(Color.BLACK);
      g.drawPolygon(xs, ys, 4);
      if(piece!=null){
        String pieceStr = Character.toString(piece.getType().getChar());
        FontMetrics metrics = g.getFontMetrics();
        int x = centre[0] - metrics.stringWidth(pieceStr) / 2;
        int y = centre[1] - metrics.getHeight() / 2 + metrics.getAscent();
        g.setColor(pos == manualMoveFrom ? Color.WHITE : getColour(colour, false, false));
        for (int dx = -1; dx <= 1; ++dx) {
          for (int dy = -1; dy <= 1; ++dy) {
            if (dx != 0 || dy != 0) g.drawString(pieceStr, x + dx, y + dy);
          }
        }
        g.setColor(getColour(piece.getColour(), true, true));
        g.drawString(pieceStr, x, y);
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
    MouseAdapter mouseListener = new MouseAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        onMouseMove(e.getX(), e.getY());
      }
      @Override
      public void mouseExited(MouseEvent e) {
        onMouseMove(-1, -1);
      }
      @Override
      public void mouseReleased(MouseEvent e) {
        onMouseClick(e.getX(), e.getY(), e.getButton());
        repaintCanvas();
      }
    };
    canvas.addMouseListener(mouseListener);
    canvas.addMouseMotionListener(mouseListener);

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

  /** @return the state of the board before move {@param moveIndex}. **/
  private Board getHistoryBoard(int moveIndex) {
    CheatBoard historyBoard = new CheatBoard();
    for (int index = 0; index < moveIndex; ++index) {
      Position[] move = board.getMove(index);
      try {
        historyBoard.move(move[0], move[1]);
      } catch (ImpossiblePositionException e) {}
    }
    return historyBoard;
  }

  /** @return whether a past move is currently being displayed. **/
  public boolean displayingHistory() {
    return historyMoveIndex >= 0;
  }

  /** @return whether the user is allowed to display the history. **/
  public boolean canDisplayHistory() {
    return board.getMoveCount() > 0 && (board.gameOver() || waitingForManualMove());
  }

  /** @return the board that should currently be rendered. **/
  private Board getRenderedBoard() {
    return displayingHistory() ? getHistoryBoard(historyMoveIndex) : board;
  }

  /** Puts the display into a state where it expects a move to be made. **/
  public MoveFuture askForMove() {
    manualMoveFuture = new MoveFuture();
    onMouseMove(lastMouseX, lastMouseY); // Updates the cursor.
    repaintCanvas();
    return manualMoveFuture;
  }

  /** @return the Square at the given coordinates, or null. */
  private Square getSquare(int x, int y) {
    for (Square sq: squares) {
      if (sq.contains(x, y))
        return sq;
    }
    return null;
  }

  /** @return whether the display is currently waiting for the user to make a move. **/
  public boolean waitingForManualMove() {
    return manualMoveFuture != null;
  }

  /** Called when the user moves their mouse. **/
  public void onMouseMove(int x, int y) {
    // If the user hovers over the history cycle corners change the cursor to a hand.
    if (y <= HISTORY_CYCLE_CORNER_SIZE && (x <= HISTORY_CYCLE_CORNER_SIZE || x >= size - HISTORY_CYCLE_CORNER_SIZE)) {
      if (displayingHistory() || canDisplayHistory()) {
        canvas.setCursor(HAND_CURSOR);
      } else {
        canvas.setCursor(Cursor.getDefaultCursor());
      }
      return;
    }
    // If we're otherwise displaying history, nothing past this point can be interacted with.
    if (displayingHistory()) {
      canvas.setCursor(Cursor.getDefaultCursor());
      return;
    }

    Square square = getSquare(x, y);
    // Set the mouse cursor to a hand if it would be a valid click.
    if (manualMoveFuture == null
        || square == null
        || manualMoveFrom == square.pos
        || (manualMoveFrom == null && square.piece == null)
        || (manualMoveFrom == null && square.piece.getColour() != board.getTurn())
        || (manualMoveFrom != null && !board.isLegalMove(manualMoveFrom, square.pos))) {

      canvas.setCursor(Cursor.getDefaultCursor());
    } else {
      canvas.setCursor(HAND_CURSOR);
    }
  }

  /** Called when the user clicks somewhere on the canvas. **/
  public void onMouseClick(int x, int y, int button) {
    // If the user clicks in the top left or right, cycle through past moves.
    if (y <= HISTORY_CYCLE_CORNER_SIZE && (x <= HISTORY_CYCLE_CORNER_SIZE || x >= size - HISTORY_CYCLE_CORNER_SIZE)) {
      if (displayingHistory() || canDisplayHistory()) {
        int count = board.getMoveCount();
        int direction = (x <= HISTORY_CYCLE_CORNER_SIZE ? -1 : 1);
        if ((direction == 1 && historyMoveIndex == count - 1) || (direction == -1 && historyMoveIndex == 0)) {
          historyMoveIndex = -1;
        } else if (historyMoveIndex == -1) {
          historyMoveIndex = (direction == 1 ? 0 : count - 1);
        } else {
          historyMoveIndex = historyMoveIndex + direction;
        }
      } else {
        historyMoveIndex = -1;
      }
      return;
    }
    // If we're not expecting the user to make a move, ignore clicks.
    if (displayingHistory() || manualMoveFuture == null)
      return;

    Square square = getSquare(x, y);
    // If the user didn't click within a square, or clicked within an empty square, clear the current move.
    if (square == null || manualMoveFrom == square.pos || button != MouseEvent.BUTTON1) {
      manualMoveFrom = null;
      return;
    }
    // If the user hasn't selected a piece to move yet, then set the clicked square as the piece.
    if (manualMoveFrom == null) {
      manualMoveFrom = (square.piece != null && square.piece.getColour() == board.getTurn() ? square.pos : null);
      return;
    }
    // Make sure the move the user selected is a legal move.
    if (!board.isLegalMove(manualMoveFrom, square.pos)) {
      System.err.println("Illegal move, try again.");
      manualMoveFrom = null;
      return;
    }
    // The user has selected a piece to move, and now a position to move it to, so complete the future.
    try {
      manualMoveFuture.complete(new Position[] {manualMoveFrom, square.pos});
      manualMoveFuture = null;
      manualMoveFrom = null;
    } catch (InterruptedException interruptedException) {
      interruptedException.printStackTrace();
    }
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

  /** @return a graphics object that can be used to draw to the canvas. **/
  private Graphics2D getCanvasGraphics() {
    Graphics2D g = (Graphics2D) canvas.getBufferStrategy().getDrawGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    return g;
  }

  /** Repaints the board to the canvas. **/
  public void repaintCanvas(){
    Graphics2D g = getCanvasGraphics();
    try {
      drawToCanvas(g, getRenderedBoard());
    }
    finally {
      g.dispose();
    }
    canvas.getBufferStrategy().show();
  }

  public void drawToCanvas(Graphics2D g, Board board) {
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

    drawAgentLabel(g, board, getWidth() / 2.0, 1.25*v_unit, 0, Colour.BLUE);
    drawAgentLabel(g, board, 17*h_unit, 8.5*v_unit, -Math.PI/3, Colour.GREEN);
    drawAgentLabel(g, board, 3*h_unit, 8.5*v_unit, Math.PI/3, Colour.RED);

    g.setFont(new Font(g.getFont().getFontName(), Font.PLAIN, PIECE_FONTSIZE));
    for(Position pos: Position.values())squares[pos.ordinal()].setPiece(board.getPiece(pos));
    for(Square sq: squares) sq.draw(g);

    // If we're displaying the history, we want to draw the move as well.
    if (displayingHistory()) {
      Position[] move = this.board.getMove(historyMoveIndex);
      Square from = squares[move[0].ordinal()];
      Square to = squares[move[1].ordinal()];
      g.setColor(LIGHTCOLOURS[from.piece.getColour().ordinal()]);
      drawArrow(g, from, to, 6);
      g.setColor(DARKCOLOURS[from.piece.getColour().ordinal()]);
      drawArrow(g, from, to, 4);
    }

    // Draw the history back and forwards button.
    if (displayingHistory() || canDisplayHistory()) {
      g.setColor(Color.DARK_GRAY);
      int x = size - HISTORY_CYCLE_CORNER_SIZE;
      int unit = HISTORY_CYCLE_CORNER_SIZE / 4;
      drawArrow(g, new int[] {2*unit, 2*unit, unit}, new int[] {5*unit/3, unit, unit}, 2); // Back button.
      drawArrow(g, new int[] {x+unit, x+unit, x+2*unit}, new int[] {5*unit/3, unit, unit}, 2); // Forward button.
    }
  }

  private static void drawArrow(Graphics2D g, Square from, Square to, int strokeWidth) {
    int fromX = from.centre[0], fromY = from.centre[1];
    int toX = to.centre[0], toY = to.centre[1];
    double angle = Math.atan2(toY - fromY, toX - fromX);
    fromX += (int) (10*Math.cos(angle)); toX -= (int) (10*Math.cos(angle));
    fromY += (int) (10*Math.sin(angle)); toY -= (int) (10*Math.sin(angle));
    drawArrow(g, new int[] {fromX, toX}, new int[] {fromY, toY}, strokeWidth);
  }

  private static void drawArrow(Graphics2D g, int[] xs, int[] ys, int strokeWidth) {
    int fromX = xs[xs.length - 2], fromY = ys[ys.length - 2];
    int toX = xs[xs.length - 1], toY = ys[ys.length - 1];
    double angle = Math.atan2(toY - fromY, toX - fromX);
    g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f));
    for (int i=0; i < xs.length - 1; ++i) {
      g.drawLine(xs[i], ys[i], xs[i+1], ys[i+1]);
    }
    g.drawLine(toX, toY, (int) (toX - 15*Math.cos(angle - Math.PI/6)), (int) (toY - 15*Math.sin(angle - Math.PI/6)));
    g.drawLine(toX, toY, (int) (toX - 15*Math.cos(angle + Math.PI/6)), (int) (toY - 15*Math.sin(angle + Math.PI/6)));
  }

  private void drawAgentLabel(Graphics2D g, Board board, double x, double y, double angleRads, Colour colour) {
    // Get the information we want to display about the agent.
    boolean winner = (board.getWinner() == colour);
    boolean active = (!board.gameOver() && board.getTurn() == colour);
    String text = players[colour.ordinal()];
    if (!displayingHistory()) {
      text += ": " + (board.getTimeLeft(colour) / 1000);
    }
    StringBuilder takenString = new StringBuilder();
    List<Piece> captured = board.getCaptured(colour);
    for (Piece piece : captured) {
        takenString.append(piece.getType().getChar());
    }

    // Rotate the text.
    AffineTransform orig = g.getTransform();
    g.translate(x, y);
    g.rotate(angleRads);

    // Draw the agent's name.
    Font previousFont = g.getFont();
    g.setFont(new Font(previousFont.getFontName(), winner ? Font.BOLD : Font.PLAIN, AGENTS_FONTSIZE));
    FontMetrics metrics = g.getFontMetrics();
    int width = metrics.stringWidth(text);
    int drawX = -width / 2;
    int drawY = metrics.getAscent() - metrics.getHeight() / 2;
    g.setColor(DARKCOLOURS[colour.ordinal()]);
    g.drawString(text, drawX, drawY);

    // We add a * when it is the agent's turn.
    if (active) {
      g.drawString("*", drawX - metrics.stringWidth("*"), 0);
    }

    // We draw the pieces the agent has taken below their name.
    g.setFont(new Font(previousFont.getFontName(), Font.PLAIN, CAPTURED_FONTSIZE));
    metrics = g.getFontMetrics();
    int lineHeight = metrics.getHeight() * (colour == Colour.BLUE ? -1 : 1);
    int capturedY = drawY;

    int rows = (takenString.length() + CAPTURED_PER_ROW - 1) / CAPTURED_PER_ROW;
    for (int row = 0; row < rows; ++row) {
        capturedY += lineHeight;
        String line = takenString.substring(row * CAPTURED_PER_ROW, Math.min(takenString.length(), (row + 1) * CAPTURED_PER_ROW));
        int takenWidth = metrics.stringWidth(line);
        int capturedX = -takenWidth / 2;
        for (int i=0; i < line.length(); ++i) {
            Piece piece = captured.get(row * CAPTURED_PER_ROW + i);
            String pieceStr = Character.toString(piece.getType().getChar());
            g.setColor(DARKCOLOURS[piece.getColour().ordinal()]);
            g.drawString(pieceStr, capturedX, capturedY);
            capturedX += metrics.stringWidth(pieceStr);
        }
    }

    // Reset the rotation and font.
    g.setTransform(orig);
    g.setFont(previousFont);
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
