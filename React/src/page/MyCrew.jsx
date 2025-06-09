import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faLocationDot, faUser, faCalendar, faPlus, faMagnifyingGlass } from "@fortawesome/free-solid-svg-icons";

const MyCrew = () => {
    const [crews, setCrews] = useState([]);
    const [made, setMade] = useState([]);
    const [errorMessage, setErrorMessage] = useState(null);
    const [user, setUser] = useState([]);
    const [interests, setInterests] = useState([]);
    const [interestIds, setSelectedInterestIds] = useState([]);

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
                fetch('/api/crew/mycrew', { credentials: 'include' })
                    .then(res => res.json())
                    .then(data => {
                        setUser(data.user);
                        setInterests(data.interests);
                        setSelectedInterestIds(data.user.interestIds);
                    });
            });
    }, []);

    useEffect(() => {
        axios.get('/api/crew/mycrew')
            .then(response => {
                setCrews(response.data.crews || []);
                setMade(response.data.made || []);
            })
            .catch(error => {
                const msg = error.response?.data?.message || "에러가 발생했습니다.";
                setErrorMessage(msg);
                alert(msg);
            });
    }, []);

    const formatDateRange = (start, end) => {
        const startDate = new Date(start).toLocaleDateString('ko-KR');
        const endDate = end ? new Date(end).toLocaleDateString('ko-KR') : null;
        return endDate ? `${startDate} ~ ${endDate}` : startDate;
    };

    return (
        <main>
            <section className="page-header">
                <div className="container">
                    <h1>내 모임</h1>
                    <p>가입되어 있는 모임을 확인하세요</p>
                </div>
            </section>

            <section className="meeting-detail">
                <div className="container">
                    {/* 내가 만든 모임 */}
                    <div id="my-meetings">
                        <div className="meeting-header">
                            <h2>내가 만든 모임</h2>
                            <a href="/crew/create" className="btn btn-primary">
                                <FontAwesomeIcon icon={faPlus} /> 새 모임 만들기
                            </a>
                        </div>

                        <div className="meetings-grid">
                            {made.map(crew => (
                                <div
                                    className="meeting-card"
                                    key={crew.id}
                                    onClick={() => window.location.href = `/crew/crewpage?id=${crew.id}`}
                                >
                                    <div className="meeting-content">
                                        <span className="meeting-category">{crew.categoryName}</span><br />
                                        <h3 className="meeting-title">{crew.name}</h3><br />
                                        <div className="meeting-meta">
                                            <span className="meeting-date">
                                                <FontAwesomeIcon icon={faCalendar} />
                                                <span>{formatDateRange(crew.sdate, crew.edate)}</span>
                                            </span>
                                            <span className="meeting-location">
                                                <FontAwesomeIcon icon={faLocationDot} />
                                                <span>{crew.location || '장소없음'}</span>
                                            </span>
                                            <span className="meeting-members">
                                                <FontAwesomeIcon icon={faUser} />
                                                <span>{crew.curMembers}/{crew.maxMembers}</span>
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* 참여 중인 모임 */}
                    <div id="joined-meetings">
                        <div className="meeting-header">
                            <h2>참여 중인 모임</h2>
                            <a href="/crew" className="btn btn-primary">
                                <FontAwesomeIcon icon={faMagnifyingGlass} /> 모임 찾기
                            </a>
                        </div>

                        <div className="meetings-grid">
                            {crews.map(crew => (
                                <div
                                    className="meeting-card"
                                    key={crew.id}
                                    onClick={() => window.location.href = `/crew/crewpage?id=${crew.id}`}
                                >
                                    <div className="meeting-content">
                                        <span className="meeting-category">{crew.categoryName}</span><br />
                                        <h3 className="meeting-title">{crew.name}</h3><br />
                                        <div className="meeting-meta">
                                            <span className="meeting-date">
                                                <FontAwesomeIcon icon={faCalendar} />
                                                <span>{formatDateRange(crew.sdate, crew.edate)}</span>
                                            </span>
                                            <span className="meeting-location">
                                                <FontAwesomeIcon icon={faLocationDot} />
                                                <span>{crew.location || '장소없음'}</span>
                                            </span>
                                            <span className="meeting-members">
                                                <FontAwesomeIcon icon={faUser} />
                                                <span>{crew.curMembers}/{crew.maxMembers}</span>
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </section>
        </main>
    );
};

export default MyCrew;
