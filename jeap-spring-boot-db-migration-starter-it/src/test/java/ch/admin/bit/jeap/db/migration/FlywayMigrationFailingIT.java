package ch.admin.bit.jeap.db.migration;

import ch.admin.bit.jeap.starter.db.config.ShutdownService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = TestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.main.cloud-platform=KUBERNETES",
        "database-migration.init-container=true",
        "database-migration.startup-migrate-mode-enabled=false",
        "spring.flyway.locations=classpath:db/migration-failing"
})
class FlywayMigrationFailingIT extends PostgresTestContainerBase {

    @MockitoBean
    private ShutdownService shutdownService;


    @Test
    void thenShutdownIsCalledWithErrorCode() {
        verify(shutdownService).shutdown(any(ApplicationContext.class), eq(1));
    }
}
