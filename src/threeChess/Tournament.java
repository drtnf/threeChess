package threeChess;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.*;

/**
 * Runs a tournament between a set of agents.
 */
public class Tournament {

  /** Used to randomly select agents for games. **/
  private static final Random random = new Random();
  /** The interval in seconds between the tournament reporting how many games have finished. **/
  private static final int SECONDS_PER_REPORT = 10;

  private final Agent[] agents;
  private final int numGames;
  private final int timeLimitSeconds;
  private final int maximumTurns;
  private final int pauseMS;
  private final int threads;
  private final PrintStream logger;
  private final boolean displayOn;

  private final ThreadPoolExecutor executor;

  private final Map<Agent, AgentStats> stats = new HashMap<>();

  /**
   * @param agents the agents to be tested.
   *
   * @param numGames the number of games in total to be played.
   *
   * @param timeLimitSeconds the time limit in seconds for each agent in each game.
   *                         If this is set to zero, there is no time limit.
   *
   * @param maximumTurns the maximum number of turns before aborting each game.
   *                     If this is set to zero, there is no turn limit.
   *
   * @param pauseMS the minimum amount of time to spend for each turn in the game.
   *
   * @param threads the number of threads to run games on.
   *
   * @param logger the logger to print the output of the tournament to.
   *
   * @param displayOn whether to create a display for each of the games.
   */
  public Tournament(
      Agent[] agents, int numGames, int timeLimitSeconds, int maximumTurns,
      int pauseMS, int threads, PrintStream logger, boolean displayOn) {

    if (agents.length < 3)
      throw new IllegalArgumentException();
    if (threads < 1)
      throw new IllegalArgumentException();

    this.agents = agents;
    this.numGames = numGames;
    this.timeLimitSeconds = timeLimitSeconds;
    this.maximumTurns = maximumTurns;
    this.pauseMS = pauseMS;
    this.threads = threads;
    this.logger = logger;
    this.displayOn = displayOn;

    if (threads > 1) {
      executor = new ThreadPoolExecutor(threads, threads, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
      executor.allowCoreThreadTimeOut(true);
    } else {
      executor = null;
    }

    for (Agent agent : agents) {
      if (stats.put(agent, new AgentStats(agent)) != null)
        throw new IllegalArgumentException("Duplicate entry for agent " + agent);
    }
  }

  /** @return the stats for each agent, ordered in descending order. **/
  public List<AgentStats> getDescendingStats() {
    List<AgentStats> agentStats = new ArrayList<>(stats.values());
    Collections.sort(agentStats);
    return agentStats;
  }

  /** Runs this tournament. **/
  public void runTournament() {
    // Suppress game output when threaded.
    PrintStream gameLogger = (threads > 1 ? null : logger);

    // Generate all of the games to be played.
    List<Game> gamesToPlay = new ArrayList<>();
    for (int i=0; i < numGames; ++i) {
      gamesToPlay.add(selectGame(gameLogger));
    }
    logger.println("Starting tournament with " + gamesToPlay.size() + " games");

    // Run all of the games.
    if (threads > 1) {
      runGamesThreaded(gamesToPlay);
    } else {
      for (Game game : gamesToPlay) {
        game.run();
      }
    }

    // Accumulate the statistics of all the games.
    for (Game game : gamesToPlay) {
      Game.GameStatus status = game.getStatus();
      Agent[] agents = game.getAgents();
      int[] scores = game.getAgentScores();
      int[] times = game.getAgentTimes();
      for (Colour colour : Colour.values()) {
        Agent agent = agents[colour.ordinal()];

        int score = scores[colour.ordinal()];
        AgentStats agentStats = stats.get(agent);
        int time = times[colour.ordinal()];
        agentStats.update(score, status, time);
      }
    }

    // Print the statistics of all the agents.
    List<AgentStats> agentStats = new ArrayList<>(new HashSet<>(stats.values()));
    Collections.sort(agentStats);
    logger.println();
    for (AgentStats agentStat : agentStats) {
      logger.println(agentStat);
    }
  }

  /** Runs all of the games threaded. **/
  private void runGamesThreaded(List<Game> gamesToPlay) {
    if (executor == null)
      throw new IllegalStateException();

    List<Future<?>> futures = new ArrayList<>();
    for (Game game : gamesToPlay) {
      futures.add(executor.submit(game));
    }

    // Wait for all of the games to finish, giving periodic updates.
    int reportCountdown = SECONDS_PER_REPORT;
    while (futures.size() > 0) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      // Remove the futures that have finished execution.
      Iterator<Future<?>> iter = futures.iterator();
      while (iter.hasNext()) {
        Future<?> future = iter.next();
        if (future.isDone()) {
          iter.remove();
          // Print exceptions that occurred in the future.
          try {
            future.get();
          } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
          }
        }
      }

      if (--reportCountdown <= 0 || futures.isEmpty()) {
        reportCountdown = SECONDS_PER_REPORT;
        logger.println((gamesToPlay.size() - futures.size()) + " / " + gamesToPlay.size() + " games completed");
      }
    }
  }

  /** @return a random game to be played. **/
  private Game selectGame(PrintStream logger) {
    // Select 3 random agents.
    List<Agent> availableAgents = new ArrayList<>(Arrays.asList(agents));
    Agent[] agents = {
        selectAndRemoveRandomAgent(availableAgents),
        selectAndRemoveRandomAgent(availableAgents),
        selectAndRemoveRandomAgent(availableAgents)
    };

    // Copy the agents and link them back to their original agents.
    for (int index = 0; index < agents.length; ++index) {
      Agent original = agents[index];
      Agent clone = original.clone();
      agents[index] = clone;
      stats.put(clone, stats.get(original));
    }

    return new Game(agents, timeLimitSeconds, maximumTurns, pauseMS, logger, displayOn);
  }

  /** @return a random agent from {@param availableAgents} that gets removed from the list. **/
  private static Agent selectAndRemoveRandomAgent(List<Agent> availableAgents) {
    int index = random.nextInt(availableAgents.size());
    return availableAgents.remove(index);
  }

  /** Holds the statistics for a given agent. **/
  public static class AgentStats implements Comparable<AgentStats> {
    public final Agent agent;
    public int won = 0;
    public int lost = 0;
    public int timedOut = 0;
    public int illegalMoves = 0;
    public int draws = 0;
    public int turnedOut = 0;
    public int games = 0;
    public int totalTimeMilliseconds = 0;

    public AgentStats(Agent agent) {
      this.agent = agent;
    }

    /** Updates the stats of this agent with a score from a game. **/
    public void update(int score, Game.GameStatus status, int timeMS) {
      games += 1;
      totalTimeMilliseconds += timeMS;
      if (score > 0) {
        won += score;
      } else if (score < 0) {
        lost -= score;
        switch (status) {
          case TIMED_OUT:
            timedOut += 1;
            break;
          case ILLEGAL_MOVE:
            illegalMoves += 1;
            break;
          default:
            break;
        }
      } else {
        draws += 1;
        if (status == Game.GameStatus.HIT_MAX_TURNS) {
          turnedOut += 1;
        }
      }
    }

    /** @return an average score for this agent. **/
    public double average() {
      if (games == 0)
        return 0;
      return (double) (won - lost) / games;
    }

    @Override
    public String toString() {
      double avg = Math.round(average() * 100.0d) / 100.0d;
      double seconds = Math.round((double) totalTimeMilliseconds / games / 10.0d) / 100.0d;
      return pad(agent.toString(), 20, 3)
          + pad("avg: " + avg + ",", 14, 2)
          + pad("won: " + won + ",", 10, 2)
          + pad("draw: " + draws + ",", 11, 2)
          + pad("lost: " + lost + ",", 11, 2)
          + pad("timed-out: " + timedOut + ",", 16, 2)
          + pad("illegal-moves: " + illegalMoves + ",", 20, 2)
          + pad("hit-max-turns: " + turnedOut + ",", 20, 2)
          + pad("games: " + games, 12, 2)
          + pad("time: " + seconds, 13, 2);
    }

    @Override
    public int compareTo(AgentStats other) {
      return Double.compare(other.average(), average());
    }
  }

  /** Pads after the given string with zeroes until it is at least the given length. **/
  private static String pad(String str, int length, int minimumPad) {
    StringBuilder builder = new StringBuilder(str);
    while (builder.length() < length) {
      builder.append(' ');
    }
    for (int i=0; i<minimumPad; ++i) {
      builder.append(' ');
    }
    return builder.toString();
  }
}
