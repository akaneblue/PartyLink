package com.hangw.model.DTO;

import java.time.LocalDate;
import java.util.List;

import com.hangw.model.Interest;
import com.hangw.model.Users;

public record UserDto(String email,String name,
	    String nickname,
	    String description,
	    String location,
	    LocalDate birth,
	    String gender,
	    String imagePath,
	    List<Long> interestIds) {
	
	public static UserDto from(Users user) {
	    List<Long> interestIds = user.getInterest().stream()
	        .map(Interest::getId)
	        .toList();

	    return new UserDto(
	        user.getEmail(),
	        user.getName(),
	        user.getNickname(),
	        user.getDescription(),
	        user.getLocation(),
	        user.getBirth(),
	        user.getGender().name(),
	        user.getImagePath(),
	        interestIds
	    );
	}
}
