package my.packagename.infrastructure.driven.adapters.jdbc;

import my.packagename.application.port.outbound.TransactionCallbackPort;
import my.packagename.application.port.outbound.TransactionPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JdbcTransactionAdapter<R> implements TransactionPort<R> {

  @Override
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public R run(TransactionCallbackPort<R> callback) {
    return callback.call();
  }
}
