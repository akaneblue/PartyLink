import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faLocationDot, faUser, faCalendar} from "@fortawesome/free-solid-svg-icons";

export default function Home() {
  const [crewsList, setCrewsList] = useState([]);
  const [currentPage, setCurrentPage] = useState(0);
  const navigate = useNavigate();

  useEffect(() => {
    fetch(`/api/home?page=${currentPage}`)
      .then(res => {
        if (!res.ok) throw new Error("응답 실패");
        return res.json();
      })
      .then(data => {
        setCrewsList(data.crewsList || []); 
        setCurrentPage(data.currentPage || 0);
      })
      .catch(err => {
        console.error("API 오류:", err);
        setCrewsList([]);
      });
  }, [currentPage]);

  useEffect(() => {
    fetch(`/api/home?page=${currentPage}`)
      .then(res => res.json())
      .then(data => {
        setCrewsList(data.crewsList);
        setCurrentPage(data.currentPage);
      });
  }, [currentPage]);

  return (
    <main>
      <section className="hero">
        <div className="container">
          <div className="hero-content">
            <h1 className="hero-title">취미도, 공부도<br />함께하면<br />더 즐겁다!</h1>
          </div>
        </div>
      </section>

      <section className="featured-meetings">
        <div className="container">
          <div className="meetings-grid">
            {crewsList.map((crew) => (
              <div
                className="meeting-card"
                key={crew.id}
                onClick={() => navigate(`/crew/detail?id=${crew.id}`)}
              >
                <div className="meeting-content">
                  <span className="meeting-category">{crew.categoryName}</span><br />
                  <h3 className="meeting-title">{crew.name}</h3><br />
                  <div className="meeting-meta">
                    <span className="meeting-date">
                      <FontAwesomeIcon icon={faCalendar} />
                      <span>{crew.sdate} {crew.edate ? `~ ${crew.edate}` : ''}</span>
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

          <div className="pagination">
            {currentPage > 0 && (
              <button onClick={() => setCurrentPage(currentPage - 1)} className="page-link">
                <i className="fa-solid fa-chevron-left"></i>
              </button>
            )}
            {crewsList.length === 3 && (
              <button onClick={() => setCurrentPage(currentPage + 1)} className="page-link">
                <i className="fa-solid fa-chevron-right"></i>
              </button>
            )}
          </div>
        </div>
      </section>

      <section className="cta-section">
        <div className="container">
          <div className="cta-content">
            <h2>나만의 모임을 만들어보세요</h2>
            <p>관심사가 비슷한 사람들과 함께하는 특별한 경험</p>
            <a href="/crew/create" className="btn btn-primary">모임 만들기</a>
          </div>
        </div>
      </section>
    </main>
  );
}
