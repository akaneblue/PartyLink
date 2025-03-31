package com.hangw.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hangw.model.Posts;

public interface PostRepository extends JpaRepository<Posts,Long>{
	Optional<Posts> findByGroupId(long groupId);
}
