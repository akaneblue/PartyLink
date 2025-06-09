package com.hangw.model.DTO;

import java.time.format.DateTimeFormatter;

import com.hangw.model.Notice;

public record NoticeDto(long id, String title, String content, String created) {
	public static NoticeDto from(Notice notice) {
		return new NoticeDto(notice.getId(), notice.getTitle(), notice.getContent(), notice.getCreated().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")));
	}
}
