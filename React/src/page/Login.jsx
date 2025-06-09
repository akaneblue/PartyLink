import React, { useState } from 'react';
import { FaGoogle } from "react-icons/fa";
import { SiNaver } from "react-icons/si";

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();

    const res = await fetch('/user/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: new URLSearchParams({ email, password }),
      credentials: 'include'
    });

    if (res.redirected) {
      window.location.href = res.url; // 로그인 성공 시 리디렉션
    } else {
      setError(true); // 로그인 실패
    }
  };

  return (
    <main>
      <section className="auth-section">
        <div className="container">
          <div className="auth-container">
            <div className="auth-header">
              <h1>로그인</h1>
              {error && (
                <div className="error-message">
                  이메일 또는 비밀번호가 올바르지 않습니다.
                </div>
              )}
            </div>

            <form className="auth-form" onSubmit={handleSubmit}>
              <div className="form-group">
                <label htmlFor="email">이메일</label>
                <div className="input-with-icon">
                  <i className="fa-regular fa-user"></i>
                  <input
                    type="text"
                    id="email"
                    name="email"
                    placeholder="이메일을 입력하세요"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="password">비밀번호</label>
                <div className="input-with-icon">
                  <i className="fa-solid fa-lock"></i>
                  <input
                    type="password"
                    id="password"
                    name="password"
                    placeholder="비밀번호를 입력하세요"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </div>
              </div>

              <div className="form-options">
                <a href="/user/forgot-password" className="forgot-password">비밀번호 찾기</a>
              </div>

              <button type="submit" className="btn btn-primary btn-block">로그인</button>
            </form>

            <div className="auth-divider">
              <span>또는</span>
            </div>

            <a href="http://localhost:8080/oauth2/authorization/google" className="btn btn-social btn-google">
              <FaGoogle />
              Google로 로그인
            </a>
            <a href="http://localhost:8080/oauth2/authorization/naver" className="btn btn-social btn-naver">
              <SiNaver />
              Naver로 로그인
            </a>

            <div className="auth-footer">
              <p>아직 계정이 없으신가요? <a href="/user/signup" className="auth-link">회원가입</a></p>
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}
