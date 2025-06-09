import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const CrewCreate = () => {
    const [form, setForm] = useState({
        name: '',
        type: '',
        description: '',
        date: '',
        date2: '',
        time: '',
        maxParticipants: 10,
        gender: 'ANY',
        minAge: '',
        maxAge: '',
        location: '',
    });
    const [imageFile, setImageFile] = useState(null);
    const [showConditions, setShowConditions] = useState(false);
    const [showDate2, setShowDate2] = useState(false);
    const [errors, setErrors] = useState([]);
    const [user, setUser] = useState([]);
    const [interests, setInterests] = useState([]);
    const [selectedInterestIds, setSelectedInterestIds] = useState([]);

    useEffect(() => {
        fetch('/api/user/status', { credentials: 'include' })
            .then(res => res.json())
            .then(status => {
                if (!status.isLoggedIn) {
                    window.location.href = '/user/login';
                    return;
                }

                // 2단계: 유저 정보 로드
                fetch('/api/user/mypage', { credentials: 'include' })
                    .then(res => res.json())
                    .then(data => {
                        setUser(data.user);
                        setInterests(data.interests);
                        setSelectedInterestIds(data.user.interestIds);
                    });
            });
    }, []);


    useEffect(() => {
        setShowDate2(form.type === '정기');
    }, [form.type]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    const handleFileChange = (e) => {
        setImageFile(e.target.files[0]);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const formData = new FormData();
        for (const key in form) {
            formData.append(key, form[key]);
        }
        if (imageFile) {
            formData.append('imageFile', imageFile);
        }

        try {
            const res = await axios.post('/api/crew/create', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });

            alert(res.data.message);
            navigate('/');
        } catch (err) {
            if (err.response?.data?.errors) {
                setErrors(err.response.data.errors);
            } else if (err.response?.data?.error) {
                alert(err.response.data.error);
            }
        }
    };


    const navigate = useNavigate();

    return (
        <main>
            <section className="page-header">
                <div className="container">
                    <h1>모임 만들기</h1>
                    <p>관심사가 비슷한 사람들과 함께할 수 있는 모임을 만들어보세요.</p>
                </div>
            </section>

            <section className="create-meeting">
                <div className="container">
                    <form className="meeting-form" onSubmit={handleSubmit} encType="multipart/form-data">
                        <div className="form-group">
                            <label htmlFor="name">모임 이름</label>
                            <input type="text" id="name" name="name" placeholder="모임의 제목을 입력하세요" onChange={handleChange} required />
                        </div>

                        <div className="form-group">
                            <label htmlFor="type">형식</label>
                            <select id="type" name="type" value={form.type} onChange={handleChange} required>
                                <option value="">카테고리 선택</option>
                                <option value="단발">단발성 모임</option>
                                <option value="정기">정기 모임</option>
                            </select>
                        </div>

                        <div className="form-group">
                            <label htmlFor="description">모임 설명</label>
                            <textarea id="description" name="description" rows="5" placeholder="모임에 대한 설명을 입력하세요" onChange={handleChange} required />
                        </div>

                        <div className="form-row">
                            <div className="form-group">
                                <label htmlFor="date">날짜</label>
                                <input type="date" id="date" name="date" onChange={handleChange} required />
                            </div>

                            {showDate2 && (
                                <div className="form-group" id="date2-group">
                                    <label htmlFor="date2">종료 날짜</label>
                                    <input type="date" id="date2" name="date2" onChange={handleChange} />
                                </div>
                            )}

                            <div className="form-group">
                                <label htmlFor="time">시간</label>
                                <input type="time" id="time" name="time" onChange={handleChange} required />
                            </div>
                        </div>

                        <div className="form-group">
                            <label htmlFor="imageFile">모임 배경 이미지</label>
                            <input type="file" id="imageFile" name="imageFile" onChange={handleFileChange} accept="image/*" />
                        </div>

                        <div className="form-group">
                            <label htmlFor="maxParticipants">최대 참가자 수</label>
                            <input type="number" id="maxParticipants" name="maxParticipants" min="2" max="100" value={form.maxParticipants} onChange={handleChange} required />
                        </div>

                        <div className="form-group">
                            <button type="button" className="btn btn-outline" onClick={() => setShowConditions(!showConditions)}>
                                조건 추가하기
                            </button>
                        </div>

                        {showConditions && (
                            <div id="condition-section" style={{ border: '1px solid #ddd', padding: '15px', borderRadius: '10px', marginBottom: '20px' }}>
                                <h4>모집 조건</h4>

                                <div className="form-group">
                                    <label htmlFor="gender">성별 조건</label>
                                    <select id="gender" name="gender" value={form.gender} onChange={handleChange}>
                                        <option value="ANY">무관</option>
                                        <option value="MALE">남성만</option>
                                        <option value="FEMALE">여성만</option>
                                    </select>
                                </div>

                                <div className="form-row">
                                    <div className="form-group">
                                        <label htmlFor="minAge">최소 나이</label>
                                        <input type="number" id="minAge" name="minAge" onChange={handleChange} />
                                    </div>
                                    <div className="form-group">
                                        <label htmlFor="maxAge">최대 나이</label>
                                        <input type="number" id="maxAge" name="maxAge" onChange={handleChange} />
                                    </div>
                                </div>

                                <div className="form-group">
                                    <label htmlFor="location">허용 지역</label>
                                    <input type="text" id="location" name="location" placeholder="예: 서울 강남구" onChange={handleChange} />
                                </div>
                            </div>
                        )}

                        {errors.length > 0 && (
                            <div className="alert alert-danger">
                                {errors.map((err, idx) => (
                                    <div key={idx}>{err}</div>
                                ))}
                            </div>
                        )}

                        <div className="form-actions">
                            <button type="button" className="btn btn-secondary" onClick={() => navigate(-1)}>취소</button>
                            <button type="submit" className="btn btn-primary">모임 생성하기</button>
                        </div>
                    </form>
                </div>
            </section>
        </main>
    );
};

export default CrewCreate;
