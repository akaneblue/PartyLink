package com.hangw.controller;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hangw.model.UserReviews;
import com.hangw.model.Users;
import com.hangw.service.UserReviewService;
import com.hangw.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/review")
public class ReviewController {
	private final UserService userService;
	private final UserReviewService urService;
	
	@PostMapping("/add")
	@PreAuthorize("isAuthenticated()")
	public String addReview(@RequestParam long userId,
	                        @RequestParam String content,
	                        @RequestParam double rating,
	                        Principal principal,
	                        RedirectAttributes redirectAtt) {

	    Users targetUser = userService.findUserById(userId);
	    Users writer = userService.getUser(principal.getName());

	    // 자기 자신 리뷰 방지
	    if (targetUser.getId() == writer.getId()) {
	        redirectAtt.addFlashAttribute("errorMessage", "자기 자신에게 리뷰를 작성할 수 없습니다.");
	        return "redirect:/user/userdetail?userId=" + targetUser.getId();
	    }

	    UserReviews review = new UserReviews();
	    review.setContents(content);
	    review.setUser(targetUser);
	    review.setWriter(writer);
	    review.setRating(rating);
	    urService.save(review);

	    int count = targetUser.getReviewCount();
	    targetUser.setReviewCount(count + 1);
	    targetUser.setRating((targetUser.getRating() * count + rating) / targetUser.getReviewCount());
	    userService.save(targetUser);

	    return "redirect:/user/userdetail?userId=" + targetUser.getId();
	}

}
