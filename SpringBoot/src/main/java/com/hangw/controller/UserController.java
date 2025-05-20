package com.hangw.controller;

import java.io.File;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hangw.model.Crews;
import com.hangw.model.Interest;
import com.hangw.model.UserCreateForm;
import com.hangw.model.UserReviews;
import com.hangw.model.Users;
import com.hangw.repository.InterestRepository;
import com.hangw.service.CrewMemberService;
import com.hangw.service.CrewService;
import com.hangw.service.UserReviewService;
import com.hangw.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;
	private final CrewMemberService cmService;
	private final CrewService crewService;
	private final UserReviewService urService;
	private final InterestRepository intRepository;
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
		List<Interest> interest = intRepository.findAll();
		Users user = userService.getUser(email);
		model.addAttribute("user", user);
		model.addAttribute("interests",interest);
		return "userpage";
	}
	
	@PostMapping("/mypage")
	@PreAuthorize("isAuthenticated")
	public String updateProfile(@ModelAttribute Users userForm,
	                            @RequestParam(value = "interestIds", required = false) List<Long> interestIds,
	                            Principal principal) {
	    Users user = userService.getUser(principal.getName());

	    // 기본 필드 업데이트
	    user.setName(userForm.getName());
	    user.setNickname(userForm.getNickname());
	    user.setDescription(userForm.getDescription());
	    user.setLocation(userForm.getLocation());
	    user.setBirth(userForm.getBirth());
	    user.setGender(userForm.getGender());

	    // 관심사 설정
	    if (interestIds != null) {
	        List<Interest> selectedInterests = intRepository.findAllById(interestIds);
	        user.setInterest(selectedInterests);
	    } else {
	        user.setInterest(new ArrayList<>()); // 전부 해제된 경우
	    }

	    userService.save(user);
	    return "redirect:/"; // 저장 후 홈 또는 마이페이지로 리다이렉트
	}
	
	@GetMapping("/userdetail")
	public String userDetail(@RequestParam Long userId,
	                         @RequestParam(defaultValue = "0") int hostedPage,
	                         @RequestParam(defaultValue = "0") int reviewPage,
	                         Model model, Principal principal) {
	    Users user = userService.findUserById(userId);

	    Page<Crews> hosted = crewService.getCrewsByLeader(user, hostedPage); // 주최한 모임
	    Page<UserReviews> review = urService.findReviewByUser(user, reviewPage); // 리뷰

	    model.addAttribute("user", user);
	    model.addAttribute("hostedMeetings", hosted);
	    model.addAttribute("reviews", review);
	    model.addAttribute("hostedPage", hostedPage);
	    model.addAttribute("reviewPage", reviewPage);


	    return "userdetail";
	}
	
	@GetMapping("/uploadPopup")
	@PreAuthorize("isAuthenticated()")
	public String uploadPopup() {
	    return "uploadpopup";
	}

	@PostMapping("/uploadProfile")
	@PreAuthorize("isAuthenticated()")
	public String uploadProfile(@RequestParam("profileImage") MultipartFile file,
	                            Principal principal,
	                            RedirectAttributes redirectAttributes) {
	    Users user = userService.getUser(principal.getName());
	    try {
	        String uploadDir = System.getProperty("user.dir") + "/uploads/profile/";
	        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

	        File dir = new File(uploadDir);
	        if (!dir.exists()) dir.mkdirs();

	        File saveFile = new File(uploadDir, fileName);
	        file.transferTo(saveFile);

	        // 웹 경로 저장
	        user.setImagePath("/uploads/profile/" + fileName);
	        userService.save(user);

	        redirectAttributes.addFlashAttribute("success", true);
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("error", true);
	    }
	    return "redirect:/user/uploadPopup";
	}



}
