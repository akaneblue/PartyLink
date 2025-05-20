package com.hangw.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hangw.model.Crews;
import com.hangw.model.Notice;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long>{

	List<Notice> findByCrew(Crews crew);
}
