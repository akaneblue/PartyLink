package com.hangw.controller;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

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
import com.hangw.model.DTO.CrewDto;
import com.hangw.model.DTO.NoticeDto;
import com.hangw.model.DTO.PostDto;
import com.hangw.model.DTO.UserDetailDto;
import com.hangw.repository.CrewRepository;
import com.hangw.repository.InterestRepository;
import com.hangw.repository.NotificationRepository;
import com.hangw.repository.PostRepository;
import com.hangw.service.AICategoryService;
import com.hangw.service.AICategoryService.AIKeywordResult;
import com.hangw.service.CrewMemberService;
import com.hangw.service.CrewService;
import com.hangw.service.NoticeService;
import com.hangw.service.PostService;
import com.hangw.service.RequirementService;
import com.hangw.service.ScheduleService;
import com.hangw.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/crew")
@RequiredArgsConstructor
public class CrewController {
	private final CrewRepository crewRepository;
	private final CrewService crewService;
	private final UserService userService;
	private final CrewMemberService cmService;
	private final AICategoryService aiService;
	private final RequirementService reqService;
	private final InterestRepository intRepository;
	private final ScheduleService scheduleService;
	private final NoticeService noticeService;
	private final NotificationRepository notificationRepository;
	private final PostRepository postRepository;
	private final PostService postService;

	@GetMapping("")
	public ResponseEntity<?> CrewList(@RequestParam(defaultValue = "0") int page,
			@RequestParam(value = "category", required = false) String categoryName,
			@RequestParam(value = "keyword", required = false) String keyword, Principal principal) {
		Map<String, Object> response = new HashMap<>();

		List<Crews> crewList;

		if (keyword != null && !keyword.isBlank()) {
			crewList = crewService.searchCrewsByKeyword(keyword, page, 15);
			response.put("keyword", keyword);
		} else if (categoryName != null && !categoryName.isBlank()) {
			Optional<Interest> category = intRepository.findByName(categoryName);
			Interest Category = category.get();
			crewList = crewService.getCrewsByCategory(Category, page, 15);
			response.put("categoryName", Category.getName());
		} else {
			crewList = crewService.getCrews(page, 15);
		}

		List<CrewDto> crewDtos = crewList.stream().map(CrewDto::from).toList();

		response.put("crewList", crewDtos);
		response.put("currentPage", page);

		if (principal != null) {
			Users user = userService.getUser(principal.getName());
			response.put("user", UserDetailDto.from(user));
		}

		return ResponseEntity.ok(response);
	}

	@GetMapping("/detail")
	public ResponseEntity<?> crewDetail(@RequestParam(value = "id") Long crewId, Principal principal) {

		Map<String, Object> response = new HashMap<>();

		Crews crew = crewService.getCrewById(crewId);
		CrewDto crewDto = CrewDto.from(crew);
		List<Crew_members> members = cmService.findMembers(crewId);
		List<UserDetailDto> memberDtos = members.stream().map(Crew_members::getUser).map(UserDetailDto::from).toList();
		UserDetailDto leader = UserDetailDto.from(crew.getLeader());
		response.put("crew", crewDto);
		response.put("members", memberDtos);
		response.put("leader", leader);

		if (principal != null) {
			Users user = userService.getUser(principal.getName());
			List<Notification> unread = notificationRepository.findByUserAndCrewAndIsReadFalse(user, crew);

			for (Notification n : unread) {
				n.setRead(true);
			}
			notificationRepository.saveAll(unread);
		}
		return ResponseEntity.ok(response);
	}

	@PostMapping("/create")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> createCrew(@Valid CrewCreateForm crewCreateForm, BindingResult bindingResult,
			@RequestParam(required = false) MultipartFile imageFile, Principal principal) {
		if (bindingResult.hasErrors()) {
			List<String> errors = bindingResult.getAllErrors().stream()
					.map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
			return ResponseEntity.badRequest().body(Map.of("errors", errors));
		}

		try {
			AIKeywordResult category = aiService.getCategoryFromAI(crewCreateForm.getDescription());

			Interest interest = intRepository.findByName(category.getKeyword()).orElseGet(() -> {
				Interest newInterest = new Interest();
				newInterest.setName(category.getKeyword());
				return intRepository.save(newInterest); // 🔁 먼저 저장
			});

			crewCreateForm.setCategory(interest); // ✅ 영속 객체로 설정

			if (category.isNew()) {
				interest.setName(category.getKeyword());
				intRepository.save(interest);
			}

			String imagePath = null;
			if (imageFile != null && !imageFile.isEmpty()) {
				String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
				String uploadDir = System.getProperty("user.dir") + "/uploads/";
				File uploadPath = new File(uploadDir);
				if (!uploadPath.exists())
					uploadPath.mkdirs();
				File saveFile = new File(uploadPath, fileName);
				imageFile.transferTo(saveFile);
				imagePath = "/uploads/" + fileName;
			}

			Crews crew = crewService.create(crewCreateForm.getName(), crewCreateForm.getMaxParticipants(),
					crewCreateForm.getDescription(), crewCreateForm.getCategory(), crewCreateForm.getDate(),
					crewCreateForm.getDate2(), crewCreateForm.getTime(), crewCreateForm.getLocation(),
					userService.getUser(principal.getName()), imagePath);

			cmService.create(crew.getId(), crew.getLeader().getId(), UserRole.Leader);

			if (crewCreateForm.getGender() != null || crewCreateForm.getMinAge() != null
					|| crewCreateForm.getMaxAge() != null || crewCreateForm.getLocation() != null) {
				reqService.create(crew, crewCreateForm.getGender(), crewCreateForm.getMinAge(),
						crewCreateForm.getMaxAge(), crewCreateForm.getLocation());
			}

			List<Users> interestedUsers = userService.findByInterest(interest);
			String message = "' 당신의 관심사 '" + interest.getName() + "'에 맞는 새로운 모임이 생성되었어요!";
			List<Notification> notifications = interestedUsers.stream()
					.filter(u -> !u.getEmail().equals(principal.getName())).map(user -> {
						Notification n = new Notification();
						n.setUser(user);
						n.setCrew(crew);
						n.setMessage(message);
						return n;
					}).toList();
			notificationRepository.saveAll(notifications);

			return ResponseEntity.ok(Map.of("message", "모임 생성 성공", "crewId", crew.getId()));

		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "이미 존재하는 이름입니다."));
		} catch (IOException e) {
			return ResponseEntity.internalServerError().body(Map.of("error", "이미지 업로드 중 오류가 발생했습니다."));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("error", "알 수 없는 오류가 발생했습니다."));
		}
	}

	@GetMapping("/mycrew")
	@PreAuthorize("isAuthenticated")
	public ResponseEntity<?> myCrew(Principal principal) {

		Map<String, Object> response = new HashMap<>();

		Users user = userService.getUser(principal.getName());
		List<Crews> crews = cmService.findGroupByuser(user);
		List<CrewDto> crewDto = crews.stream().map(CrewDto::from).toList();
		List<Crews> made = crewService.getCrewByLeader(user);
		List<CrewDto> madeDto = made.stream().map(CrewDto::from).toList();
		response.put("crews", crewDto);
		response.put("made", madeDto);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/crewpage")
	@PreAuthorize("isAuthenticated")
	public ResponseEntity<?> crewPage(@RequestParam long id, Principal principal) {
		Map<String, Object> response = new HashMap<>();

		Users user = userService.getUser(principal.getName());
		Crews crew = crewService.getCrewById(id);

		List<Crews> joined = cmService.findGroupByuser(user);
		if (!joined.contains(crew)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "모임의 회원만 접근할 수 있습니다."));
		}

		List<Notice> notice = noticeService.findNoticeByCrew(crew);
		List<Post> posts = postRepository.findByCrew(crew);

		response.put("crew", CrewDto.from(crew));
		response.put("notices", notice.stream().map(NoticeDto::from).toList());
		response.put("posts", posts.stream().map(PostDto::from).toList());

		return ResponseEntity.ok(response);
	}

	@GetMapping("/calendarList")
	@PreAuthorize("isAuthenticated")
	public List<Map<String, Object>> getCalendarEvents(@RequestParam Long crewId) {
		Crews crew = crewService.getCrewById(crewId);
		List<Schedule> schedules = scheduleService.getSchedule(crew);

		return schedules.stream().map(s -> {
			Map<String, Object> event = new HashMap<>();
			event.put("id", s.getId());
			event.put("title", s.getTitle());

			event.put("start", s.getDate().atTime(s.getSTime()).toString());
			event.put("end", s.getDate().atTime(s.getETime()).toString());
			event.put("allDay", false);
			return event;
		}).collect(Collectors.toList());
	}

	@PostMapping("/crewpage/notice")
	@PreAuthorize("isAuthenticated")
	public ResponseEntity<?> addNotice(@RequestParam("id") Long crewId, @RequestBody Map<String, String> payload,
			Principal principal) {
		String title = payload.get("title");
		String content = payload.get("content");

		Users user = userService.getUser(principal.getName());
		Crews crew = crewService.getCrewById(crewId);

		if (!crew.getLeader().equals(user)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "모임의 관리자만 공지를 작성할 수 있습니다."));
		}

		Notice notice = new Notice();
		notice.setCrew(crew);
		notice.setTitle(title);
		notice.setContent(content);
		notice.setCreated(LocalDateTime.now());

		noticeService.save(notice);

		return ResponseEntity.ok().build();
	}

	@GetMapping("/info")
	@PreAuthorize("isAuthenticated")
	public ResponseEntity<?> getCrewInfo(@RequestParam Long id, Principal principal) {
		Users user = userService.getUser(principal.getName());
		Crews crew = crewService.getCrewById(id);

		if (!crew.getLeader().equals(user)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "모임의 관리자만 접근할 수 있습니다."));
		}

		return ResponseEntity.ok(Map.of("id", crew.getId(), "name", crew.getName()));
	}

	@PostMapping("/crewpage/schedule")
	@PreAuthorize("isAuthenticated")
	public ResponseEntity<?> addSchedules(@RequestParam Long id, @RequestBody Map<String, String> body,
			Principal principal) {
		Users user = userService.getUser(principal.getName());
		Crews crew = crewService.getCrewById(id);

		Schedule schedule = new Schedule();
		schedule.setCrew(crew);
		schedule.setTitle(body.get("title"));
		schedule.setDescription(body.get("description"));
		schedule.setDate(LocalDate.parse(body.get("date")));
		schedule.setSTime(LocalTime.parse(body.get("stime")));
		schedule.setETime(LocalTime.parse(body.get("etime")));
		schedule.setWriter(user);

		scheduleService.save(schedule);

		return ResponseEntity.ok().build();
	}

	@PostMapping("/crewpage/posts")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> createPost(@RequestParam Long id, @RequestBody Map<String, String> body,
			Principal principal) {
		Crews crew = crewService.getCrewById(id);

		// ✅ FastAPI를 호출하여 tone 분석
		String content = body.get("content");
		String tone = postService.analyzeToneWithLLM(content);

		// ✅ Post 생성 및 저장
		Post post = new Post();
		post.setTitle(body.get("title"));
		post.setContent(content);
		post.setCrew(crew);
		post.setTone(tone);
		postRepository.save(post);

		// ✅ Crew의 toneTag 업데이트
		postService.updateCrewToneTag(crew);

		return ResponseEntity.ok(Map.of("tone", tone));
	}

	@PostMapping("/crewpage/posts/uploadImage")
	@PreAuthorize("isAuthenticated")
	public String uploadImage(@RequestParam MultipartFile file) throws IOException {
		String uploadDir = System.getProperty("user.dir") + "/uploads/";
		File dir = new File(uploadDir);
		if (!dir.exists())
			dir.mkdirs();

		String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
		File saveFile = new File(uploadDir, fileName);
		file.transferTo(saveFile);

		return "/uploads/" + fileName;
	}

	@GetMapping("/crewpage/post")
	@PreAuthorize("isAuthenticated")
	public ResponseEntity<?> postDetail(@RequestParam Long id) {
		Post post = postRepository.findById(id).orElseThrow();

		Map<String, Object> response = new HashMap<>();

		response.put("post", PostDto.from(post));

		return ResponseEntity.ok(response);
	}

	@GetMapping("/recommend")
	public ResponseEntity<?> recommendCrews(@RequestParam(defaultValue = "0") int page, @RequestParam String toneQuery,
			Principal principal) {
		Users user = userService.getUser(principal.getName());

		Map<String, Object> response = new HashMap<>();

		// 1. 유저 관심사 카테고리 추출
		List<Interest> interests = user.getInterest(); // List<Interest>

		// 2. 관심사 카테고리 안의 모임만 가져옴
		List<Crews> candidateCrews = interests.stream()
				.flatMap(interest -> crewService.getCrewsByCategory(interest, page, 15).stream())
				.collect(Collectors.toList());

		// 3. 후보 분위기 태그 목록 수집
		Set<String> candidateTags = candidateCrews.stream().map(Crews::getTag).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		// 4. FastAPI에 분위기 유사도 요청
		List<String> matchedTags = getBestMatchedToneTags(toneQuery, new ArrayList<>(candidateTags));

		// 분위기 기준에 맞는 모임만 필터링
		List<Crews> recommended = candidateCrews.stream().filter(c -> matchedTags.contains(c.getTag()))
				.collect(Collectors.toList());

		List<CrewDto> recommendedDtos = recommended.stream().map(CrewDto::from).toList();

		response.put("recommend", recommendedDtos);

		return ResponseEntity.ok(response);
	}

	@Autowired
	private RestTemplate restTemplate;

	private List<String> getBestMatchedToneTags(String query, List<String> tags) {
		String url = "http://localhost:8000/match-tone";

		Map<String, Object> request = Map.of("query", query, "candidates", tags, "threshold", 0.65 // 필요시 이 값을 조정
		);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

		try {
			ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
			List<Map<String, Object>> matchedTags = (List<Map<String, Object>>) response.getBody().get("matched_tags");

			if (matchedTags == null || matchedTags.isEmpty())
				return List.of();

			return matchedTags.stream().map(m -> (String) m.get("tag")).collect(Collectors.toList());

		} catch (Exception e) {
			throw new RuntimeException("FastAPI 호출 실패: " + e.getMessage());
		}
	}

}
//thymeleaf controller
//@Controller
//@RequestMapping("/meetings")
//@RequiredArgsConstructor
//public class CrewController {
//
//	private final CrewService groupService;
//	private final UserService userService;
//	private final CrewMemberService cmService;
//	private final AICategoryService aiService;
//	private final RequirementService reqService;
//	private final InterestRepository intRepository;
//	private final ScheduleService scheduleService;
//	private final NoticeService noticeService;
//	private final NotificationRepository notificationRepository;
//	private final PostRepository postRepository;
//
//	@GetMapping("")
//	public String groupList(@RequestParam(defaultValue = "0") int page,
//	                        @RequestParam(value = "category", required = false) String categoryName,
//	                        @RequestParam(value = "keyword", required = false) String keyword,
//	                        Model model,
//	                        Principal principal) {
//
//	    if (principal != null) {
//	        Users user = userService.getUser(principal.getName());
//	        model.addAttribute("pageuser", user);
//	        model.addAttribute("Login", true);
//	    } else {
//	        model.addAttribute("Login", false);
//	    }
//
//	    List<Crews> crewsList;
//
//	    // 🔍 검색 키워드가 우선 적용됨
//	    if (keyword != null && !keyword.isBlank()) {
//	        crewsList = groupService.searchCrewsByKeyword(keyword, page, 15);
//	        model.addAttribute("keyword", keyword);
//	    }
//	    // 🔎 키워드 없고, 카테고리 필터 있을 경우
//	    else if (categoryName != null && !categoryName.isBlank()) {
//	        Optional<Interest> categoryOpt = intRepository.findByName(categoryName);
//	        if (categoryOpt.isPresent()) {
//	            Interest category = categoryOpt.get();
//	            crewsList = groupService.getCrewsByCategory(category, page, 15);
//	            model.addAttribute("selectedCategory", category);
//	        } else {
//	            // 존재하지 않는 카테고리면 전체 목록 보여주기
//	            crewsList = groupService.getCrews(page, 15);
//	        }
//	    }
//	    // 🔁 아무 필터도 없는 기본 조회
//	    else {
//	        crewsList = groupService.getCrews(page, 15);
//	    }
//
//	    model.addAttribute("crewsList", crewsList);
//	    model.addAttribute("currentPage", page);
//	    return "list";
//	}
//
//
//
//
//	@GetMapping("/create")
//	@PreAuthorize("isAuthenticated")
//	public String groupCreate(CrewCreateForm crewCreateForm) {
//		return "create";
//	}
//
//	@PostMapping("/create")
//	public String groupCreate(@Valid CrewCreateForm crewCreateForm, BindingResult bindingResult,
//			@RequestParam MultipartFile imageFile, Principal principal) {
//		if (bindingResult.hasErrors())
//			return "create";
//		try {
//			
//			AIKeywordResult category = aiService.getCategoryFromAI(crewCreateForm.getDescription());
//
//			Interest interest = intRepository.findByName(category.getKeyword())
//			    .orElseGet(() -> {
//			        Interest newInterest = new Interest();
//			        newInterest.setName(category.getKeyword());
//			        return intRepository.save(newInterest);  // 🔁 먼저 저장
//			    });
//
//			crewCreateForm.setCategory(interest);  // ✅ 영속 객체로 설정
//
//			
//			if(category.isNew()) {
//				interest.setName(category.getKeyword());
//				intRepository.save(interest);
//			}
//			
//			String imagePath = null;
//			if (imageFile != null && !imageFile.isEmpty()) {
//				String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
//				String uploadDir = System.getProperty("user.dir") + "/uploads/";
//				File uploadPath = new File(uploadDir);
//
//				// 디렉토리 없으면 생성
//				if (!uploadPath.exists()) {
//					uploadPath.mkdirs();
//				}
//
//				File saveFile = new File(uploadDir, fileName);
//				System.out.println("파일 저장 경로: " + saveFile.getAbsolutePath());
//				
//				imageFile.transferTo(saveFile);
//				imagePath = "/uploads/" + fileName;
//			}
//
//			Crews crew = groupService.create(crewCreateForm.getName(), crewCreateForm.getMaxParticipants(),
//					crewCreateForm.getDescription(), crewCreateForm.getCategory(), crewCreateForm.getDate(), crewCreateForm.getDate2(), crewCreateForm.getTime(),
//					crewCreateForm.getLocation(), userService.getUser(principal.getName()), imagePath);
//			cmService.create(crew.getId(), crew.getLeader().getId(), UserRole.Leader);
//
//			if (crewCreateForm.getGender() != null || crewCreateForm.getMinAge() != null || crewCreateForm.getMaxAge() != null || crewCreateForm.getLocation() != null) {
//				reqService.create(crew, crewCreateForm.getGender(), crewCreateForm.getMinAge(), crewCreateForm.getMaxAge(), crewCreateForm.getLocation());
//			}
//			
//			// 1. 관심사로 유저 찾기
//			List<Users> interestedUsers = userService.findByInterest(interest);
//
//			// 2. 알림 생성 및 저장
//			String message = "' 당신의 관심사 '" + interest.getName() + "'에 맞는 새로운 모임이 생성되었어요!";
//			List<Notification> notifications = interestedUsers.stream()
//			    .filter(u -> !u.getEmail().equals(principal.getName())) // 생성자 제외
//			    .map(user -> {
//			        Notification notification = new Notification();
//			        notification.setUser(user);
//			        notification.setCrew(crew);
//			        notification.setMessage(message);
//			        return notification;
//			    })
//			    .collect(Collectors.toList());
//
//			notificationRepository.saveAll(notifications);
//			
//		} catch (DataIntegrityViolationException e) {
//			e.printStackTrace();
//			bindingResult.reject("signupFailed", "이미 존재하는 이름입니다.");
//			return "create";
//		} catch (IOException e) {
//			e.printStackTrace();
//			bindingResult.reject("imageUploadFailed", "이미지 업로드 중 오류가 발생했습니다.");
//			return "create";
//		} catch (Exception e) {
//		    e.printStackTrace();
//		    bindingResult.reject("imageUploadFailed", "알 수 없는 오류가 발생했습니다.");
//		    return "create";
//		}
//
//		
//		return "redirect:/";
//	}
//
//	@GetMapping("/detail")
//	public String crewDetail(@RequestParam(value = "id") Long crewId, Model model, Principal principal) {
//	    Crews crew = groupService.getCrewById(crewId);
//	    List<Crew_members> members = cmService.findMembers(crewId);
//
//	    model.addAttribute("crew", crew);
//	    model.addAttribute("leader", crew.getLeader());
//	    model.addAttribute("members", members);
//
//	    // ✅ 로그인 사용자에게만 알림 읽음 처리
//	    if (principal != null) {
//	        Users user = userService.getUser(principal.getName());
//	        List<Notification> unread = notificationRepository
//	            .findByUserAndCrewAndIsReadFalse(user, crew);
//
//	        for (Notification n : unread) {
//	            n.setRead(true);
//	        }
//	        notificationRepository.saveAll(unread);
//	    }
//
//	    return "detail";
//	}
//	
//	@GetMapping("/mycrew")
//	@PreAuthorize("isAuthenticated")
//	public String myCrew(Principal principal, Model model) {
//		Users user =userService.getUser(principal.getName());
//		List<Crews> crews = cmService.findGroupByuser(user);
//		List<Crews> made = groupService.getCrewByLeader(user);
//		model.addAttribute("crews", crews);
//		model.addAttribute("made", made);
//		return "mycrew";
//	}
//	
//	@GetMapping("/calendarList")
//	@ResponseBody
//	@PreAuthorize("isAuthenticated")
//	public List<Map<String, Object>> getCalendarEvents(@RequestParam Long crewId) {
//	    Crews crew = groupService.getCrewById(crewId);
//	    List<Schedule> schedules = scheduleService.getSchedule(crew);
//
//	    return schedules.stream().map(s -> {
//	        Map<String, Object> event = new HashMap<>();
//	        event.put("id", s.getId());
//	        event.put("title", s.getTitle());
//
//	        // LocalDate + LocalTime => ISO LocalDateTime string ("2024-06-01T14:00")
//	        event.put("start", s.getDate().atTime(s.getSTime()).toString());
//	        event.put("end", s.getDate().atTime(s.getETime()).toString());
//	        event.put("allDay", false);
//	        return event;
//	    }).collect(Collectors.toList());
//	}
//
//	@GetMapping("/crewpage")
//	@PreAuthorize("isAuthenticated")
//	public String crewPage(@RequestParam(value = "id") Long crewId, Principal principal, Model model, RedirectAttributes redirectAttributes) {
//	    Users user = userService.getUser(principal.getName());
//	    Crews crew = groupService.getCrewById(crewId);
//	    List<Post> posts = postRepository.findByCrew(crew);
//	    List<Crews> joined = cmService.findGroupByuser(user);
//
//	    if (joined.contains(crew)) {
//	        List<Notice> notice = noticeService.findNoticeByCrew(crew);
//	        model.addAttribute("crew", crew);
//	        model.addAttribute("notice", notice);
//	        model.addAttribute("posts", posts);
//	        return "crewPage";
//	    } else {
//	        redirectAttributes.addFlashAttribute("alertMessage", "모임의 회원만 접근할 수 있습니다.");
//	        return "redirect:/meetings/mycrew";
//	    }
//	}
//	
//	@GetMapping("/crewpage/notice")
//	@PreAuthorize("isAuthenticated")
//	public String addNotice(@RequestParam(value = "id") Long crewId, Principal principal, Model model, RedirectAttributes redirectAttributes) {
//
//	    Users user = userService.getUser(principal.getName());
//	    Crews crew = groupService.getCrewById(crewId);
//
//	    if (!crew.getLeader().equals(user)) {
//	    	redirectAttributes.addFlashAttribute("alertMessage", "모임의 관리자만 공지를 작성할 수 있습니다.");
//	        return "redirect:/meetings/crewpage?id=" + crewId;
//	    }
//
//	    model.addAttribute("crew", crew);
//	    return "noticeForm";
//	}
//
//	@PostMapping("/crewpage/notice")
//	@PreAuthorize("isAuthenticated")
//	public String addNotice(@RequestParam(value = "id") Long crewId, @RequestParam String title, @RequestParam String content) {
//
//	    Crews crew = groupService.getCrewById(crewId);
//
//	    Notice notice = new Notice();
//	    notice.setCrew(crew);
//	    notice.setTitle(title);
//	    notice.setContent(content);
//	    notice.setCreated(LocalDateTime.now());
//
//	    noticeService.save(notice);
//
//	    return "redirect:/meetings/crewpage?id=" + crewId;
//	}
//
//
//	@GetMapping("/crewpage/schedule")
//	@PreAuthorize("isAuthenticated")
//	public String addSchedule(@RequestParam(value = "id") Long crewId, Principal principal, Model model, RedirectAttributes redirectAttributes) {
//		
//		Users user = userService.getUser(principal.getName());
//	    Crews crew = groupService.getCrewById(crewId);
//	    
//		if (!cmService.existsByCrewIdAndUserId(crewId, user.getId())) {
//	        redirectAttributes.addFlashAttribute("alertMessage", "모임 멤버만 일정을 등록할 수 있습니다.");
//	        return "redirect:/meetings/crewpage?id=" + crewId;
//	    }
//		
//		model.addAttribute("crew", crew);
//		return "scheduleForm";
//	}
//	
//	@PostMapping("/crewpage/schedule")
//	@PreAuthorize("isAuthenticated")
//	public String addSchedules(@RequestParam(value = "id") Long crewId, @RequestParam String title, @RequestParam String description, @RequestParam LocalDate date, @RequestParam LocalTime stime, @RequestParam LocalTime etime, Principal principal) {
//
//		Users user = userService.getUser(principal.getName());
//	    Crews crew = groupService.getCrewById(crewId);
//
//	    Schedule schedule = new Schedule();
//	    schedule.setCrew(crew);
//	    schedule.setDescription(description);
//	    schedule.setDate(date);
//	    schedule.setSTime(stime);
//	    schedule.setETime(etime);
//	    schedule.setTitle(title);
//	    schedule.setWriter(user);
//	    
//	    scheduleService.save(schedule);
//
//	    return "redirect:/meetings/crewpage?id=" + crewId;
//	}
//	
//	@GetMapping("/crewpage/posts")
//	@PreAuthorize("isAuthenticated")
//	public String createForm(@RequestParam(value = "id") Long crewId, Model model) {
//	    model.addAttribute("crewId", crewId);
//	    return "postCreate"; 
//	}
//
//	@PostMapping("/crewpage/posts")
//	@PreAuthorize("isAuthenticated")
//	public String createPost(@RequestParam String title,
//	                         @RequestParam String content,
//	                         @RequestParam Long crewId) {
//
//	    Crews crew = groupService.getCrewById(crewId);
//
//	    Post post = new Post();
//	    post.setTitle(title);
//	    post.setContent(content);
//	    post.setCrew(crew);
//	    postRepository.save(post);
//
//	    return "redirect:/meetings/crewpage?id=" + crewId + "#gallery"; // 갤러리 탭으로 이동
//	}
//
//	@PostMapping("/crewpage/posts/uploadImage")
//	@ResponseBody
//	@PreAuthorize("isAuthenticated")
//	public String uploadImage(@RequestParam MultipartFile file) throws IOException {
//	    String uploadDir = System.getProperty("user.dir") + "/uploads/";
//	    File dir = new File(uploadDir);
//	    if (!dir.exists()) dir.mkdirs();
//
//	    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//	    File saveFile = new File(uploadDir, fileName);
//	    file.transferTo(saveFile);
//
//	    return "/uploads/" + fileName;
//	}
//
//	
//	@GetMapping("/crewpage/post")
//	public String viewPost(@RequestParam(value = "id") Long id, Model model) {
//	    Post post = postRepository.findById(id).orElseThrow();
//	    model.addAttribute("post", post);
//	    return "postDetail";
//	}
//
//}
