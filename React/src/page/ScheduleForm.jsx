import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import axios from 'axios';

const ScheduleForm = () => {
  const [params] = useSearchParams();
  const crewId = params.get("id");
  const navigate = useNavigate();

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [date, setDate] = useState('');
  const [stime, setStime] = useState('');
  const [etime, setEtime] = useState('');
  const [crewName, setCrewName] = useState('');

  useEffect(() => {
    axios.get(`/api/crew/info?id=${crewId}`)
      .then(res => setCrewName(res.data.name))
      .catch(err => {
        alert(err.response?.data?.message || "접근 권한이 없습니다.");
        navigate(`/api/crew/crewpage?id=${crewId}`);
      });
  }, [crewId, navigate]);

  const handleSubmit = (e) => {
    e.preventDefault();
    axios.post(`http://localhost:8080/api/crew/crewpage/schedule?id=${crewId}`, {
      title,
      description,
      date,
      stime,
      etime
    }, { withCredentials: true })
      .then(() => navigate(`/crew/crewpage?id=${crewId}`))
      .catch(err => alert(err.response?.data?.message || "일정 등록 실패"));
  };

  return (
    <main>
      <section className="page-header">
        <div className="container">
          <h1>일정 작성</h1>
          <p>{crewName} 모임에 등록할 일정을 작성해주세요.</p>
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
                placeholder="일정 제목을 입력하세요"
                value={title}
                onChange={e => setTitle(e.target.value)}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="description">내용</label>
              <textarea
                id="description"
                name="description"
                placeholder="일정 설명을 입력하세요"
                rows="8"
                value={description}
                onChange={e => setDescription(e.target.value)}
                required
              ></textarea>
            </div>

            <div className="form-group">
              <label htmlFor="date">날짜</label>
              <input
                type="date"
                id="date"
                name="date"
                value={date}
                onChange={e => setDate(e.target.value)}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="stime">시작 시간</label>
              <input
                type="time"
                id="stime"
                name="stime"
                value={stime}
                onChange={e => setStime(e.target.value)}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="etime">종료 시간</label>
              <input
                type="time"
                id="etime"
                name="etime"
                value={etime}
                onChange={e => setEtime(e.target.value)}
                required
              />
            </div>

            <div className="form-actions">
              <button type="button" className="btn btn-secondary" onClick={() => navigate(-1)}>취소</button>
              <button type="submit" className="btn btn-primary">일정 등록하기</button>
            </div>
          </form>
        </div>
      </section>
    </main>
  );
};

export default ScheduleForm;
