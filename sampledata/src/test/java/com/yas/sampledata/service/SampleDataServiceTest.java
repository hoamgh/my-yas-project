package com.yas.sampledata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.yas.sampledata.utils.SqlScriptExecutor;
import com.yas.sampledata.viewmodel.SampleDataVm;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SampleDataServiceTest {

    @Mock
    private DataSource productDataSource;

    @Mock
    private DataSource mediaDataSource;

    private SampleDataService sampleDataService;

    @BeforeEach
    void setUp() {
        sampleDataService = new SampleDataService(productDataSource, mediaDataSource);
    }

    @Test
    void createSampleData_WhenCalled_ShouldReturnSuccessMessage() {
        try (MockedConstruction<SqlScriptExecutor> mocked = Mockito.mockConstruction(SqlScriptExecutor.class,
            (mock, context) -> {
                doNothing().when(mock).executeScriptsForSchema(any(DataSource.class), any(String.class), any(String.class));
            })) {

            SampleDataVm result = sampleDataService.createSampleData();

            assertNotNull(result);
            assertEquals("Insert Sample Data successfully!", result.message());

            SqlScriptExecutor executor = mocked.constructed().get(0);
            verify(executor, times(1)).executeScriptsForSchema(
                eq(productDataSource), eq("public"), eq("classpath*:db/product/*.sql"));
            verify(executor, times(1)).executeScriptsForSchema(
                eq(mediaDataSource), eq("public"), eq("classpath*:db/media/*.sql"));
        }
    }

    @Test
    void createSampleData_WhenCalled_ShouldExecuteProductScriptsFirst() {
        try (MockedConstruction<SqlScriptExecutor> mocked = Mockito.mockConstruction(SqlScriptExecutor.class,
            (mock, context) -> {
                doNothing().when(mock).executeScriptsForSchema(any(DataSource.class), any(String.class), any(String.class));
            })) {

            sampleDataService.createSampleData();

            SqlScriptExecutor executor = mocked.constructed().get(0);
            verify(executor).executeScriptsForSchema(productDataSource, "public", "classpath*:db/product/*.sql");
        }
    }

    @Test
    void createSampleData_WhenCalled_ShouldExecuteMediaScripts() {
        try (MockedConstruction<SqlScriptExecutor> mocked = Mockito.mockConstruction(SqlScriptExecutor.class,
            (mock, context) -> {
                doNothing().when(mock).executeScriptsForSchema(any(DataSource.class), any(String.class), any(String.class));
            })) {

            sampleDataService.createSampleData();

            SqlScriptExecutor executor = mocked.constructed().get(0);
            verify(executor).executeScriptsForSchema(mediaDataSource, "public", "classpath*:db/media/*.sql");
        }
    }
}
