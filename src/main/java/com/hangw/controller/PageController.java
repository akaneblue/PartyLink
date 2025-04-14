package com.hangw.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hangw.model.Crews;
import com.hangw.service.CrewService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("")
@RequiredArgsConstructor
public class PageController {
	private final CrewService crewService;
	
	@GetMapping("/")
	public String home(@RequestParam(defaultValue = "0") int page, Model model) {
		List<Crews> crewsList = crewService.getCrews(page,3);
	    model.addAttribute("crewsList", crewsList);
	    model.addAttribute("currentPage", page);
		return "index";
	}
}
