import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faUser, faEnvelope, faLock } from "@fortawesome/free-solid-svg-icons";
import { FaGoogle } from "react-icons/fa";
import { SiNaver } from "react-icons/si";

const Signup = () => {
    const [form, setForm] = useState({
        userName: '',
        email: '',
        password1: '',
        password2: ''
    });
    const [errors, setErrors] = useState([]);
    const navigate = useNavigate();

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrors([]);
        
        const res = await fetch('/api/user/signup', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify(form),
        });

        if (res.ok) {
            navigate('/');
        } else {
            const data = await res.json();
            setErrors(data.errors || [data.message]);
        }
    };


    return (
        <main>
            <section className="auth-section">
                <div className="container">
                    <div className="auth-container">
                        <div className="auth-header">
                            <h1>회원가입</h1>
                            <p>회원이 되어 다양한 모임에 참여해보세요</p>
                        </div>

                        <form className="auth-form" onSubmit={handleSubmit}>
                            {/* 오류 메시지 출력 */}
                            {errors.length > 0 && (
                                <div>
                                    {errors.map((err, idx) => (
                                        <div key={idx} className="error-message">{err}</div>
                                    ))}
                                </div>
                            )}

                            <div className="form-group">
                                <label htmlFor="userName">이름</label>
                                <div className="input-with-icon">
                                    <FontAwesomeIcon icon={faUser} />
                                    <input
                                        type="text"
                                        id="userName"
                                        name="userName"
                                        placeholder="이름을 입력하세요"
                                        value={form.userName}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                            </div>

                            <div className="form-group">
                                <label htmlFor="email">이메일</label>
                                <div className="input-with-icon">
                                    <FontAwesomeIcon icon={faEnvelope} />
                                    <input
                                        type="email"
                                        id="email"
                                        name="email"
                                        placeholder="이메일을 입력하세요"
                                        value={form.email}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                            </div>

                            <div className="form-group">
                                <label htmlFor="password1">비밀번호</label>
                                <div className="input-with-icon">
                                    <FontAwesomeIcon icon={faLock} />
                                    <input
                                        type="password"
                                        id="password1"
                                        name="password1"
                                        placeholder="비밀번호를 입력하세요"
                                        value={form.password1}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                            </div>

                            <div className="form-group">
                                <label htmlFor="password2">비밀번호 확인</label>
                                <div className="input-with-icon">
                                    <FontAwesomeIcon icon={faLock} />
                                    <input
                                        type="password"
                                        id="password2"
                                        name="password2"
                                        placeholder="비밀번호를 다시 입력하세요"
                                        value={form.password2}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                            </div>

                            <button type="submit" className="btn btn-primary btn-block">회원가입</button>
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
                            <p>이미 계정이 있으신가요? <a href="/user/login" className="auth-link">로그인</a></p>
                        </div>
                    </div>
                </div>
            </section>
        </main>
    );
};

export default Signup;
