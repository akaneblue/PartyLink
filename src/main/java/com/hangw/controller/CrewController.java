package com.hangw.controller;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.hangw.model.CrewCreateForm;
import com.hangw.model.Crew_members;
import com.hangw.model.Crews;
import com.hangw.model.UserRole;
import com.hangw.model.Users;
import com.hangw.service.AICategoryService;
import com.hangw.service.CrewMemberService;
import com.hangw.service.CrewService;
import com.hangw.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class CrewController {

	private final CrewService groupService;
	private final UserService userService;
	private final CrewMemberService cmService;
	private final AICategoryService aiService;

	@GetMapping("")
	public String groupList(@RequestParam(defaultValue = "0") int page,
	                        @RequestParam(value = "category", required = false) String category,
	                        Model model,
	                        Principal principal) {

	    if (principal != null) {
	        String username = principal.getName();
	        Users user = userService.getUser(username);
	        model.addAttribute("user", user);
	        model.addAttribute("Login", true);
	    } else {
	        model.addAttribute("Login", false);
	    }

	    List<Crews> crewsList;

	    if (category != null && !category.isEmpty()) {
	        crewsList = groupService.getCrewsByCategory(category, page, 15);
	        model.addAttribute("selectedCategory", category);
	    } else {
	        crewsList = groupService.getCrews(page, 15);
	    }

	    model.addAttribute("crewsList", crewsList);
	    model.addAttribute("currentPage", page);
	    return "list";
	}


	@GetMapping("/create")
	@PreAuthorize("isAuthenticated")
	public String groupCreate(CrewCreateForm crewCreateForm) {
		return "create";
	}

	@PostMapping("/create")
	public String groupCreate(@Valid CrewCreateForm crewCreateForm, BindingResult bindingResult,
			@RequestParam MultipartFile imageFile, Principal principal) {
		if (bindingResult.hasErrors())
			return "create";
		try {
			String category = aiService.getCategoryFromAI(crewCreateForm.getDescription());
			crewCreateForm.setCategory(category);
			
			String imagePath = null;
			if (imageFile != null && !imageFile.isEmpty()) {
				String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
				String uploadDir = System.getProperty("user.dir") + "/uploads/";
				File uploadPath = new File(uploadDir);

				// 디렉토리 없으면 생성
				if (!uploadPath.exists()) {
					uploadPath.mkdirs();
				}

				File saveFile = new File(uploadDir, fileName);
				System.out.println("파일 저장 경로: " + saveFile.getAbsolutePath());
				
				imageFile.transferTo(saveFile);
				imagePath = "/uploads/" + fileName;
			}

			Crews crew = groupService.create(crewCreateForm.getName(), crewCreateForm.getMaxParticipants(),
					crewCreateForm.getDescription(), crewCreateForm.getCategory(), crewCreateForm.getDate(), crewCreateForm.getDate2(), crewCreateForm.getTime(),
					crewCreateForm.getLocation(), userService.getUser(principal.getName()), imagePath);
			cmService.create(crew.getId(), crew.getLeader().getId(), UserRole.Leader);
		} catch (DataIntegrityViolationException e) {
			e.printStackTrace();
			bindingResult.reject("signupFailed", "이미 존재하는 이름입니다.");
			return "create";
		} catch (IOException e) {
			e.printStackTrace();
			bindingResult.reject("imageUploadFailed", "이미지 업로드 중 오류가 발생했습니다.");
			return "create";
		} catch (Exception e) {
		    e.printStackTrace();
		    bindingResult.reject("imageUploadFailed", "알 수 없는 오류가 발생했습니다.");
		    return "create";
		}

		return "redirect:/";
	}

	@GetMapping("/detail")
	public String crewDetail(@RequestParam(value = "id") Long crewId, Model model) {
		Crews crew = groupService.getCrewById(crewId);
		List<Crew_members> members = cmService.findMembers(crewId);
		model.addAttribute("crew", crew);
		model.addAttribute("leader", crew.getLeader());
		model.addAttribute("members", members);
		return "detail";
	}
	
	@GetMapping("/mycrew")
	@PreAuthorize("isAuthenticated")
	public String myCrew(Principal principal, Model model) {
		Users user =userService.getUser(principal.getName());
		List<Crews> crews = cmService.findGroupByuser(user);
		List<Crews> made = groupService.getCrewByLeader(user);
		model.addAttribute("crews", crews);
		model.addAttribute("made", made);
		return "mycrew";
	}
}
