import React, { useEffect, useState } from "react";
import axios from "axios";
import { useParams, useSearchParams, useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCalendar, faClock, faLocationDot, faUser } from "@fortawesome/free-solid-svg-icons";

const CrewDetail = () => {
    const [searchParams] = useSearchParams();
    const crewId = searchParams.get("id");
    const navigate = useNavigate();

    const [crew, setCrew] = useState(null);
    const [members, setMembers] = useState([]);
    const [leader, setLeader] = useState(null);

    useEffect(() => {
        axios.get(`/api/crew/detail?id=${crewId}`)
            .then(res => {
                setCrew(res.data.crew);
                setMembers(res.data.members || []);
                setLeader(res.data.leader);
            })
            .catch(err => {
                console.error(err);
                alert("모임 정보를 불러오지 못했습니다.");
            });
    }, [crewId]);

    if (!crew) return <div>Loading...</div>;

    return (
        <main>
            <section className="meeting-detail">
                <div className="container">
                    <div className="meeting-header">
                        <div className="meeting-category">{crew.categoryName}</div>
                        <h1 className="meeting-title">{crew.name}</h1>
                        <div className="meeting-meta">
                            <span className="meeting-date">
                                <FontAwesomeIcon icon={faCalendar} />
                                <span>
                                    {crew.sdate}
                                    {crew.edate ? ` ~ ${crew.edate}` : ''}
                                </span>
                            </span>
                            <span className="meeting-time">
                                <FontAwesomeIcon icon={faClock} />
                                <span>{crew.time?.substring(0, 5)}</span>
                            </span>
                            <span className="meeting-location">
                                <FontAwesomeIcon icon={faLocationDot} />
                                <span>{crew.location || '장소없음'}</span>
                            </span>
                            <span className="meeting-members">
                                <FontAwesomeIcon icon={faUser} />
                                <span>{crew.curMembers} / {crew.maxMembers}</span>
                            </span>
                        </div>
                    </div>

                    <div className="meeting-content">
                        {crew.imagePath && (
                            <div className="meeting-image1">
                                <img src={`http://localhost:8080${crew.imagePath}`} alt="모임 이미지" />
                            </div>
                        )}


                        <div className="meeting-description">
                            <h2>모임 소개</h2>
                            <p style={{ whiteSpace: "pre-wrap" }}>{crew.description}</p>
                        </div>

                        <div className="host-info">
                            <h2>주최자 정보</h2>
                            <div className="host-profile">
                                <div className="host-avatar">
                                    <FontAwesomeIcon icon={faUser} />
                                </div>
                                <div
                                    className="host-details"
                                    onClick={() => navigate(`/user/userdetail?userId=${leader.id}`)}
                                    style={{ cursor: "pointer" }}
                                >
                                    <h3>{leader.nickname || leader.name}</h3>
                                    <p>{leader.description}</p>
                                </div>
                            </div>
                        </div>

                        <div className="participants">
                            <h2>참가자 <span>{crew.curMembers} / {crew.maxMembers}</span></h2>
                            <div className="participants-list">
                                {members.map((member, index) => (
                                    <div className="participant" onClick={() => navigate(`/user/userdetail?userId=${member.id}`)} key={index}>
                                        <div className="participant-avatar">
                                            <FontAwesomeIcon icon={faUser} />
                                        </div>
                                        <div className="participant-name">
                                            {member.nickname || member.name}
                                        </div>

                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>

                    <div className="meeting-actions">
                        <a href={`/appli/create?groupId=${crew.id}`} className="btn btn-primary">
                            참가 신청하기
                        </a>
                    </div>
                </div>
            </section>
        </main>
    );
};

export default CrewDetail;
