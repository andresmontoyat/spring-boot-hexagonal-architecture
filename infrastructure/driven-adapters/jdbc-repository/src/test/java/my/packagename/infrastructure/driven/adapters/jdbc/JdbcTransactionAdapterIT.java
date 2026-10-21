package my.packagename.infrastructure.driven.adapters.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import my.packagename.domain.model.Custom;
import my.packagename.domain.model.CustomId;
import my.packagename.domain.model.CustomName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
  PostgresContainerConfiguration.class,
  CustomRepositoryAdapter.class,
  JdbcTransactionAdapter.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class JdbcTransactionAdapterIT {

  @Autowired private JdbcTransactionAdapter<Custom> transaction;
  @Autowired private CustomRepositoryAdapter repository;

  @Test
  void should_roll_back_every_write_when_callback_fails() {
    // Given
    CustomId id = new CustomId(UUID.randomUUID());

    // When
    assertThatThrownBy(
            () ->
                transaction.run(
                    () -> {
                      repository.insert(Custom.create(id, new CustomName("Doomed")));
                      throw new IllegalStateException("boom");
                    }))
        .isInstanceOf(IllegalStateException.class);

    // Then
    assertThat(repository.findById(id)).isEmpty();
  }

  @Test
  void should_commit_when_callback_succeeds() {
    // Given
    CustomId id = new CustomId(UUID.randomUUID());

    // When
    transaction.run(() -> repository.insert(Custom.create(id, new CustomName("Kept"))));

    // Then
    assertThat(repository.findById(id)).isPresent();
    repository.deleteById(id);
  }
}
