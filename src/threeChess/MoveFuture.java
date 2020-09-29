package threeChess;

import java.util.concurrent.*;

/**
 * Represents a move that will be selected in the future.
 */
public class MoveFuture implements Future<Position[]> {

  private final BlockingQueue<Position[]> move = new ArrayBlockingQueue<>(1);

  protected void complete(Position[] move) throws InterruptedException {
    this.move.put(move);
  }

  @Override
  public Position[] get() throws InterruptedException {
    return move.take();
  }

  @Override
  public Position[] get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
    Position[] moveOrNull = move.poll(timeout, unit);
    if (moveOrNull == null)
      throw new TimeoutException();
    return moveOrNull;
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isCancelled() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isDone() {
    throw new UnsupportedOperationException();
  }
}
