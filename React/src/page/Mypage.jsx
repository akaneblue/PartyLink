import React, { useState, useEffect, useRef } from 'react';

const Mypage = () => {
    const [user, setUser] = useState(null);
    const [interests, setInterests] = useState([]);
    const [selectedInterestIds, setSelectedInterestIds] = useState([]);

    // 데이터 불러오기
    useEffect(() => {
        // 1단계: 로그인 상태 확인
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


    const handleChange = (e) => {
        setUser({ ...user, [e.target.name]: e.target.value });
    };

    const toggleInterest = (id) => {
        setSelectedInterestIds(prev =>
            prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id]
        );
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        const payload = {
            ...user,
            interestIds: selectedInterestIds
        };

        fetch('/api/user/mypage', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify(payload)
        })
            .then(res => {
                if (res.ok) window.location.href = '/';
                else alert('저장에 실패했습니다.');
            });
    };

    const fileInputRef = useRef();

    const handleFileChange = async (e) => {
        const formData = new FormData();
        formData.append("file", e.target.files[0]);

        const res = await fetch("/api/user/uploadProfile", {
            method: "POST",
            body: formData,
            credentials: "include"
        });

        if (res.ok) {
            const data = await res.json();
            setUser(prev => ({ ...prev, imagePath: data.imagePath }));
        } else {
            alert("업로드 실패");
        }
    };




    if (!user) return <div>로딩 중...</div>;

    return (
        <main>
            <section className="page-header">
                <div className="container">
                    <h1>마이페이지</h1>
                    <p>본인의 정보를 수정할 수 있습니다.</p>
                </div>
            </section>

            <section className="meeting-detail">
                <div className="container">
                    <div id="profile-info" className="tab-content active">
                        <div className="meeting-header">
                            <h2>기본 정보</h2>
                        </div>

                        <div className="meeting-content" style={{ display: 'block' }}>
                            <div className="host-profile" style={{ marginBottom: '30px' }}>
                                <div className="host-avatar">
                                    <img
                                        src={
                                            user.imagePath
                                                ? `http://localhost:8080${user.imagePath}?v=${Date.now()}`
                                                : "http://localhost:8080/placeholder.png"
                                        }
                                        alt="프로필"
                                        style={{ width: 100, height: 100 }}
                                    />


                                </div>
                                <div className="host-details">
                                    <h3>{user.name || '홍길동'}</h3>
                                    <p>{user.email}</p>

                                    {/* 🔥 이게 숨겨진 input */}
                                    <input
                                        type="file"
                                        accept="image/*"
                                        ref={fileInputRef}
                                        onChange={handleFileChange}
                                        style={{ display: "none" }}
                                    />

                                    {/* 🔥 이게 실제로 보이는 버튼 */}
                                    <button
                                        type="button"
                                        className="btn btn-outline"
                                        style={{ marginTop: '10px' }}
                                        onClick={() => fileInputRef.current.click()} // 👈 핵심
                                    >
                                        프로필 사진 변경
                                    </button>
                                </div>

                            </div>

                            <form className="meeting-form" onSubmit={handleSubmit}>
                                <div className="form-group">
                                    <label htmlFor="name">이름</label>
                                    <input type="text" id="name" name="name" value={user.name} onChange={handleChange} placeholder="이름을 입력하세요" />
                                </div>

                                <div className="form-group">
                                    <label htmlFor="nickname">닉네임</label>
                                    <input type="text" id="nickname" name="nickname" value={user.nickname} onChange={handleChange} placeholder="닉네임을 입력하세요" />
                                </div>

                                <div className="form-group">
                                    <label htmlFor="description">자기소개</label>
                                    <textarea id="description" name="description" rows="4" value={user.description} onChange={handleChange} placeholder="자기소개를 입력하세요" />
                                </div>

                                <div className="form-group">
                                    <label htmlFor="birth">생년월일</label>
                                    <input type="date" id="birth" name="birth" value={user.birth || ''} onChange={handleChange} />
                                </div>

                                <div className="form-group">
                                    <label htmlFor="gender">성별</label>
                                    <select id="gender" name="gender" value={user.gender || ''} onChange={handleChange}>
                                        <option value="MALE">남성</option>
                                        <option value="FEMALE">여성</option>
                                    </select>
                                </div>

                                <div className="form-group">
                                    <label htmlFor="location">지역</label>
                                    <input type="text" id="location" name="location" value={user.location} onChange={handleChange} placeholder="지역을 입력하세요" />
                                </div>

                                <div className="form-group">
                                    <label>관심사</label>
                                    <div className="category-filters" id="interest-tags">
                                        {interests.map(interest => (
                                            <button
                                                key={interest.id}
                                                type="button"
                                                className={`filter-btn ${selectedInterestIds.includes(interest.id) ? 'active' : ''}`}
                                                onClick={() => toggleInterest(interest.id)}
                                            >
                                                {interest.name}
                                            </button>
                                        ))}
                                    </div>

                                    <div id="selected-interest-inputs">
                                        {selectedInterestIds.map(id => (
                                            <input key={id} type="hidden" name="interestIds" value={id} />
                                        ))}
                                    </div>
                                </div>

                                <div className="form-actions">
                                    <button type="submit" className="btn btn-primary">저장하기</button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </section>
        </main>
    );
};

export default Mypage;
