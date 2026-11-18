package com.github.salilvnair.excelprocessor.spring;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.util.function.Function;

/**
 * The {@code beanResolver} excelprocessor uses for {@code excelTaskBeanName},
 * {@code excelTaskValidatorBeanName} and bean-named tasks: looks the name up in the Spring
 * context, returning null for unknown names so the library falls back to its own lookup.
 */
public class SpringBeanResolver implements Function<String, Object> {
    private final BeanFactory beanFactory;

    public SpringBeanResolver(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public Object apply(String beanName) {
        if (beanName == null || beanName.isEmpty()) {
            return null;
        }
        try {
            return beanFactory.getBean(beanName);
        }
        catch (NoSuchBeanDefinitionException e) {
            return null;
        }
    }
}
