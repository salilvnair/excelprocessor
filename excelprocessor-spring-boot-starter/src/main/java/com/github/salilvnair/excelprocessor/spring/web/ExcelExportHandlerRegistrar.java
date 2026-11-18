package com.github.salilvnair.excelprocessor.spring.web;

import com.github.salilvnair.excelprocessor.spring.ExcelProcessorTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Puts {@link ExcelExportReturnValueHandler} in front of Spring MVC's return value handlers.
 * Handlers added through {@code WebMvcConfigurer} run after {@code @ResponseBody} handling, which
 * claims every return type of a {@code @RestController}.
 */
public class ExcelExportHandlerRegistrar implements BeanPostProcessor {
    private final ObjectProvider<ExcelProcessorTemplate> template;

    public ExcelExportHandlerRegistrar(ObjectProvider<ExcelProcessorTemplate> template) {
        this.template = template;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RequestMappingHandlerAdapter) {
            RequestMappingHandlerAdapter adapter = (RequestMappingHandlerAdapter) bean;
            List<HandlerMethodReturnValueHandler> current = adapter.getReturnValueHandlers();
            if (current != null && current.stream().noneMatch(ExcelExportReturnValueHandler.class::isInstance)) {
                List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>(current.size() + 1);
                handlers.add(new ExcelExportReturnValueHandler(template.getObject()));
                handlers.addAll(current);
                adapter.setReturnValueHandlers(handlers);
            }
        }
        return bean;
    }
}
