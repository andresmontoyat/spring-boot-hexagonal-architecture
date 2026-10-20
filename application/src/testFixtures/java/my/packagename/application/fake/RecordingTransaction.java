package my.packagename.application.fake;

import my.packagename.application.port.outbound.TransactionCallbackPort;
import my.packagename.application.port.outbound.TransactionPort;

/** Runs the callback inline and remembers how many boundaries were opened. */
public final class RecordingTransaction<R> implements TransactionPort<R> {

  private int boundaries;

  @Override
  public R run(TransactionCallbackPort<R> callback) {
    boundaries++;
    return callback.call();
  }

  public int boundaries() {
    return boundaries;
  }
}
