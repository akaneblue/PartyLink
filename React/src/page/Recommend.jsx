import React, { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCalendar, faLocationDot, faUser } from "@fortawesome/free-solid-svg-icons";

const Recommend = () => {
    const [query, setQuery] = useState("");
    const [recommendations, setRecommendations] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const handleRecommend = () => {
        if (!query.trim()) return;

        setLoading(true);
        setError(null);

        axios
            .get(`/api/crew/recommend?toneQuery=${encodeURIComponent(query)}`, {
                withCredentials: true,
            })
            .then((res) => {
                setRecommendations(res.data.recommend || []);
            })
            .catch(() => {
                setError("추천 요청 중 오류가 발생했습니다.");
            })
            .finally(() => {
                setLoading(false);
            });
    };

    return (
        <main>
            <section className="page-header">
                <div className="container">
                    <h1>모임 추천</h1>
                    <div className="search-filter">
                        <div className="search-box" style={{ width: "100%" }}>
                            <form
                                onSubmit={(e) => {
                                    e.preventDefault();
                                    handleRecommend();
                                }}
                                style={{ display: "flex", flex: 1 }}
                            >
                                <input
                                    type="text"
                                    placeholder="예: 활발하고 에너지 넘치는 모임"
                                    value={query}
                                    onChange={(e) => setQuery(e.target.value)}
                                />
                                <button type="submit">추천받기</button>
                            </form>
                        </div>
                    </div>
                </div>
            </section>

            <section className="meetings-list">
                <div className="container">
                    {loading && <p>로딩 중...</p>}
                    {error && <p className="text-red-500">{error}</p>}
                    {!loading && recommendations.length === 0 && query && <p>추천된 모임이 없습니다.</p>}

                    <div className="meetings-grid">
                        {recommendations.map((crew, index) => (
                            <div
                                className="meeting-card"
                                key={crew.id ? `crew-${crew.id}` : `crew-${index}`}
                                onClick={() => navigate(`/crew/detail?id=${crew.id}`)}
                            >
                                <div className="meeting-content">
                                    <span className="meeting-category">{crew.categoryName}</span><br />
                                    <h3 className="meeting-title">{crew.name}</h3><br />
                                    <div className="meeting-meta">
                                        <span className="meeting-date">
                                            <FontAwesomeIcon icon={faCalendar} />
                                            <span>
                                                {crew.sdate}{crew.edate ? ` ~ ${crew.edate}` : ''}
                                            </span>
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
            </section>
        </main>
    );
};

export default Recommend;
