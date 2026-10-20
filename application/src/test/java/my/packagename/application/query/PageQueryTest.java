package my.packagename.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PageQueryTest {

  @ParameterizedTest
  @CsvSource({"-1, 20", "0, 0", "0, 101"})
  void should_reject_query_when_bounds_are_invalid(int page, int size) {
    assertThatThrownBy(() -> new PageQuery(page, size))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_compute_zero_pages_when_result_is_empty() {
    assertThat(new PageResult<>(List.of(), 0, 20, 0).totalPages()).isZero();
  }

  @Test
  void should_round_total_pages_up_when_last_page_is_partial() {
    assertThat(new PageResult<>(List.of(), 0, 20, 41).totalPages()).isEqualTo(3);
  }

  @Test
  void should_copy_items_defensively_when_created() {
    List<String> items = new ArrayList<>(List.of("a"));

    PageResult<String> result = new PageResult<>(items, 0, 20, 1);
    items.add("b");

    assertThat(result.items()).containsExactly("a");
  }
}
