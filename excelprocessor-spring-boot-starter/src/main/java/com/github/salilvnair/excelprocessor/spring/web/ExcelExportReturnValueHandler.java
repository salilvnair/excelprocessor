package com.github.salilvnair.excelprocessor.spring.web;

import com.github.salilvnair.excelprocessor.spring.ExcelExport;
import com.github.salilvnair.excelprocessor.spring.ExcelProcessorTemplate;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.nio.charset.StandardCharsets;

/**
 * Writes an {@link ExcelExport} returned by a controller method straight to the response with
 * download headers. Registered ahead of Spring's own handlers, so it also works in
 * {@code @RestController}s.
 */
public class ExcelExportReturnValueHandler implements HandlerMethodReturnValueHandler {
    private final ExcelProcessorTemplate template;

    public ExcelExportReturnValueHandler(ExcelProcessorTemplate template) {
        this.template = template;
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return ExcelExport.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        mavContainer.setRequestHandled(true);
        if (returnValue == null) {
            return;
        }
        ExcelExport<?> export = (ExcelExport<?>) returnValue;
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        if (response == null) {
            throw new IllegalStateException("ExcelExport needs a servlet response");
        }
        response.setContentType(export.contentType());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(export.fileName(), StandardCharsets.UTF_8).build().toString());
        export.writeTo(response.getOutputStream(), template.context());
    }
}
