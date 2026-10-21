package my.packagename.application.port.outbound;

/** Transaction boundary as a port, so use cases stay free of {@code @Transactional}. */
@FunctionalInterface
public interface TransactionPort<R> {
  R run(TransactionCallbackPort<R> callback);
}
