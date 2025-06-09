package com.hangw.model.DTO;

import java.time.format.DateTimeFormatter;

import com.hangw.model.Post;

public record PostDto(long id, String title, String content, String created) {
	public static PostDto from(Post post) {
		return new PostDto(post.getId(),post.getTitle(), post.getContent(), post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")));
	}
}
