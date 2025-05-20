package com.hangw.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.hangw.model.Notification;
import com.hangw.model.Users;
import com.hangw.repository.NotificationRepository;
import com.hangw.service.UserService;

@ControllerAdvice
public class GlobalModelAttributeAdvice {

    @Autowired
    private NotificationRepository notificationService;

    @Autowired
    private UserService userService;

    @ModelAttribute
    public void addNotificationsToModel(Model model, Principal principal) {
        if (principal != null) {
            Users user = userService.getUser(principal.getName());
            List<Notification> notifications = notificationService.findByUser(user);
            long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();

            model.addAttribute("notifications", notifications);
            model.addAttribute("unreadCount", unreadCount);
        }
    }
}

