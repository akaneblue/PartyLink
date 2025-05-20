package com.hangw.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hangw.model.Crews;
import com.hangw.model.Notice;
import com.hangw.repository.NoticeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {

	private final NoticeRepository NRepo;
	
	public List<Notice> findNoticeByCrew(Crews crew){
		return NRepo.findByCrew(crew);
	}

	public void save(Notice notice) {
		NRepo.save(notice);
	}
}
