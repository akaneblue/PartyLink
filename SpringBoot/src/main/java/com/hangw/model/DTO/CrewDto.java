package com.hangw.model.DTO;

import com.hangw.model.Crews;

public record CrewDto(
	    long id,
	    String name,
	    int maxMembers,
	    int curMembers,
	    String description,
	    String sdate,
	    String edate,
	    String time,
	    String created,
	    String categoryName,
	    String location,
	    String imagePath
	) {
	    public static CrewDto from(Crews crew) {
	        return new CrewDto(
	            crew.getId(),
	            crew.getName(),
	            crew.getMax_members(),
	            crew.getCur_members(),
	            crew.getDescription(),
	            crew.getSdate() != null ? crew.getSdate().toString() : null,
	            crew.getEdate() != null ? crew.getEdate().toString() : null,
	            crew.getTime() != null ? crew.getTime().toString() : null,
	            crew.getCreated() != null ? crew.getCreated().toString() : null,
	            crew.getCategory() != null ? crew.getCategory().getName() : null,
	            crew.getLocation(),
	            crew.getImagePath()
	        );
	    }
	}

