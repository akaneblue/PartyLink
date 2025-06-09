import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faLocationDot, faUser, faCalendar, faImage, faPlus, faBell } from "@fortawesome/free-solid-svg-icons";
import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import listPlugin from '@fullcalendar/list';
import interactionPlugin from '@fullcalendar/interaction';
import koLocale from '@fullcalendar/core/locales/ko';
import { useSearchParams } from 'react-router-dom';

const CrewPage = () => {
  const [params] = useSearchParams();
  const crewId = params.get("id");
  const [crew, setCrew] = useState(null);
  const [notices, setNotices] = useState([]);
  const [posts, setPosts] = useState([]);
  const [activeTab, setActiveTab] = useState("announcements");

  useEffect(() => {
    axios.get(`/api/crew/crewpage?id=${crewId}`)
      .then(res => {
        setCrew(res.data.crew);
        setNotices(res.data.notices);
        setPosts(res.data.posts);
      })
      .catch(err => {
        alert(err.response?.data?.message || "오류 발생");
        window.location.href = "/crew/mycrew";
      });
  }, [crewId]);

  if (!crew) return null;

  const renderCalendar = () => (
    <FullCalendar
      plugins={[dayGridPlugin, timeGridPlugin, listPlugin, interactionPlugin]}
      initialView="dayGridMonth"
      locale={koLocale}
      height="550px"
      headerToolbar={{
        left: 'prevYear,prev,next,nextYear today',
        center: 'title',
        right: 'dayGridMonth,timeGridWeek,timeGridDay,listWeek',
      }}
      events={{
        url: '/api/crew/calendarList',
        method: 'GET',
        extraParams: { crewId },
        failure: () => alert('일정 데이터를 불러오는 데 실패했습니다.')
      }}
    />
  );

  return (
    <main>
      <section className="meeting-detail">
        <div className="container">
          <div className="meeting-header">
            <div className="meeting-category">{crew.categoryName}</div>
            <h1 className="meeting-title">{crew.name}</h1>
            <div className="meeting-meta">
              <span className="meeting-location"><FontAwesomeIcon icon={faLocationDot} /> {crew.location || "장소없음"}</span>
              <span className="meeting-members"><FontAwesomeIcon icon={faUser} /> {crew.curMembers}/{crew.maxMembers}</span>
            </div>
          </div>

          <div className="filter-options" id="group-tabs">
            <button className={`filter-btn ${activeTab === "announcements" ? "active" : ""}`} onClick={() => setActiveTab("announcements")}>공지사항</button>
            <button className={`filter-btn ${activeTab === "schedule" ? "active" : ""}`} onClick={() => setActiveTab("schedule")}>일정</button>
            <button className={`filter-btn ${activeTab === "gallery" ? "active" : ""}`} onClick={() => setActiveTab("gallery")}>게시판</button>
          </div>

          {/* 공지사항 */}
          {activeTab === "announcements" && (
            <div className="meeting-content">
              <h2>공지사항</h2>
              <a href={`/crew/crewpage/notice?id=${crewId}`} className="btn btn-primary">
                <FontAwesomeIcon icon={faPlus} /> 공지 작성
              </a>
              <div>
                {notices.length > 0 ? (
                  notices.map(n => (
                    <div key={n.id}>
                      <h3>{n.title}</h3>
                      <p style={{ whiteSpace: 'pre-wrap' }}>{n.content}</p>
                      <span>{n.created}</span>
                      <hr />
                    </div>
                  ))
                ) : (
                  <div className="empty-state">
                    <FontAwesomeIcon icon={faBell} />
                    <h3>공지 없음</h3>
                    <p>아직 쓰여진 공지가 없습니다.</p>
                  </div>
                )}
              </div>

            </div>
          )}

          {/* 일정 */}
          {activeTab === "schedule" && (
            <div className="tab-content active">
              <h2>모임 일정</h2>
              <a href={`/crew/crewpage/schedule?id=${crewId}`} className="btn btn-primary">
                <FontAwesomeIcon icon={faPlus} /> 일정 작성
              </a>
              <div style={{ marginTop: 20 }}>{renderCalendar()}</div>
            </div>
          )}

          {/* 갤러리 */}
          {activeTab === "gallery" && (
            <div className="tab-content active">
              <h2>게시판</h2>
              <a href={`/crew/crewpage/posts?id=${crewId}`} className="btn btn-primary">
                <FontAwesomeIcon icon={faPlus} /> 글 작성
              </a>
              <div className="meeting-content">
                <div>
                  {posts.length > 0 ? posts.map(p => (
                    <div className="gallery-card" key={p.id}>
                      <a href={`/crew/crewpage/post?id=${p.id}`}>
                        <h3>{p.title}</h3>
                      </a>
                      <hr />
                    </div>
                  )) : (
                    <div className="empty-state">
                      <FontAwesomeIcon icon={faImage} />
                      <h3>게시글 없음</h3>
                      <p>아직 등록된 게시글이 없습니다.</p>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}
        </div>
      </section>
    </main>
  );
};

export default CrewPage;
