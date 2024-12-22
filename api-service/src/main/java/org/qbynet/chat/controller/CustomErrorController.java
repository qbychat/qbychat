package org.qbynet.chat.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.qbynet.shared.entity.RestBean;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomErrorController implements ErrorController {
    @RequestMapping("error")
    public RestBean<?> error(HttpServletRequest request, HttpServletResponse response) {
        String errorMessage = getErrorMessage(request);
        return RestBean.failure(response.getStatus(), errorMessage);
    }

    private String getErrorMessage(@NotNull HttpServletRequest request) {
        String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        return StringUtils.hasText(errorMessage) ? errorMessage : "";
    }
}
