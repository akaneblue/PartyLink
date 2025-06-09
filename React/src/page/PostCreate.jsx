import React, { useEffect, useRef, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import $ from 'jquery';
import 'summernote/dist/summernote-lite.css';
import 'summernote/dist/summernote-lite.js';

const PostCreate = () => {
  const [params] = useSearchParams();
  const crewId = params.get('id');
  const navigate = useNavigate();
  const editorRef = useRef(null);
  const [title, setTitle] = useState('');
  const [crewName, setCrewName] = useState('');

  useEffect(() => {
    axios.get(`/api/crew/info?id=${crewId}`)
      .then(res => setCrewName(res.data.name))
      .catch(() => setCrewName(''));

    $(editorRef.current).summernote({
      height: 300,
      toolbar: [
        ['style', ['bold', 'italic', 'underline', 'clear']],
        ['insert', ['picture', 'link', 'video']],
        ['para', ['ul', 'ol', 'paragraph']],
        ['view', ['codeview']]
      ],
      callbacks: {
        onImageUpload: function (files) {
          for (let i = 0; i < files.length; i++) {
            uploadImage(files[i]);
          }
        }
      }
    });
  }, [crewId]);

  const uploadImage = (file) => {
    const formData = new FormData();
    formData.append('file', file);

    $.ajax({
      url: '/api/crew/crewpage/posts/uploadImage',
      type: 'POST',
      data: formData,
      contentType: false,
      processData: false,
      xhrFields: { withCredentials: true },
      success: function (url) {
        const fullUrl = `http://localhost:8080${url}`;
        $(editorRef.current).summernote('insertImage', fullUrl);
      },
      error: function () {
        alert('이미지 업로드 실패');
      }
    });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const content = $(editorRef.current).summernote('code');

    axios.post(`/api/crew/crewpage/posts?id=${crewId}`, {
      title,
      content
    }, { withCredentials: true })
      .then(() => {
        navigate(`/crew/crewpage?id=${crewId}`);
      })
      .catch(err => {
        alert(err.response?.data?.message || '게시글 등록 실패');
      });
  };

  return (
    <main>
      <section className="page-header">
        <div className="container">
          <h1>게시글 작성</h1>
          <p>{crewName} 모임에 등록할 게시글을 작성해주세요.</p>
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
                placeholder="게시글 제목을 입력하세요"
                className="form-control"
                value={title}
                onChange={e => setTitle(e.target.value)}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="content">내용</label>
              <textarea
                id="summernote"
                name="content"
                ref={editorRef}
              ></textarea>
            </div>

            <div className="form-actions">
              <button type="button" className="btn btn-secondary" onClick={() => navigate(-1)}>취소</button>
              <button type="submit" className="btn btn-primary">게시글 등록하기</button>
            </div>
          </form>
        </div>
      </section>
    </main>
  );
};

export default PostCreate;
