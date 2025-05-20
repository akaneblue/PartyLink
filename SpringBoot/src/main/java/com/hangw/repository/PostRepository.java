package com.hangw.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hangw.model.Crews;
import com.hangw.model.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
	List<Post> findByCrew(Crews crew);
}
