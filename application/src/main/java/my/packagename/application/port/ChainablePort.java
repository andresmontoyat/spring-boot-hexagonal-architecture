package my.packagename.application.port;

/**
 * A decorator link for any port (cache, metrics, logging). Deliberately free of Spring's {@code
 * Ordered}: adapters declare their position with {@code @Order} and bootstrap sorts the chain.
 */
public interface ChainablePort<T> {

  void setDelegate(T delegate);
}
