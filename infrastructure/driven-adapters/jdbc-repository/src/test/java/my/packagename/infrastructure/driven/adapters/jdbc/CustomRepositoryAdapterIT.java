package my.packagename.infrastructure.driven.adapters.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import my.packagename.application.query.PageQuery;
import my.packagename.application.query.PageResult;
import my.packagename.domain.exception.CustomConcurrentModificationException;
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

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostgresContainerConfiguration.class, CustomRepositoryAdapter.class})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CustomRepositoryAdapterIT {

  private static final CustomName NAME = new CustomName("Demo");

  @Autowired private CustomRepositoryAdapter adapter;

  @Test
  void should_find_custom_when_it_was_inserted() {
    // Given
    Custom inserted = adapter.insert(Custom.create(newId(), NAME));

    // When / Then
    assertThat(adapter.findById(inserted.id()))
        .hasValueSatisfying(found -> assertThat(found.name()).isEqualTo(NAME));
  }

  @Test
  void should_bump_version_when_updated() {
    // Given
    Custom inserted = adapter.insert(Custom.create(newId(), NAME));

    // When
    Custom updated = adapter.update(inserted.rename(new CustomName("Renamed")));

    // Then
    assertThat(updated.version()).isEqualTo(inserted.version() + 1);
    assertThat(adapter.findById(inserted.id()))
        .hasValueSatisfying(found -> assertThat(found.name().value()).isEqualTo("Renamed"));
  }

  @Test
  void should_reject_update_when_version_is_stale() {
    // Given
    Custom inserted = adapter.insert(Custom.create(newId(), NAME));
    adapter.update(inserted.rename(new CustomName("First")));

    // When / Then
    assertThatThrownBy(() -> adapter.update(inserted.rename(new CustomName("Second"))))
        .isInstanceOf(CustomConcurrentModificationException.class);
  }

  @Test
  void should_return_empty_when_id_is_unknown() {
    assertThat(adapter.findById(newId())).isEmpty();
  }

  @Test
  void should_report_deletion_only_when_row_existed() {
    // Given
    Custom inserted = adapter.insert(Custom.create(newId(), NAME));

    // When / Then
    assertThat(adapter.deleteById(inserted.id())).isTrue();
    assertThat(adapter.deleteById(inserted.id())).isFalse();
  }

  @Test
  void should_page_results_ordered_by_name_when_listing() {
    // Given
    adapter.insert(Custom.create(newId(), new CustomName("Charlie")));
    adapter.insert(Custom.create(newId(), new CustomName("Alpha")));
    adapter.insert(Custom.create(newId(), new CustomName("Bravo")));

    // When
    PageResult<Custom> page = adapter.findAll(new PageQuery(0, 2));

    // Then
    assertThat(page.items()).extracting(c -> c.name().value()).containsExactly("Alpha", "Bravo");
    assertThat(page.totalElements()).isEqualTo(3);
  }

  private static CustomId newId() {
    return new CustomId(UUID.randomUUID());
  }
}
