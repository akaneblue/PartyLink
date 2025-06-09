package com.hangw.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hangw.model.Notification;
import com.hangw.model.Users;
import com.hangw.model.DTO.NotificationDto;
import com.hangw.repository.NotificationRepository;
import com.hangw.service.UserService;

//thymeleaf용 controller
//@ControllerAdvice
//public class GlobalControllerAdvice {
//
//    private final UserService userService;
//
//    public GlobalControllerAdvice(UserService userService) {
//        this.userService = userService;
//    }
//
//    @ModelAttribute("loginUserId")
//    public Long loginUserId(Principal principal) {
//        if (principal != null) {
//            return userService.getUser(principal.getName()).getId();
//        }
//        return null;
//    }
//}

@RestController
@RequestMapping("/api")
public class NotificationApiController {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public NotificationApiController(NotificationRepository notificationRepository, UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        Users user = userService.getUser(principal.getName());

        List<Notification> notifications = notificationRepository.findByUser(user);

        List<NotificationDto> response = notifications.stream().map(n ->
            new NotificationDto(
                n.getId(),
                n.getMessage(),
                n.getCreatedAt(),
                n.isRead(),
                n.getCrew().getId(),
                n.getCrew().getName()
            )
        ).toList();

        return ResponseEntity.ok(response);
    }
}
