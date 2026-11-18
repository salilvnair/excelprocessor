package com.github.salilvnair.excelprocessor.spring;

import com.github.salilvnair.excelprocessor.spring.web.ExcelExportHandlerRegistrar;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetWriterFactory;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetWriter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the excelprocessor reader, writer and {@link ExcelProcessorTemplate}, a
 * {@link SpringBeanResolver}, and (in Spring MVC applications) the {@link ExcelExport} return type.
 * Every bean backs off when the application defines its own.
 */
@AutoConfiguration
@ConditionalOnClass(ExcelSheetReader.class)
@EnableConfigurationProperties(ExcelProcessorProperties.class)
public class ExcelProcessorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "excelProcessorBeanResolver")
    public SpringBeanResolver excelProcessorBeanResolver(BeanFactory beanFactory) {
        return new SpringBeanResolver(beanFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public ExcelProcessorTemplate excelProcessorTemplate(SpringBeanResolver excelProcessorBeanResolver, ExcelProcessorProperties properties) {
        return new ExcelProcessorTemplate(excelProcessorBeanResolver, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ExcelSheetReader excelSheetReader() {
        return ExcelSheetReaderFactory.generate();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExcelSheetWriter excelSheetWriter() {
        return ExcelSheetWriterFactory.generate();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(name = "org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter")
    @ConditionalOnBooleanProperty(name = "excelprocessor.web-exports", matchIfMissing = true)
    static class WebExportConfiguration {

        @Bean
        static ExcelExportHandlerRegistrar excelExportHandlerRegistrar(ObjectProvider<ExcelProcessorTemplate> template) {
            return new ExcelExportHandlerRegistrar(template);
        }
    }
}
