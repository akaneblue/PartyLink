package com.hangw.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hangw.model.Group_members;

public interface GroupMembersRepository extends JpaRepository<Group_members,Long>{
	Optional<Group_members> findByUserId(long userId);
	
	Optional<Group_members> findByGroupId(long groupId);
}
