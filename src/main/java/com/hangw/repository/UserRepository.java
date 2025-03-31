package com.hangw.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hangw.model.Users;

public interface UserRepository extends JpaRepository<Users, Long> {
	Optional<Users> findByEmail(String Email);
	
	Optional<Users> findByInterest(String interest);
}
