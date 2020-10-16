package threeChess;

import threeChess.agents.GUIAgent;
import threeChess.agents.RandomAgent;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Holds the state to simulate a three-chess game.
 */
public class Game implements Runnable {

  /** A random agent used to have a valid move to pass to the board when an agent times out. **/
  private static final RandomAgent randomAgent = new RandomAgent();
  /** The additional delay allowed for the executor being slow when executing moves. **/
  private static final int ADDITIONAL_DELAY_ALLOWANCE_MS = 3000;

  /** Represents the status of the game. **/
  public enum GameStatus {
    RUNNING, GAME_OVER, TIMED_OUT, HIT_MAX_TURNS, ILLEGAL_MOVE
  }

  private final Agent[] agents;
  private final int timeLimitSeconds;
  private final boolean isTimed;
  private final int maximumTurns;
  private final int pauseMS;
  private final PrintStream logger;

  private final Board board;
  private final ThreeChessDisplay display;
  private final AtomicReference<GameStatus> status = new AtomicReference<>();
  private int[] scores;

  /**
   * Constructs a threeChess game between three players.
   *
   * @param agents a length 3 array of the agents to play the game, in order of blue, green, and then red.
   * @param timeLimitSeconds the cumulative time each player has (in seconds).
   *                         To specify an untimed game, set as less than or equal to zero.
   * @param maximumTurns the maximum number of turns to play before deciding the game is a draw.
   *                     To specify no maximum, set to less than or equal to zero.
   * @param pauseMS the minimum amount of time to wait in milliseconds between each turn.
   * @param logger a printStream to write the game moves to.
   * @param displayOn a boolean flag for whether the game should be graphically displayed
   */
  public Game(
      Agent[] agents, int timeLimitSeconds, int maximumTurns,
      int pauseMS, PrintStream logger, boolean displayOn) {

    if (agents.length != 3)
      throw new IllegalArgumentException();

    this.agents = agents;
    this.timeLimitSeconds = timeLimitSeconds;
    this.isTimed = (timeLimitSeconds > 0);
    this.maximumTurns = maximumTurns;
    this.pauseMS = pauseMS;
    this.logger = logger;

    board = new Board(isTimed ? 1000 * timeLimitSeconds : 1);
    if (displayOn) {
      display = new ThreeChessDisplay(board, agents[0].toString(), agents[1].toString(), agents[2].toString());
      GUIAgent.currentDisplay = display;
    } else {
      display = null;
    }
    status.set(GameStatus.RUNNING);

    log("======NEW GAME======");
    log("BLUE: " + agents[0]);
    log("GREEN: " + agents[1]);
    log("RED: " + agents[2]);
  }

  /** @return the status of this game. **/
  public GameStatus getStatus() {
    return status.get();
  }

  /** @return a length-3 array of the final score for each agent in the order blue, green, red. **/
  public int[] getAgentScores() {
    if (status.get() == GameStatus.RUNNING)
      throw new IllegalStateException("Game is not over!");

    return scores;
  }

  /** @return a length-3 array of the time spent in milliseconds for each agent in the order blue, green, red. **/
  public int[] getAgentTimes() {
    int[] times = new int[3];
    for (Colour colour : Colour.values()) {
      times[colour.ordinal()] = 1000 * timeLimitSeconds - board.getTimeLeft(colour);
    }
    return times;
  }

  /** @return the agents that are playing in this game. **/
  public Agent[] getAgents() {
    return agents;
  }

  /** Updates the status of this game to a game over status. **/
  private void endGame(GameStatus status, int[] scores) {
    if (status == GameStatus.RUNNING)
      throw new IllegalStateException("Can not end the game with status " + status);

    this.scores = scores;
    this.status.set(status);

    repaint();
    GUIAgent.currentDisplay = null;

    log("=====Game Over=====");
    for (Colour colour : Colour.values()) {
      int score = scores[colour.ordinal()];
      int timeLeft = board.getTimeLeft(colour);
      log(colour + " - score: " + score + ", time:" + timeLeft);
    }
  }

  /** Plays out the whole game. **/
  @Override
  public void run() {
    ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    executor.allowCoreThreadTimeOut(true);

    try {
      playGame(executor);

      // Sanity check.
      if (status.get() == GameStatus.RUNNING)
        throw new IllegalStateException();
    } finally {
      executor.shutdownNow();

      try {
        // Wait for the executor service to shutdown.
        if (!executor.awaitTermination(15, TimeUnit.SECONDS)) {
          System.err.println("Executor service is not terminating, is there an infinite loop in an agent?");
        }
      } catch (InterruptedException e) {
        // Preserve interrupt status.
        Thread.currentThread().interrupt();
      }
    }
  }

  /** Plays out the whole game. **/
  public void playGame(ExecutorService executor) {
    if (status.get() != GameStatus.RUNNING)
      throw new IllegalStateException("Game is not running! Has status " + status);
    if (board.gameOver())
      throw new IllegalStateException("Board is showing gameOver, yet status is " + status);

    // Keep looping until game over.
    while (true) {
      long start = System.nanoTime();

      // Play one turn.
      Colour turnColour = board.getTurn();
      Agent agent = agents[turnColour.ordinal()];
      playTurn(executor, turnColour, agent);

      // Check if the status has changed from running.
      if (status.get() != GameStatus.RUNNING)
        break;

      // Check for game over.
      if (board.gameOver()) {
        GameStatus status = (board.isTimedOut() ? GameStatus.TIMED_OUT : GameStatus.GAME_OVER);
        int[] scores = {0, 0, 0};
        scores[board.getWinner().ordinal()] = 1;
        scores[board.getLoser().ordinal()] = -1;
        endGame(status, scores);
        break;
      }

      // Check for hitting maximum turns.
      if (maximumTurns > 0 && board.getMoveCount() >= maximumTurns) {
        int[] scores = {0, 0, 0};
        endGame(GameStatus.HIT_MAX_TURNS, scores);
        break;
      }

      // Pause as much time as needed to make the length of this turn at least pauseMS.
      long durationMS = (System.nanoTime() - start + 500_000L) / 1_000_000L;
      long pauseTimeMS = pauseMS - durationMS;
      if (agent.isAutonomous() && display != null && pauseTimeMS > 0) {
        try {
          Thread.sleep(pauseTimeMS);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  /** @return a clone of the current board of the game. **/
  private Board cloneBoard() {
    try {
      return (Board) board.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  /** Plays one turn of the game. **/
  public void playTurn(ExecutorService executor, Colour turnColour, Agent agent) {
    if (status.get() != GameStatus.RUNNING)
      throw new IllegalStateException("Game is not running! Has status " + status);

    // Submit the move to be decided in a separate thread.
    AgentPlayMoveTask task = new AgentPlayMoveTask(agent, cloneBoard());
    AgentPlayMoveResult result;
    try {
      Future<AgentPlayMoveResult> future = executor.submit(task);

      // Get the resulting move the agent decided, being careful of timeouts.
      if (isTimed) {
        long timeRemainingMS = board.getTimeLeft(turnColour);
        try {
          result = future.get(timeRemainingMS + ADDITIONAL_DELAY_ALLOWANCE_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
          future.cancel(true);

          // The agent timed out trying to decide its move, give it a random move.
          log(turnColour + " agent " + agent + " TIMED OUT");
          Position[] move = randomAgent.playMove(cloneBoard());
          result = new AgentPlayMoveResult(move, timeRemainingMS + ADDITIONAL_DELAY_ALLOWANCE_MS);
        }
      } else {
        result = future.get();
      }
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e);
    }

    // Get the move and duration from the result object.
    Position[] move = result.move;
    long durationMS = result.durationMS;

    // Check if it was an illegal move, and if so end the game.
    if (move == null || move.length != 2 || !board.isLegalMove(move[0], move[1])) {
      log("INVALID MOVE: " + Arrays.toString(move));
      int[] scores = {1, 1, 1};
      scores[turnColour.ordinal()] = -2;
      endGame(GameStatus.ILLEGAL_MOVE, scores);
      return;
    }

    // Play the move on the board.
    try {
      board.move(move[0], move[1], (int) durationMS);
      log(turnColour + ": " + move[0] + '-' + move[1] + " t:" + durationMS);
      repaint();
    } catch (ImpossiblePositionException e) {
      throw new RuntimeException(e);
    }
  }

  /** If this Game has a logger, println's the given log. **/
  private void log(String log) {
    if (logger != null) {
      logger.println(log);
    }
  }

  /** If there is a display, repaints it. **/
  private void repaint() {
    if (display != null) {
      display.repaintCanvas();
    }
  }

  /** The result of a AgentPlayMoveTask. **/
  private static class AgentPlayMoveResult {
    public final Position[] move;
    public final long durationMS;
    private AgentPlayMoveResult(Position[] move, long durationMS) {
      this.move = move;
      this.durationMS = durationMS;
    }
  }

  /** A task that can be submitted to an executor service to get an agent to play a move. **/
  private static class AgentPlayMoveTask implements Callable<AgentPlayMoveResult> {
    private final Agent agent;
    private final Board board;
    private AgentPlayMoveTask(Agent agent, Board board) {
      this.agent = agent;
      this.board = board;
    }
    @Override
    public AgentPlayMoveResult call() {
      long startTimeNanos = System.nanoTime();
      Position[] move = agent.playMove(board);
      long durationNanos = System.nanoTime() - startTimeNanos;
      long durationMS = (durationNanos + 500_000L) / 1_000_000L;
      return new AgentPlayMoveResult(move, durationMS);
    }
  }
}
