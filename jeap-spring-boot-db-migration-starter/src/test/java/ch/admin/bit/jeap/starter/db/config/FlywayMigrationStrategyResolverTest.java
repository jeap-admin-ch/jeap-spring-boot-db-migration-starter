package ch.admin.bit.jeap.starter.db.config;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlywayMigrationStrategyResolverTest {

    @Mock
    private Flyway flyway;
    @Mock
    private ApplicationContext ctx;
    @Mock
    private Environment environment;
    @Mock
    private FlywayMigrationStrategyService flywayMigrationStrategyService;
    @Mock
    private DatabaseMigrationProperties migrationProperties;
    @Mock
    private ShutdownService shutdownService;
    @InjectMocks
    private FlywayMigrationStrategyResolver resolver;

    @Test
    void test_WhenStartupMigrateModeIsEnabled_ThenExecuteStartupModeStrategy() {
        when(environment.getProperty("spring.main.cloud-platform")).thenReturn("CLOUD_FOUNDRY");

        resolver.resolveFlywayStrategy(ctx, environment, flyway);

        verify(flywayMigrationStrategyService).executeStartupModeStrategy(flyway);
        verifyNoInteractions(shutdownService);
    }

    @Test
    void test_WhenPlatformIsKubernetesModeIsEnabled_ThenExecuteStartupModeStrategy() {
        when(environment.getProperty("spring.main.cloud-platform")).thenReturn("KUBERNETES");
        when(migrationProperties.isStartupMigrateModeEnabled()).thenReturn(true);
        when(migrationProperties.isInitContainer()).thenReturn(false);

        resolver.resolveFlywayStrategy(ctx, environment, flyway);

        verify(flywayMigrationStrategyService).executeStartupModeStrategy(flyway);
        verifyNoInteractions(shutdownService);
    }

    @Test
    void test_WhenStartupMigrateModeIsNotEnabled_ThenExecuteInitContainerStrategy() {
        when(environment.getProperty("spring.main.cloud-platform")).thenReturn("KUBERNETES");
        when(migrationProperties.isStartupMigrateModeEnabled()).thenReturn(false);
        when(migrationProperties.isInitContainer()).thenReturn(true);

        resolver.resolveFlywayStrategy(ctx, environment, flyway);

        verify(flywayMigrationStrategyService).executeInitContainerStrategy(flyway);
        verify(shutdownService).shutdown(any(ApplicationContext.class), eq(0));
    }

    @Test
    void test_WhenStartupMigrateModeIsNotEnabled_ThenExecuteApplicationContainerStrategy() {
        when(environment.getProperty("spring.main.cloud-platform")).thenReturn("KUBERNETES");
        when(migrationProperties.isStartupMigrateModeEnabled()).thenReturn(false);
        when(migrationProperties.isInitContainer()).thenReturn(false);

        resolver.resolveFlywayStrategy(ctx, environment, flyway);

        verify(flywayMigrationStrategyService).executeApplicationContainerStrategy(ctx, flyway);
        verifyNoInteractions(shutdownService);
    }

    @Test
    void test_rethrowExceptionIfExceptionIsThrown_whenStartupMigrateModeEnabled() {
        when(environment.getProperty("spring.main.cloud-platform")).thenReturn("KUBERNETES");
        when(migrationProperties.isStartupMigrateModeEnabled()).thenReturn(true);
        when(migrationProperties.isInitContainer()).thenReturn(false);

        doThrow(new RuntimeException("Uiui")).when(flywayMigrationStrategyService).executeStartupModeStrategy(flyway);

        assertThrows(RuntimeException.class, () -> resolver.resolveFlywayStrategy(ctx, environment, flyway));
        verifyNoInteractions(shutdownService);
    }

    @Test
    void test_shutdownIfExceptionIsThrown_whenInitContainer() {
        when(environment.getProperty("spring.main.cloud-platform")).thenReturn("KUBERNETES");
        when(migrationProperties.isStartupMigrateModeEnabled()).thenReturn(false);
        when(migrationProperties.isInitContainer()).thenReturn(true);

        doThrow(new RuntimeException("Uiui")).when(flywayMigrationStrategyService).executeInitContainerStrategy(flyway);

        resolver.resolveFlywayStrategy(ctx, environment, flyway);

        verify(shutdownService).shutdown(any(ApplicationContext.class), eq(1));
    }
}
