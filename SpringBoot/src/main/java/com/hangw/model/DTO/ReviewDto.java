package com.hangw.model.DTO;

import java.time.format.DateTimeFormatter;

import com.hangw.model.UserReviews;

public record ReviewDto(
	    Long id,
	    String writerName,
	    double rating,
	    String date,
	    String contents
	) {
	public static ReviewDto from(UserReviews r) {
        return new ReviewDto(
            r.getId(),
            r.getWriter().getNickname() != null ? r.getWriter().getNickname() : r.getWriter().getName(),
            r.getRating(),
            r.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")),
            r.getContents()
        );
    }
}