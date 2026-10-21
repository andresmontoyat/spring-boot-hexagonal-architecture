package my.packagename.application.port.outbound;

@FunctionalInterface
public interface TransactionCallbackPort<R> {
  R call();
}
