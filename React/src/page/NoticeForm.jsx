import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import axios from 'axios';

const NoticeForm = () => {
    const [params] = useSearchParams();
    const crewId = params.get("id");
    const navigate = useNavigate();

    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [crewName, setCrewName] = useState('');

    useEffect(() => {
        // 공지 작성 권한 확인 + 모임 정보 로드
        axios.get(`/api/crew/info?id=${crewId}`)
            .then(res => {
                setCrewName(res.data.name);
            })
            .catch(err => {
                alert(err.response?.data?.message || "접근 권한이 없습니다.");
                navigate(`/api/crew/crewpage?id=${crewId}`);
            });
    }, [crewId, navigate]);

    const handleSubmit = (e) => {
        e.preventDefault();
        axios.post(`http://localhost:8080/api/crew/crewpage/notice?id=${crewId}`, { title, content }, {
            withCredentials: true
        })
            .then(() => {
                navigate(`/crew/crewpage?id=${crewId}`);
            })
            .catch(err => {
                alert(err.response?.data?.message || "공지 등록에 실패했습니다.");
            });
    };

    return (
        <main>
            <section className="page-header">
                <div className="container">
                    <h1>공지 작성</h1>
                    <p>{crewName} 모임의 인원들이 알아야 할 공지사항을 작성해주세요</p>
                </div>
            </section>

            <section className="create-meeting">
                <div className="container">
                    <form className="meeting-form" onSubmit={handleSubmit}>
                        <input type="hidden" name="crewId" value={crewId} />

                        <div className="form-group">
                            <label htmlFor="title">제목</label>
                            <input
                                type="text"
                                id="title"
                                name="title"
                                placeholder="공지 제목을 입력하세요"
                                value={title}
                                onChange={(e) => setTitle(e.target.value)}
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="content">내용</label>
                            <textarea
                                id="content"
                                name="content"
                                placeholder="공지 내용을 입력하세요"
                                rows="6"
                                value={content}
                                onChange={(e) => setContent(e.target.value)}
                                required
                            />
                        </div>

                        <div className="form-actions">
                            <button type="button" className="btn btn-secondary" onClick={() => navigate(-1)}>취소</button>
                            <button type="submit" className="btn btn-primary">공지 작성하기</button>
                        </div>
                    </form>
                </div>
            </section>
        </main>
    );
};

export default NoticeForm;
