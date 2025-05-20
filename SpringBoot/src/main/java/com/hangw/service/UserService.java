package com.hangw.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hangw.model.DataNotFoundException;
import com.hangw.model.Interest;
import com.hangw.model.Users;
import com.hangw.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	public Users create(String name, String email, String password) {
		Users user = new Users();
		user.setName(name);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(password));
		userRepository.save(user);
		return user;
	}
	
	public Users getUser(String email) {
		Optional<Users> pageUser= userRepository.findByEmail(email);
		if(pageUser.isPresent()) {
			return pageUser.get();
		}else {
			throw new DataNotFoundException("사용자를 찾을 수 없습니다.");
		}
	}
	
	public Users findUserById(Long id) {
		Users user = userRepository.findById(id).get();
		return user;
	}
	
	public List<Users> findByInterest(Interest interest) {
		return userRepository.findByInterest(interest);
	}
	
	
	public void save(Users user) {
        userRepository.save(user);
    }
}
