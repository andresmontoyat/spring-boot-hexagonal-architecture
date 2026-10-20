package my.packagename.application.fake;

import my.packagename.application.port.outbound.TransactionCallbackPort;
import my.packagename.application.port.outbound.TransactionPort;

/** Runs the callback inline, counting boundaries and exposing whether one is open right now. */
public final class RecordingTransaction<R> implements TransactionPort<R> {

  private int boundaries;
  private boolean open;

  @Override
  public R run(TransactionCallbackPort<R> callback) {
    boundaries++;
    open = true;
    try {
      return callback.call();
    } finally {
      open = false;
    }
  }

  public int boundaries() {
    return boundaries;
  }

  public boolean isOpen() {
    return open;
  }
}
