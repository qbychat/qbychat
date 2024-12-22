package org.qbynet.chat.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.vo.NotificationVO;
import org.qbynet.chat.service.NotificationService;
import org.qbynet.shared.entity.RestBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Log4j2
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    @Resource
    NotificationService notificationService;

    @GetMapping("fetch")
    public DeferredResult<ResponseEntity<RestBean<List<NotificationVO>>>> fetch(HttpServletRequest request, @RequestAttribute("user") User user) {
        DeferredResult<ResponseEntity<RestBean<List<NotificationVO>>>> result = new DeferredResult<>();
        executorService.submit(() -> {
            try {
                while (!notificationService.hasNotifications(user)) {
                    TimeUnit.SECONDS.sleep(2);
                }
                // push to client
                log.info("Push notifications to client!");
                result.setResult(ResponseEntity.ok(RestBean.success(notificationService.fetch(user).stream().map(NotificationVO::from).toList())));
            } catch (InterruptedException ignored) {
                result.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestBean.failure(500, "Internal Server Error")));
            }
        });
        return result;
    }
}
