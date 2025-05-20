package com.hangw.controller;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hangw.model.CrewCreateForm;
import com.hangw.model.Crew_members;
import com.hangw.model.Crews;
import com.hangw.model.Interest;
import com.hangw.model.Notice;
import com.hangw.model.Notification;
import com.hangw.model.Post;
import com.hangw.model.Schedule;
import com.hangw.model.UserRole;
import com.hangw.model.Users;
import com.hangw.repository.InterestRepository;
import com.hangw.repository.NotificationRepository;
import com.hangw.repository.PostRepository;
import com.hangw.service.AICategoryService;
import com.hangw.service.AICategoryService.AIKeywordResult;
import com.hangw.service.CrewMemberService;
import com.hangw.service.CrewService;
import com.hangw.service.NoticeService;
import com.hangw.service.RequirementService;
import com.hangw.service.ScheduleService;
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
	private final RequirementService reqService;
	private final InterestRepository intRepository;
	private final ScheduleService scheduleService;
	private final NoticeService noticeService;
	private final NotificationRepository notificationRepository;
	private final PostRepository postRepository;

	@GetMapping("")
	public String groupList(@RequestParam(defaultValue = "0") int page,
	                        @RequestParam(value = "category", required = false) String categoryName,
	                        @RequestParam(value = "keyword", required = false) String keyword,
	                        Model model,
	                        Principal principal) {

	    if (principal != null) {
	        Users user = userService.getUser(principal.getName());
	        model.addAttribute("pageuser", user);
	        model.addAttribute("Login", true);
	    } else {
	        model.addAttribute("Login", false);
	    }

	    List<Crews> crewsList;

	    // 🔍 검색 키워드가 우선 적용됨
	    if (keyword != null && !keyword.isBlank()) {
	        crewsList = groupService.searchCrewsByKeyword(keyword, page, 15);
	        model.addAttribute("keyword", keyword);
	    }
	    // 🔎 키워드 없고, 카테고리 필터 있을 경우
	    else if (categoryName != null && !categoryName.isBlank()) {
	        Optional<Interest> categoryOpt = intRepository.findByName(categoryName);
	        if (categoryOpt.isPresent()) {
	            Interest category = categoryOpt.get();
	            crewsList = groupService.getCrewsByCategory(category, page, 15);
	            model.addAttribute("selectedCategory", category);
	        } else {
	            // 존재하지 않는 카테고리면 전체 목록 보여주기
	            crewsList = groupService.getCrews(page, 15);
	        }
	    }
	    // 🔁 아무 필터도 없는 기본 조회
	    else {
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
			
			AIKeywordResult category = aiService.getCategoryFromAI(crewCreateForm.getDescription());

			Interest interest = intRepository.findByName(category.getKeyword())
			    .orElseGet(() -> {
			        Interest newInterest = new Interest();
			        newInterest.setName(category.getKeyword());
			        return intRepository.save(newInterest);  // 🔁 먼저 저장
			    });

			crewCreateForm.setCategory(interest);  // ✅ 영속 객체로 설정

			
			if(category.isNew()) {
				interest.setName(category.getKeyword());
				intRepository.save(interest);
			}
			
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

			if (crewCreateForm.getGender() != null || crewCreateForm.getMinAge() != null || crewCreateForm.getMaxAge() != null || crewCreateForm.getLocation() != null) {
				reqService.create(crew, crewCreateForm.getGender(), crewCreateForm.getMinAge(), crewCreateForm.getMaxAge(), crewCreateForm.getLocation());
			}
			
			// 1. 관심사로 유저 찾기
			List<Users> interestedUsers = userService.findByInterest(interest);

			// 2. 알림 생성 및 저장
			String message = "' 당신의 관심사 '" + interest.getName() + "'에 맞는 새로운 모임이 생성되었어요!";
			List<Notification> notifications = interestedUsers.stream()
			    .filter(u -> !u.getEmail().equals(principal.getName())) // 생성자 제외
			    .map(user -> {
			        Notification notification = new Notification();
			        notification.setUser(user);
			        notification.setCrew(crew);
			        notification.setMessage(message);
			        return notification;
			    })
			    .collect(Collectors.toList());

			notificationRepository.saveAll(notifications);
			
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
	public String crewDetail(@RequestParam(value = "id") Long crewId, Model model, Principal principal) {
	    Crews crew = groupService.getCrewById(crewId);
	    List<Crew_members> members = cmService.findMembers(crewId);

	    model.addAttribute("crew", crew);
	    model.addAttribute("leader", crew.getLeader());
	    model.addAttribute("members", members);

	    // ✅ 로그인 사용자에게만 알림 읽음 처리
	    if (principal != null) {
	        Users user = userService.getUser(principal.getName());
	        List<Notification> unread = notificationRepository
	            .findByUserAndCrewAndIsReadFalse(user, crew);

	        for (Notification n : unread) {
	            n.setRead(true);
	        }
	        notificationRepository.saveAll(unread);
	    }

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
	
	@GetMapping("/calendarList")
	@ResponseBody
	@PreAuthorize("isAuthenticated")
	public List<Map<String, Object>> getCalendarEvents(@RequestParam Long crewId) {
	    Crews crew = groupService.getCrewById(crewId);
	    List<Schedule> schedules = scheduleService.getSchedule(crew);

	    return schedules.stream().map(s -> {
	        Map<String, Object> event = new HashMap<>();
	        event.put("id", s.getId());
	        event.put("title", s.getTitle());

	        // LocalDate + LocalTime => ISO LocalDateTime string ("2024-06-01T14:00")
	        event.put("start", s.getDate().atTime(s.getSTime()).toString());
	        event.put("end", s.getDate().atTime(s.getETime()).toString());
	        event.put("allDay", false);
	        return event;
	    }).collect(Collectors.toList());
	}

	@GetMapping("/crewpage")
	@PreAuthorize("isAuthenticated")
	public String crewPage(@RequestParam(value = "id") Long crewId, Principal principal, Model model, RedirectAttributes redirectAttributes) {
	    Users user = userService.getUser(principal.getName());
	    Crews crew = groupService.getCrewById(crewId);
	    List<Post> posts = postRepository.findByCrew(crew);
	    List<Crews> joined = cmService.findGroupByuser(user);

	    if (joined.contains(crew)) {
	        List<Notice> notice = noticeService.findNoticeByCrew(crew);
	        model.addAttribute("crew", crew);
	        model.addAttribute("notice", notice);
	        model.addAttribute("posts", posts);
	        return "crewPage";
	    } else {
	        redirectAttributes.addFlashAttribute("alertMessage", "모임의 회원만 접근할 수 있습니다.");
	        return "redirect:/meetings/mycrew";
	    }
	}
	
	@GetMapping("/crewpage/notice")
	@PreAuthorize("isAuthenticated")
	public String addNotice(@RequestParam(value = "id") Long crewId, Principal principal, Model model, RedirectAttributes redirectAttributes) {

	    Users user = userService.getUser(principal.getName());
	    Crews crew = groupService.getCrewById(crewId);

	    if (!crew.getLeader().equals(user)) {
	    	redirectAttributes.addFlashAttribute("alertMessage", "모임의 관리자만 공지를 작성할 수 있습니다.");
	        return "redirect:/meetings/crewpage?id=" + crewId;
	    }

	    model.addAttribute("crew", crew);
	    return "noticeForm";
	}

	@PostMapping("/crewpage/notice")
	@PreAuthorize("isAuthenticated")
	public String addNotice(@RequestParam(value = "id") Long crewId, @RequestParam String title, @RequestParam String content) {

	    Crews crew = groupService.getCrewById(crewId);

	    Notice notice = new Notice();
	    notice.setCrew(crew);
	    notice.setTitle(title);
	    notice.setContent(content);
	    notice.setCreated(LocalDateTime.now());

	    noticeService.save(notice);

	    return "redirect:/meetings/crewpage?id=" + crewId;
	}


	@GetMapping("/crewpage/schedule")
	@PreAuthorize("isAuthenticated")
	public String addSchedule(@RequestParam(value = "id") Long crewId, Principal principal, Model model, RedirectAttributes redirectAttributes) {
		
		Users user = userService.getUser(principal.getName());
	    Crews crew = groupService.getCrewById(crewId);
	    
		if (!cmService.existsByCrewIdAndUserId(crewId, user.getId())) {
	        redirectAttributes.addFlashAttribute("alertMessage", "모임 멤버만 일정을 등록할 수 있습니다.");
	        return "redirect:/meetings/crewpage?id=" + crewId;
	    }
		
		model.addAttribute("crew", crew);
		return "scheduleForm";
	}
	
	@PostMapping("/crewpage/schedule")
	@PreAuthorize("isAuthenticated")
	public String addSchedules(@RequestParam(value = "id") Long crewId, @RequestParam String title, @RequestParam String description, @RequestParam LocalDate date, @RequestParam LocalTime stime, @RequestParam LocalTime etime, Principal principal) {

		Users user = userService.getUser(principal.getName());
	    Crews crew = groupService.getCrewById(crewId);

	    Schedule schedule = new Schedule();
	    schedule.setCrew(crew);
	    schedule.setDescription(description);
	    schedule.setDate(date);
	    schedule.setSTime(stime);
	    schedule.setETime(etime);
	    schedule.setTitle(title);
	    schedule.setWriter(user);
	    
	    scheduleService.save(schedule);

	    return "redirect:/meetings/crewpage?id=" + crewId;
	}
	
	@GetMapping("/crewpage/posts")
	@PreAuthorize("isAuthenticated")
	public String createForm(@RequestParam(value = "id") Long crewId, Model model) {
	    model.addAttribute("crewId", crewId);
	    return "postCreate"; 
	}

	@PostMapping("/crewpage/posts")
	@PreAuthorize("isAuthenticated")
	public String createPost(@RequestParam String title,
	                         @RequestParam String content,
	                         @RequestParam Long crewId) {

	    Crews crew = groupService.getCrewById(crewId);

	    Post post = new Post();
	    post.setTitle(title);
	    post.setContent(content);
	    post.setCrew(crew);
	    postRepository.save(post);

	    return "redirect:/meetings/crewpage?id=" + crewId + "#gallery"; // 갤러리 탭으로 이동
	}

	@PostMapping("/crewpage/posts/uploadImage")
	@ResponseBody
	@PreAuthorize("isAuthenticated")
	public String uploadImage(@RequestParam MultipartFile file) throws IOException {
	    String uploadDir = System.getProperty("user.dir") + "/uploads/";
	    File dir = new File(uploadDir);
	    if (!dir.exists()) dir.mkdirs();

	    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
	    File saveFile = new File(uploadDir, fileName);
	    file.transferTo(saveFile);

	    return "/uploads/" + fileName;
	}

	
	@GetMapping("/crewpage/post")
	public String viewPost(@RequestParam(value = "id") Long id, Model model) {
	    Post post = postRepository.findById(id).orElseThrow();
	    model.addAttribute("post", post);
	    return "postDetail";
	}

}
