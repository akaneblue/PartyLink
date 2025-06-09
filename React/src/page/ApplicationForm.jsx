import React, { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import axios from 'axios';

const ApplicationForm = () => {
  const [intro, setIntro] = useState('');
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const groupId = searchParams.get('groupId'); // URL에서 groupId 파라미터 읽기

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const form = new FormData();
      form.append('intro', intro);
      form.append('groupId', groupId);

      const res = await axios.post('/api/appli/create', form, {
        withCredentials: true,
      });

      alert('신청이 완료되었습니다.');
      navigate('/'); // 신청 후 이동할 페이지
    } catch (err) {
      const message = err.response?.data?.message || '신청 중 오류가 발생했습니다.';
      alert(message);
    }
  };

  return (
    <main>
      <section className="page-header">
        <div className="container">
          <h1>모임 신청서 작성</h1>
          <p>모임에 참여하고 싶다면 신청서를 작성해주세요.</p>
        </div>
      </section>

      <section className="create-application">
        <div className="container">
          <form className="meeting-form" onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="intro">자기소개</label>
              <textarea
                id="intro"
                name="intro"
                rows="5"
                placeholder="자신을 소개해주세요"
                value={intro}
                onChange={(e) => setIntro(e.target.value)}
                required
              />
            </div>
            <input type="hidden" name="groupId" value={groupId || ''} />
            <div className="form-actions">
              <button type="button" className="btn btn-secondary" onClick={() => navigate(-1)}>
                취소
              </button>
              <button type="submit" className="btn btn-primary">신청하기</button>
            </div>
          </form>
        </div>
      </section>
    </main>
  );
};

export default ApplicationForm;
