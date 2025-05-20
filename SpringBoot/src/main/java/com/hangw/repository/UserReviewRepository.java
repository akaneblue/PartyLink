package com.hangw.repository;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hangw.model.UserReviews;
import com.hangw.model.Users;

public interface UserReviewRepository extends JpaRepository<UserReviews, Long>{
	Page<UserReviews> findByUser(Users user, Pageable pageable);
}
