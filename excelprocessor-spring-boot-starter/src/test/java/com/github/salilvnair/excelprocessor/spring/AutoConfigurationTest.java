package com.github.salilvnair.excelprocessor.spring;

import com.github.salilvnair.excelprocessor.spring.web.ExcelExportHandlerRegistrar;
import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.*;

class AutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ExcelProcessorAutoConfiguration.class));

    @Configuration(proxyBeanMethods = false)
    static class TaskBeans {
        @Bean
        String orderTasks() {
            return "the order task bean";
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class OwnReader {
        @Bean
        ExcelSheetReader myReader() {
            return ExcelSheetReaderFactory.generate(true, 50);
        }
    }

    @Test
    void registersBeansAndResolvesTaskBeansFromTheContext() {
        runner.withUserConfiguration(TaskBeans.class).run(context -> {
            assertNotNull(context.getBean(ExcelSheetReader.class));
            assertNotNull(context.getBean(ExcelSheetWriter.class));
            ExcelProcessorTemplate template = context.getBean(ExcelProcessorTemplate.class);
            ExcelSheetContext sheetContext = template.context();
            assertEquals("the order task bean", sheetContext.beanFunction().apply("orderTasks"));
            assertNull(sheetContext.beanFunction().apply("noSuchBean"), "unknown names fall back to the library");
            assertTrue(sheetContext.suppressTaskExceptions());
            assertFalse(context.containsBean("excelExportHandlerRegistrar"), "no MVC registration outside web apps");
        });
    }

    @Test
    void backsOffForApplicationBeansAndHonoursProperties() {
        runner.withUserConfiguration(OwnReader.class)
                .withPropertyValues("excelprocessor.fail-on-task-exceptions=true", "excelprocessor.resolve-beans=false")
                .run(context -> {
                    assertEquals(1, context.getBeansOfType(ExcelSheetReader.class).size());
                    assertTrue(context.containsBean("myReader"));
                    ExcelSheetContext sheetContext = context.getBean(ExcelProcessorTemplate.class).context();
                    assertFalse(sheetContext.suppressTaskExceptions());
                    assertNull(sheetContext.beanFunction());
                });
    }

    @Test
    void registersTheExportHandlerInServletApps() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ExcelProcessorAutoConfiguration.class))
                .run(context -> assertNotNull(context.getBean(ExcelExportHandlerRegistrar.class)));
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ExcelProcessorAutoConfiguration.class))
                .withPropertyValues("excelprocessor.web-exports=false")
                .run(context -> assertTrue(context.getBeansOfType(ExcelExportHandlerRegistrar.class).isEmpty()));
    }
}
