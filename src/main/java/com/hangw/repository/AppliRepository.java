package com.hangw.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hangw.model.Applicants;
import com.hangw.model.Status;

@Repository
public interface AppliRepository extends JpaRepository<Applicants,Long>{
	List<Applicants> findByGroupId(long groupId);
	
	List<Applicants> findByUserId(long userId);

	boolean existsByGroupIdAndUserIdAndStatus(Long groupId, Long userId, Status status);
	
}
