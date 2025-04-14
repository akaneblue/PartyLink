package com.hangw.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hangw.model.Crew_members;
import com.hangw.model.Crews;
import com.hangw.model.Users;

@Repository
public interface CrewMembersRepository extends JpaRepository<Crew_members,Long>{
	List<Crew_members> findByUserId(long userId);
	
	List<Crew_members> findByGroupId(long groupId);
	
	@Query("SELECT cm.group FROM Crew_members cm WHERE cm.user = :user")
    List<Crews> findGroupsByUser(@Param("user") Users user);

	boolean existsByGroupIdAndUserId(Long groupId, Long userId);
}
