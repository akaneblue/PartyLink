package com.hangw.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hangw.model.Crews;
import com.hangw.model.Notification;
import com.hangw.model.Users;

public interface NotificationRepository extends JpaRepository<Notification, Long>{

	public List<Notification> findByUser(Users user);

	public List<Notification> findByUserAndCrewAndIsReadFalse(Users user, Crews crew);
}
