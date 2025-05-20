package com.hangw.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hangw.model.Applicants;
import com.hangw.model.Status;
import com.hangw.repository.AppliRepository;
import com.hangw.repository.CrewRepository;
import com.hangw.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppliService {
	private final CrewRepository crewRepository;
	private final UserRepository userRepository;
	private final AppliRepository appliRepository;
	
	public Applicants create(long groupId, long userId, String content) {
		Applicants app = new Applicants();
		app.setGroup(crewRepository.findById(groupId).get());
		app.setUser(userRepository.findById(userId).get());
		app.setContent(content);
		app.setStatus(Status.Processing);
		return appliRepository.save(app);
	}
	
	public List<Applicants> getAppByUser(long userId){
		return appliRepository.findByUserId(userId);
	}
	
	public List<Applicants> GetAppByCrew(long crewId) {
		return appliRepository.findByGroupId(crewId);
	}

	public Applicants GetAppById(long appId) {
		return appliRepository.findById(appId).get();
	}

	public void save(Applicants app) {
		appliRepository.save(app);
	}
	
	public void deleteById(Long id) {
	    appliRepository.deleteById(id);
	}

	public boolean existsByGroupIdAndUserIdAndStatusProcessing(Long groupId, Long userId) {
	    return appliRepository.existsByGroupIdAndUserIdAndStatus(groupId, userId, Status.Processing);
	}

}
