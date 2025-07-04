package com.hangw.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hangw.model.Applicants;
import com.hangw.model.ApplicationForm;
import com.hangw.model.Crews;
import com.hangw.model.Gender;
import com.hangw.model.Requirements;
import com.hangw.model.Status;
import com.hangw.model.Users;
import com.hangw.model.DTO.ApplicantDto;
import com.hangw.service.AppliService;
import com.hangw.service.CrewMemberService;
import com.hangw.service.CrewService;
import com.hangw.service.RequirementService;
import com.hangw.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("api/appli")
@RequiredArgsConstructor
public class AppliController {
	private final AppliService appService;
	private final CrewService crewService;
	private final UserService userService;
	private final CrewMemberService cmService;
	private final RequirementService reqService;

	@GetMapping("")
	@PreAuthorize("isAuthenticated")
	public ResponseEntity<?> list(Principal principal) {
		Map<String, Object> response = new HashMap<>();
		List<Applicants> sent = appService.getAppByUser(userService.getUser(principal.getName()).getId());

		Map<String, List<ApplicantDto>> groupedApplications = new HashMap<>();
		crewService.getCrewByLeader(userService.getUser(principal.getName())).forEach(crew -> {
			List<ApplicantDto> apps = appService.GetAppByCrew(crew.getId()).stream()
					.filter(app -> app.getStatus() == Status.Processing).collect(Collectors.toList()).stream()
					.map(ApplicantDto::from).toList();
			groupedApplications.put(crew.getName(), apps);
		});
		List<ApplicantDto> sentDto = sent.stream().map(ApplicantDto::from).toList();

		response.put("groupedApps", groupedApplications);
		response.put("sent", sentDto);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/create")
	@PreAuthorize("isAuthenticated")
	public ResponseEntity<?> createApplication(@Valid @ModelAttribute ApplicationForm appForm,
			@RequestParam Long groupId, Principal principal) {
		Users user = userService.getUser(principal.getName());
		Long userId = user.getId();

		// 1. 조건 검증
		if (cmService.existsByCrewIdAndUserId(groupId, userId)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "이미 해당 모임의 멤버 입니다."));
		} else if (appService.existsByGroupIdAndUserIdAndStatusProcessing(groupId, userId)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "이미 이 모임에 신청서를 제출하셨습니다."));
		}

		Requirements req = reqService.findByCrew(crewService.getCrewById(groupId));
		if (req != null) {
			if (req.getLocation() != null && !req.getLocation().isBlank()) {
				if (user.getLocation() == null || !user.getLocation().contains(req.getLocation())) {
					return ResponseEntity.status(HttpStatus.FORBIDDEN)
							.body(Map.of("message", "이 모임은 " + req.getLocation() + " 지역의 사용자만 신청할 수 있습니다."));
				}
			}

			if (req.getGender() != null && req.getGender() != Gender.ANY) {
				if (user.getGender() == null || user.getGender() != req.getGender()) {
					return ResponseEntity.status(HttpStatus.FORBIDDEN)
							.body(Map.of("message", "이 모임은 " + req.getGender().name() + "만 신청할 수 있습니다."));
				}
			}

			if (user.getBirth() != null) {
				int userAge = Period.between(user.getBirth(), LocalDate.now()).getYears();
				if (req.getMinAge() != null && userAge < req.getMinAge()) {
					return ResponseEntity.status(HttpStatus.FORBIDDEN)
							.body(Map.of("message", "이 모임은 " + req.getMinAge() + "세 이상만 신청할 수 있습니다."));
				}
				if (req.getMaxAge() != null && userAge > req.getMaxAge()) {
					return ResponseEntity.status(HttpStatus.FORBIDDEN)
							.body(Map.of("message", "이 모임은 " + req.getMaxAge() + "세 이하만 신청할 수 있습니다."));
				}
			} else {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body(Map.of("message", "나이 정보가 없어 신청할 수 없습니다. 프로필을 먼저 수정해 주세요."));
			}
		}

		// 2. 신청 저장
		appService.create(groupId, userId, appForm.getIntro());
		return ResponseEntity.ok(Map.of("message", "신청이 완료되었습니다."));
	}

	@PostMapping("/accept")
	public ResponseEntity<?> acceptApplication(@RequestParam Long appId, Principal principal) {
		Applicants app = appService.GetAppById(appId);

		if (app == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "신청 정보를 찾을 수 없습니다."));
		}

		if (!app.getGroup().getLeader().getEmail().equals(principal.getName())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "승인 권한이 없습니다."));
		}

		app.setStatus(Status.Accepted);
		appService.save(app);

		Crews group = app.getGroup();
		group.setCur_members(group.getCur_members() + 1);
		crewService.save(group);

		cmService.create(group.getId(), app.getUser().getId());

		return ResponseEntity.ok(Map.of("message", "신청을 승인했습니다."));
	}

	@PostMapping("/reject")
	public ResponseEntity<?> rejectApplication(@RequestParam Long appId, Principal principal) {
		Applicants app = appService.GetAppById(appId);

		if (app == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "신청 정보를 찾을 수 없습니다."));
		}

		if (!app.getGroup().getLeader().getEmail().equals(principal.getName())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "거절 권한이 없습니다."));
		}

		app.setStatus(Status.Rejected);
		appService.save(app);

		return ResponseEntity.ok(Map.of("message", "신청을 거절했습니다."));
	}

	@PostMapping("/cancel")
	public ResponseEntity<?> cancelApplication(@RequestParam Long appId, Principal principal) {
		Applicants app = appService.GetAppById(appId);

		if (app == null || !app.getUser().getEmail().equals(principal.getName())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "신청을 취소할 수 없습니다."));
		}

		appService.deleteById(appId);
		return ResponseEntity.ok(Map.of("message", "신청을 취소했습니다."));
	}

}

//thymeleaf controller
//@Controller
//@RequestMapping("/appli")
//@RequiredArgsConstructor
//public class AppliController {
//	private final AppliService appService;
//	private final CrewService crewService;
//	private final UserService userService;
//	private final CrewMemberService cmService;
//	private final RequirementService reqService;
//	
//	@GetMapping("")
//	@PreAuthorize("isAuthenticated")
//	public String list(Principal principal,Model model) {
//		List<Applicants> sent = appService.getAppByUser(userService.getUser(principal.getName()).getId());
//		
//		Map<String, List<Applicants>> groupedApplications = new HashMap<>();
//		crewService.getCrewByLeader(userService.getUser(principal.getName()))
//	    .forEach(crew -> {
//	        List<Applicants> apps = appService.GetAppByCrew(crew.getId()).stream()
//	            .filter(app -> app.getStatus() == Status.Processing)
//	            .collect(Collectors.toList());
//	        groupedApplications.put(crew.getName(), apps);
//	    });
//
//		model.addAttribute("groupedApps", groupedApplications);
//		model.addAttribute("sent", sent);
//		
//		return "applist";
//	}
//	
//	@GetMapping("/create")
//	@PreAuthorize("isAuthenticated")
//	public String create(@ModelAttribute ApplicationForm applicationform,
//	                     @RequestParam Long groupId,
//	                     Model model,
//	                     Principal principal,
//	                     HttpServletRequest request,
//	                     RedirectAttributes redirectAttributes) {
//
//	    Long userId = userService.getUser(principal.getName()).getId();
//	    Users user = userService.findUserById(userId);
//	    
//	    String referer = request.getHeader("Referer");
//	    if (referer == null) referer = "/";
//
//	    if (cmService.existsByCrewIdAndUserId(groupId, userId)) {
//	        redirectAttributes.addFlashAttribute("errorMessage", "이미 해당 모임의 멤버입니다.");
//	        return "redirect:" + referer;
//	    } else if (appService.existsByGroupIdAndUserIdAndStatusProcessing(groupId, userId)) {
//	        redirectAttributes.addFlashAttribute("errorMessage", "이미 이 모임에 신청서를 제출하셨습니다.");
//	        return "redirect:" + referer;
//	    }
//
//	    Requirements req = reqService.findByCrew(crewService.getCrewById(groupId));
//	    if (req != null) {
//	        // 3. 지역 조건 검사
//	        if (req.getLocation() != null && !req.getLocation().isBlank()) {
//	            if (user.getLocation() == null || !user.getLocation().contains(req.getLocation())) {
//	                redirectAttributes.addFlashAttribute("errorMessage", "이 모임은 " + req.getLocation() + " 지역의 사용자만 신청할 수 있습니다.");
//	                return "redirect:" + referer;
//	            }
//	        }
//
//	        // 4. 성별 조건 검사
//	        if (req.getGender() != null && req.getGender() != Gender.ANY) {
//	            if (user.getGender() == null || user.getGender() != req.getGender()) {
//	                redirectAttributes.addFlashAttribute("errorMessage", "이 모임은 " + req.getGender().name() + "만 신청할 수 있습니다.");
//	                return "redirect:" + referer;
//	            }
//	        }
//
//	        // 5. 나이 조건 검사
//	        if (user.getBirth() != null) {
//	            int userAge = Period.between(user.getBirth(), LocalDate.now()).getYears();
//
//	            if (req.getMinAge() != null && userAge < req.getMinAge()) {
//	                redirectAttributes.addFlashAttribute("errorMessage", "이 모임은 " + req.getMinAge() + "세 이상만 신청할 수 있습니다.");
//	                return "redirect:" + referer;
//	            }
//	            if (req.getMaxAge() != null && userAge > req.getMaxAge()) {
//	                redirectAttributes.addFlashAttribute("errorMessage", "이 모임은 " + req.getMaxAge() + "세 이하만 신청할 수 있습니다.");
//	                return "redirect:" + referer;
//	            }
//	        } else {
//	            // 생년월일이 없는 사용자도 막을 수 있음
//	            redirectAttributes.addFlashAttribute("errorMessage", "나이 정보가 없어 신청할 수 없습니다. 프로필을 먼저 수정해 주세요.");
//	            return "redirect:" + referer;
//	        }
//	    }
//	    model.addAttribute("groupId", groupId);
//	    return "applicant";
//	}
//
//
//	
//	@PostMapping("/create")
//	public String submitApplication(@ModelAttribute ApplicationForm applicationForm, Principal principal) {
//	    appService.create(applicationForm.getCrew(), userService.getUser(principal.getName()).getId(), applicationForm.getIntro());
//	    return "redirect:/";
//	}
//
//	@GetMapping("/accept")
//	public String acceptApplication(@RequestParam Long appId, Principal principal) {
//	    Applicants app = appService.GetAppById(appId);
//
//	    if (app == null) {
//	        return "redirect:/error"; // 혹은 에러 페이지로
//	    }
//
//	    if (!app.getGroup().getLeader().getEmail().equals(principal.getName())) {
//	        return "redirect:/access-denied"; // 또는 403 에러 처리
//	    }
//
//	    app.setStatus(Status.Accepted);
//	    appService.save(app); // Applicants 저장
//
//	    Crews group = app.getGroup();
//	    group.setCur_members(group.getCur_members() + 1);
//	    crewService.save(group); // 모임 정보 저장
//
//	    cmService.create(group.getId(), app.getUser().getId());
//	    
//	    return "redirect:/appli";
//	}
//
//	@GetMapping("/reject")
//	public String rejectApplication(@RequestParam Long appId, Principal principal) {
//		Applicants app = appService.GetAppById(appId);
//		
//		if (app == null) {
//	        return "redirect:/error"; // 혹은 에러 페이지로
//	    }
//
//	    if (!app.getGroup().getLeader().getEmail().equals(principal.getName())) {
//	        return "redirect:/access-denied"; // 또는 403 에러 처리
//	    }
//	    app.setStatus(Status.Rejected);
//	    appService.save(app);
//	    
//	    return "redirect:/appli";
//	}
//	
//	@GetMapping("/cancel")
//	public String cancelApplication(@RequestParam Long appId) {
//		appService.deleteById(appId);
//		return "redirect:/appli";
//	}
//}
