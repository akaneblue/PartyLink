import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCalendar} from "@fortawesome/free-solid-svg-icons";

const PostDetail = () => {
  const [params] = useSearchParams();
  const postId = params.get('id');
  const [post, setPost] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    axios
      .get(`/api/crew/crewpage/post?id=${postId}`, { withCredentials: true })
      .then((res) => {
        setPost(res.data.post);
      })
      .catch((err) => {
        alert(err.response?.data?.message || '게시글을 불러오지 못했습니다.');
        navigate(-1);
      });
  }, [postId, navigate]);

  if (!post) return null;

  return (
    <main>
      <section className="meeting-detail">
        <div className="container">
          <div className="meeting-header">
            <h1 className="meeting-title">{post.title}</h1>
            <div className="meeting-meta">
              <span className="meeting-date">
                <FontAwesomeIcon icon={faCalendar} />
                <span>{post.created}</span>
              </span>
            </div>
          </div>

          <div className="meeting-content">
            <div
              className="meeting-description post-content"
              dangerouslySetInnerHTML={{ __html: post.content }}
            />
          </div>

          <div className="meeting-actions">
            <button
              className="btn btn-secondary"
              onClick={() => navigate(-1)}
            >
              뒤로가기
            </button>
          </div>
        </div>
      </section>
    </main>
  );
};

export default PostDetail;
