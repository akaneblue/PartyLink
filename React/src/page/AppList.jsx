import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faUserGroup, faEnvelopeOpen, faPaperPlane } from "@fortawesome/free-solid-svg-icons";

const AppList = () => {
    const [groupedApps, setGroupedApps] = useState({});
    const [sent, setSent] = useState([]);
    const [activeTab, setActiveTab] = useState('received-applications');
    const [user, setUser] = useState([]);

    useEffect(() => {
        fetch('/api/user/status', { credentials: 'include' })
            .then(res => res.json())
            .then(status => {
                if (!status.isLoggedIn) {
                    window.location.href = '/user/login';
                } else {
                    fetch('/api/appli', { credentials: 'include' })
                        .then(res => res.json())
                        .then(data => {
                            setUser(data.user);
                        });
                }
            });
    }, []);


    useEffect(() => {
        axios.get('/api/appli', { withCredentials: true })
            .then(response => {
                setGroupedApps(response.data.groupedApps || {});
                setSent(response.data.sent || []);
            })
            .catch(error => {
                alert(error.response?.data?.message || '신청 정보를 불러오지 못했습니다.');
            });
    }, []);

    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' });
    };

    const handleApplicationAction = (type, appId) => {
        const url = `/api/appli/${type}?appId=${appId}`;
        axios.post(url, null, { withCredentials: true })
            .then(res => {
                alert(res.data.message);
                window.location.reload();
            })
            .catch(err => {
                alert(err.response?.data?.message || '요청 처리 중 오류가 발생했습니다.');
            });
    };



    return (
        <main>
            <section className="page-header">
                <div className="container">
                    <h1>신청 관리</h1>
                    <p>모임 참가 신청 내역을 확인하고 관리하세요</p>
                </div>
            </section>

            <section className="meeting-detail">
                <div className="container">
                    {/* 탭 메뉴 */}
                    <div className="filter-options" id="applications-tabs">
                        <button
                            className={`filter-btn ${activeTab === 'received-applications' ? 'active' : ''}`}
                            onClick={() => setActiveTab('received-applications')}
                        >
                            받은 신청
                        </button>
                        <button
                            className={`filter-btn ${activeTab === 'sent-applications' ? 'active' : ''}`}
                            onClick={() => setActiveTab('sent-applications')}
                        >
                            보낸 신청
                        </button>
                    </div>

                    {/* 받은 신청 탭 */}
                    <div id="received-applications" className={`tab-content ${activeTab === 'received-applications' ? 'active' : ''}`}>
                        <div className="meeting-header">
                            <h2>받은 신청</h2>
                            <p>내 모임에 참가하고 싶어하는 사용자들의 신청입니다</p>
                        </div>

                        {Object.keys(groupedApps).length === 0 ? (
                            <div className="empty-state">
                                <FontAwesomeIcon icon={faUserGroup} />
                                <h3>모임이 없습니다</h3>
                                <p>아직 본인이 생성한 모임이 없습니다.</p>
                                <a href="/meetings/create" className="btn btn-primary">모임 만들기</a>
                            </div>
                        ) : (
                            Object.entries(groupedApps).map(([crewName, apps]) => (
                                <div key={crewName} className="application-group">
                                    <h3>{crewName} 모임</h3>
                                    {apps.length === 0 ? (
                                        <div className="empty-state">
                                            <FontAwesomeIcon icon={faEnvelopeOpen} />
                                            <h3>신청자 없음</h3>
                                            <p>아직 이 모임에 참가 신청한 사용자가 없습니다.</p>
                                        </div>
                                    ) : (
                                        apps.map(app => (
                                            <div key={app.id} className="application-item">
                                                <div
                                                    className="application-user"
                                                    onClick={() => window.location.href = `/user/userdetail?userId=${app.userId}`}
                                                >
                                                    <div className="user-avatar">
                                                        <img src={`http://localhost:8080${app.userImage}?width=60&height=60`} alt="사용자 프로필" />
                                                    </div>
                                                    <div className="user-info">
                                                        <h3 className="user-name">{app.userNickname}</h3>
                                                        <p className="application-date">신청일: {formatDate(app.createdAt)}</p>
                                                    </div>
                                                </div>
                                                <div className="application-meeting">
                                                    <h3 className="meeting-title">{app.groupName}</h3>
                                                </div>
                                                <div className="application-message">
                                                    <h4>신청 메시지</h4>
                                                    <p>{app.content}</p>
                                                </div>
                                                <div className="application-actions">
                                                    <button className="btn btn-primary" onClick={() => handleApplicationAction('accept', app.id)}>
                                                        승인하기
                                                    </button>
                                                    <button className="btn btn-secondary" onClick={() => handleApplicationAction('reject', app.id)}>
                                                        거절하기
                                                    </button>
                                                </div>

                                            </div>
                                        ))
                                    )}
                                </div>
                            ))
                        )}
                    </div>

                    {/* 보낸 신청 탭 */}
                    <div id="sent-applications" className={`tab-content ${activeTab === 'sent-applications' ? 'active' : ''}`}>
                        <div className="meeting-header">
                            <h2>보낸 신청</h2>
                            <p>내가 참가하고 싶어 신청한 모임 목록입니다</p>
                        </div>
                        <div className="application-list">
                            {sent.length === 0 ? (
                                <div className="empty-state">
                                    <FontAwesomeIcon icon={faPaperPlane} />
                                    <h3>보낸 신청이 없습니다</h3>
                                    <p>아직 참가 신청한 모임이 없습니다.</p>
                                    <a href="/crew" className="btn btn-primary">모임 찾아보기</a>
                                </div>
                            ) : (
                                sent.map(app => (
                                    <div key={app.id} className="application-item">
                                        <div className="application-meeting">
                                            <div className="meeting-info">
                                                <h3 className="meeting-title">{app.groupName}</h3>
                                                <p className="meeting-host">주최자: <span>{app.leader}</span></p>
                                            </div>
                                        </div>
                                        <div className="application-message">
                                            <h4>내 신청 메시지</h4>
                                            <p>{app.content}</p>
                                        </div>
                                        <div className={`application-status ${app.status === 'Accepted' ? 'status-approved' : app.status === 'Rejected' ? 'status-rejected' : 'status-pending'}`}>
                                            <span className="status-badge">{app.status}</span>
                                        </div>
                                        <p className="status-date">신청일: {formatDate(app.createdAt)}</p>
                                        {app.status === 'Processing' && (
                                            <button className="btn btn-outline" onClick={() => handleApplicationAction('cancel', app.id)}>
                                                신청 취소
                                            </button>
                                        )}
                                    </div>
                                ))
                            )}
                        </div>
                    </div>
                </div>
            </section>
        </main>
    );
};

export default AppList;
