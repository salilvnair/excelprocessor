package com.github.salilvnair.excelprocessor.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code excelprocessor.*} settings.
 */
@ConfigurationProperties(prefix = "excelprocessor")
public class ExcelProcessorProperties {

    /** Resolve {@code excelTaskBeanName}/{@code excelTaskValidatorBeanName} and task beans from the Spring context. */
    private boolean resolveBeans = true;

    /** Let task and validator exceptions propagate instead of being logged and skipped. */
    private boolean failOnTaskExceptions = false;

    /** Register the {@link ExcelExport} controller return type (Spring MVC applications only). */
    private boolean webExports = true;

    public boolean isResolveBeans() {
        return resolveBeans;
    }

    public void setResolveBeans(boolean resolveBeans) {
        this.resolveBeans = resolveBeans;
    }

    public boolean isFailOnTaskExceptions() {
        return failOnTaskExceptions;
    }

    public void setFailOnTaskExceptions(boolean failOnTaskExceptions) {
        this.failOnTaskExceptions = failOnTaskExceptions;
    }

    public boolean isWebExports() {
        return webExports;
    }

    public void setWebExports(boolean webExports) {
        this.webExports = webExports;
    }
}
