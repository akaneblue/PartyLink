package com.hangw.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.hangw.model.Users;
import com.hangw.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSecurityService implements UserDetailsService{
	private final UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Optional<Users> _user = userRepository.findByEmail(email);
		if(_user.isEmpty()) {
			throw new UsernameNotFoundException("사용자를 탖을 수 없습니다.");
		}
		
		Users user = _user.get();

		// 권한 설정
        List<GrantedAuthority> authorities = new ArrayList<>();
        return new User(user.getEmail(), user.getPassword(), authorities);
	}
	

}
