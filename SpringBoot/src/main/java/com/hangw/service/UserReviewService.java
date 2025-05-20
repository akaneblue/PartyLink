package com.hangw.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.hangw.model.UserReviews;
import com.hangw.model.Users;
import com.hangw.repository.UserReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserReviewService {
	private final UserReviewRepository urRepository;
	
	public Page<UserReviews> findReviewByUser(Users user, int page){
		Pageable pageable = (Pageable) PageRequest.of(page, 10);
		
		return urRepository.findByUser(user, pageable);
	}

	public void save(UserReviews review) {
		// TODO Auto-generated method stub
		urRepository.save(review);
	}
}
