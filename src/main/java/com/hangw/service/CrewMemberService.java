package com.hangw.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hangw.model.Crew_members;
import com.hangw.model.Crews;
import com.hangw.model.UserRole;
import com.hangw.model.Users;
import com.hangw.repository.CrewMembersRepository;
import com.hangw.repository.CrewRepository;
import com.hangw.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CrewMemberService {
	private final CrewMembersRepository cmRepository;
	private final UserRepository userRepository;
	private final CrewRepository crewRepository;
	
	public List<Crew_members> findMembers(Long groupId){
		List<Crew_members> member = cmRepository.findByGroupId(groupId);
		return member;
	}
	
	public Crew_members create(long group_id, long user_id, UserRole role) {
		Crew_members member= new Crew_members();
		member.setGroup(crewRepository.findById(group_id).get());
		member.setUser(userRepository.findById(user_id).get());
		member.setRole(role);
		return cmRepository.save(member);
	}
	
	public Crew_members create(long group_id, long user_id) {
		Crew_members member= new Crew_members();
		member.setGroup(crewRepository.findById(group_id).get());
		member.setUser(userRepository.findById(user_id).get());
		return cmRepository.save(member);
	}
	
	public List<Crews> findGroupByuser(Users user){
		return cmRepository.findGroupsByUser(user);
	}

	public boolean existsByCrewIdAndUserId(Long groupId, Long userId) {
		return cmRepository.existsByGroupIdAndUserId(groupId, userId);
	}
}
