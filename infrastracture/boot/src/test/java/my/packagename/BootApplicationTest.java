package my.packagename;

import my.packagename.service.CustomService;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BootApplicationTest {

    @Autowired
    private CustomService customService;

    @DisplayName("My custom test")
    @Test
    public void test() {
        assertNotNull(customService.defaultServiceMethod(), "My custom message assert");
    }
}
