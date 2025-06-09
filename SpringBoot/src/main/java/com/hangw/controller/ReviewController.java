package com.hangw.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hangw.model.UserReviews;
import com.hangw.model.Users;
import com.hangw.service.UserReviewService;
import com.hangw.service.UserService;

import lombok.RequiredArgsConstructor;

//thymeleaf용 controller
//@Controller
//@RequiredArgsConstructor
//@RequestMapping("/review")
//public class ReviewController {
//	private final UserService userService;
//	private final UserReviewService urService;
//	
//	@PostMapping("/add")
//	@PreAuthorize("isAuthenticated()")
//	public String addReview(@RequestParam long userId,
//	                        @RequestParam String content,
//	                        @RequestParam double rating,
//	                        Principal principal,
//	                        RedirectAttributes redirectAtt) {
//
//	    Users targetUser = userService.findUserById(userId);
//	    Users writer = userService.getUser(principal.getName());
//
//	    // 자기 자신 리뷰 방지
//	    if (targetUser.getId() == writer.getId()) {
//	        redirectAtt.addFlashAttribute("errorMessage", "자기 자신에게 리뷰를 작성할 수 없습니다.");
//	        return "redirect:/user/userdetail?userId=" + targetUser.getId();
//	    }
//
//	    UserReviews review = new UserReviews();
//	    review.setContents(content);
//	    review.setUser(targetUser);
//	    review.setWriter(writer);
//	    review.setRating(rating);
//	    urService.save(review);
//
//	    int count = targetUser.getReviewCount();
//	    targetUser.setReviewCount(count + 1);
//	    targetUser.setRating((targetUser.getRating() * count + rating) / targetUser.getReviewCount());
//	    userService.save(targetUser);
//
//	    return "redirect:/user/userdetail?userId=" + targetUser.getId();
//	}
//
//}



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {

    private final UserService userService;
    private final UserReviewService urService;

    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addReview(@RequestBody ReviewRequest request, Principal principal) {
        Users targetUser = userService.findUserById(request.userId());
        Users writer = userService.getUser(principal.getName());

        if (targetUser.getId()==writer.getId()) {
            return ResponseEntity.badRequest().body(Map.of("message", "자기 자신에게 리뷰를 작성할 수 없습니다."));
        }

        // 1. 리뷰 저장
        UserReviews review = new UserReviews();
        review.setContents(request.content());
        review.setUser(targetUser);
        review.setWriter(writer);
        review.setRating(request.rating());
        urService.save(review);

        // 2. 사용자 평점 갱신
        int count = targetUser.getReviewCount();
        double totalScore = targetUser.getRating() * count;
        double newAverage = (totalScore + request.rating()) / (count + 1);
        targetUser.setReviewCount(count + 1);
        targetUser.setRating(newAverage);
        userService.save(targetUser); // 꼭 저장해야 DB 반영됨

        return ResponseEntity.ok().build(); // 갱신 성공 응답
    }


    public record ReviewRequest(Long userId, String content, double rating) {}
}
