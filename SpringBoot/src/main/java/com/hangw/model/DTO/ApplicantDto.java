package com.hangw.model.DTO;

import java.time.LocalDateTime;

import com.hangw.model.Applicants;

public record ApplicantDto(
    long id,
    Long groupId,
    String groupName,
    String leader,
    Long userId,
    String userNickname,
    String userImage,
    String content,
    LocalDateTime createdAt,
    String status
) {
    public static ApplicantDto from(Applicants applicant) {
        return new ApplicantDto(
            applicant.getId(),
            applicant.getGroup() != null ? applicant.getGroup().getId() : null,
            applicant.getGroup() != null ? applicant.getGroup().getName() : null,
            applicant.getGroup().getLeader().getNickname() != null ? applicant.getGroup().getLeader().getNickname() : applicant.getGroup().getLeader().getName(),
            applicant.getUser() != null ? applicant.getUser().getId() : null,
            applicant.getUser() != null ? 
                (applicant.getUser().getNickname() != null && !applicant.getUser().getNickname().isBlank()
                    ? applicant.getUser().getNickname()
                    : applicant.getUser().getName())
                : null,
            applicant.getUser().getImagePath() != null && applicant.getUser().getImagePath() != "" ? applicant.getUser().getImagePath() : "/placeholder.png",
            applicant.getContent(),
            applicant.getCreatedAt(),
            applicant.getStatus() != null ? applicant.getStatus().name() : null
        );
    }
}

