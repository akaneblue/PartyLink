package com.hangw.model.DTO;

import java.util.List;

import com.hangw.model.Users;

public record UserDetailDto(
	    Long id,
	    String name,
	    String nickname,
	    String email,
	    String description,
	    String location,
	    String imagePath,
	    double rating,
	    int reviewCount,
	    List<InterestDto> interest
	) {
	    public static UserDetailDto from(Users user) {
	        List<InterestDto> interests = user.getInterest().stream()
	            .map(i -> new InterestDto(i.getId(), i.getName()))
	            .toList();

	        return new UserDetailDto(
	            user.getId(),
	            user.getName(),
	            user.getNickname(),
	            user.getEmail(),
	            user.getDescription(),
	            user.getLocation(),
	            user.getImagePath(),
	            user.getRating(),
	            user.getReviewCount(),
	            interests
	        );
	    }
	}