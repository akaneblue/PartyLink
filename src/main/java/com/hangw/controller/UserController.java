package com.hangw.controller;

import java.security.Principal;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hangw.model.UserCreateForm;
import com.hangw.model.Users;
import com.hangw.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	
	@GetMapping("/signup")
	public String signup(UserCreateForm userCreateForm) {
			return "signup";
	}
	
	@PostMapping("/signup") // user회원가입 처리
	public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "signup";
		}

		if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
			bindingResult.rejectValue("password2", "passwordInCorrect", "2개의 패스워드가 일치하지 않습니다.");
			return "signup";
		}

		try {
			userService.create(userCreateForm.getUserName(), userCreateForm.getEmail(), userCreateForm.getPassword1());
		} catch (DataIntegrityViolationException e) {
			e.printStackTrace();
			bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
			return "signup";
		} catch (Exception e) {
			e.printStackTrace();
			bindingResult.reject("signupFailed", e.getMessage());
			return "signup";
		}

		return "redirect:/";
	}
	
	@GetMapping("/login")
	public String login(HttpServletRequest request, Model model) {
		String referrer = request.getHeader("Referer");
		request.getSession().setAttribute("prevPage", referrer);
		return "login";
	}
	
	@GetMapping("/mypage")
	@PreAuthorize("isAuthenticated")
	public String mypage(Model model, Principal principal) {
		String email = principal.getName();
		Users user = userService.getUser(email);
		model.addAttribute("user", user);
		return "userpage";
	}
	
	@PostMapping("/mypage")
	@PreAuthorize("isAuthenticated")
	public String updateProfile(@ModelAttribute Users userForm, Principal principal) {
	    String email = principal.getName(); // 현재 로그인한 사용자 이메일
	    Users user = userService.getUser(email); // 현재 사용자 정보 가져오기

	    // 업데이트할 필드만 갱신
	    user.setName(userForm.getName());
	    user.setNickname(userForm.getNickname());
	    user.setDescription(userForm.getDescription());
	    user.setLocation(userForm.getLocation());
	    //user.setInterest(userForm.getInterest());

	    userService.save(user); // 수정된 내용 저장

	    return "redirect:/"; // 수정 후 다시 마이페이지로
	}
}
