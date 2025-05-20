package com.hangw.controller;

import java.security.Principal;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.hangw.service.UserService;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserService userService;

    public GlobalControllerAdvice(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("loginUserId")
    public Long loginUserId(Principal principal) {
        if (principal != null) {
            return userService.getUser(principal.getName()).getId();
        }
        return null;
    }
}