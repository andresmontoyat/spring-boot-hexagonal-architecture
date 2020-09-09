package my.packagename;

import my.packagename.domain.model.Custom;
import my.packagename.domain.service.CustomService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class BootApplicationTest {

    @Autowired
    private CustomService customService;

    @DisplayName("My custom test")
    @Test
    public void test() {
        Custom custom = new Custom();
        custom.setName("Demo");
        assertNotNull(customService.save(custom), "Error messages");
    }
}
