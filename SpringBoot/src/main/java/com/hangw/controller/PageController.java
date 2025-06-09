package com.hangw.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hangw.model.Crews;
import com.hangw.model.DTO.CrewDto;
import com.hangw.service.CrewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PageController {
	private final CrewService crewService;
	
	@GetMapping("/home")
    public ResponseEntity<Map<String, Object>> home(@RequestParam(defaultValue = "0") int page) {
        List<Crews> crews = crewService.getCrews(page, 3);
        
        List<CrewDto> crewsList = crews.stream().map(CrewDto::from).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("crewsList", crewsList);
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }
}

//thymeleaf용 controller
//@Controller
//@RequestMapping("")
//@RequiredArgsConstructor
//public class PageController {
//	private final CrewService crewService;
//	
//	@GetMapping("/")
//	public String home(@RequestParam(defaultValue = "0") int page, Model model) {
//		List<Crews> crewsList = crewService.getCrews(page,3);
//	    model.addAttribute("crewsList", crewsList);
//	    model.addAttribute("currentPage", page);
//		return "index";
//	}
//}