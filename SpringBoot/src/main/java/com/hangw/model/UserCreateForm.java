package com.hangw.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateForm {				//회원가입 화면에서 입력이 제대로 됐는지 확인할때 사용
	@NotEmpty(message = "이름은 필수항목입니다.")
	private String userName;
	
	@NotEmpty(message = "password는 필수항목입니다.")
	private String password1;
	
	@NotEmpty(message = "password 확인은 필수항목입니다.")
	private String password2;
	
	@NotEmpty(message = "email은 필수항목입니다.")
	@Email
	private String email;
}
