import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faLocationDot, faUser, faCalendar } from "@fortawesome/free-solid-svg-icons";

const UserDetail = () => {
    const [searchParams] = useSearchParams();
    const userId = searchParams.get('userId');

    const [user, setUser] = useState(null);
    const [hostedMeetings, setHostedMeetings] = useState([]);
    const [reviews, setReviews] = useState([]);
    const [rating, setRating] = useState(0);
    const [content, setContent] = useState("");

    useEffect(() => {
        fetch('/api/user/status', { credentials: 'include' })
            .then(res => res.json())
            .then(status => {
                if (!status.isLoggedIn) {
                    window.location.href = '/user/login';
                    return;
                }

                fetch(`/api/user/userdetail?userId=${userId}`, { credentials: 'include' })
                    .then(res => res.json())
                    .then(data => {
                        setUser(data.user);
                        setHostedMeetings(data.hostedMeetings);
                        setReviews(data.reviews);
                    });
            });
    }, [userId]);

    const handleReviewSubmit = async (e) => {
        e.preventDefault();

        const res = await fetch('/api/review/add', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ userId: Number(userId), rating, content })
        });

        if (res.ok) {
            const updatedRes = await fetch(`/api/user/userdetail?userId=${userId}`, {
                credentials: 'include'
            });
            const updated = await updatedRes.json();
            setReviews(updated.reviews);
            setUser(updated.user);
            setContent("");
            setRating(0);
        } else {
            const errorData = await res.json();
            alert(errorData.message || "리뷰 작성에 실패했습니다.");
        }
    };


    const handleStarClick = (e) => {
        const width = e.currentTarget.offsetWidth;
        const offsetX = e.nativeEvent.offsetX;
        const newRating = Math.ceil((offsetX / width) * 5);
        setRating(newRating);
    };

    if (!user) return <div>로딩 중...</div>;

    return (
        <main>
            <section className="page-header">
                <div className="container">
                    <h1>사용자 프로필</h1>
                    <p>다른 사용자의 정보와 활동을 확인하세요</p>
                </div>
            </section>

            <section className="mypage-section">
                <div className="container">
                    <div className="mypage-container">
                        <div className="mypage-sidebar">
                            <div className="user-profile">
                                <div className="user-avatar">
                                    <img
                                        src={
                                            user.imagePath
                                                ? `http://localhost:8080${user.imagePath}?v=${Date.now()}`
                                                : "http://localhost:8080/placeholder.png"
                                        }
                                        alt="프로필"
                                        style={{ width: 100, height: 100 }}
                                    />
                                </div>
                                <h3 className="user-name">{user.nickname || user.name}</h3>
                                <p className="user-email">{user.email}</p>
                                <div className="rating-display">
                                    <div className="stars">
                                        <div className="empty-stars">★★★★★</div>
                                        <div
                                            className="filled-stars"
                                            style={{ width: `${(user.rating / 5) * 100}%` }}
                                        >
                                            ★★★★★
                                        </div>
                                    </div>
                                    <span className="rating-value">{user.rating.toFixed(2)}</span>
                                    <span className="rating-count">({user.reviewCount}개 평가)</span>
                                </div>
                            </div>
                            <div className="user-meta">
                                <div className="meta-item">
                                    <FontAwesomeIcon icon={faLocationDot} />
                                    <span>{user.location || '주소 정보 없음'}</span>
                                </div>
                            </div>
                        </div>

                        <div className="mypage-content">
                            <div className="content-section">
                                <div className="section-header">
                                    <h2>기본 정보</h2>
                                </div>
                                <div className="user-about">
                                    <h3>자기소개</h3>
                                    <p>{user.description}</p>
                                </div>
                                <div className="interest-tags">
                                    <h3>관심사</h3>
                                    <div className="tags-container">
                                        {user.interest.map(tag => (
                                            <span key={tag.id} className="interest-tag">{tag.name} </span>
                                        ))}
                                    </div>
                                </div>
                            </div>

                            <div className="content-section">
                                <div className="section-header">
                                    <h2>주최한 모임</h2>
                                </div>
                                <div className="meetings-grid">
                                    {hostedMeetings.map(crew => (
                                        <div key={crew.id} className="meeting-card">
                                            <div className="meeting-content">
                                                <span className="meeting-category">{crew.categoryName || '카테고리 없음'}</span><br />
                                                <h3 className="meeting-title">{crew.name}</h3><br />
                                                <div className="meeting-meta">
                                                    <span className="meeting-date">
                                                        <FontAwesomeIcon icon={faCalendar} />
                                                        <span>{crew.sdate}{crew.edate ? ` ~ ${crew.edate}` : ''}</span>
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

                            <div className="content-section">
                                <div className="section-header">
                                    <h2>받은 평가</h2>
                                </div>
                                <form onSubmit={handleReviewSubmit} className="review-form">
                                    <div className="stars" onClick={handleStarClick} style={{ cursor: 'pointer' }}>
                                        <div className="empty-stars">★★★★★</div>
                                        <div
                                            className="filled-stars"
                                            style={{ width: `${(rating / 5) * 100}%` }}
                                        >
                                            ★★★★★
                                        </div>
                                    </div>
                                    <textarea
                                        id="content"
                                        name="content"
                                        rows="4"
                                        value={content}
                                        onChange={e => setContent(e.target.value)}
                                        required
                                    ></textarea>
                                    <button type="submit" className="btn btn-primary" style={{ float: 'right' }}>등록</button>
                                </form>

                                <div className="reviews-list">
                                    {reviews.map(review => (
                                        <div key={review.id} className="review-item">
                                            <div className="review-header">
                                                <div className="reviewer-info">
                                                    <div className="reviewer-details">
                                                        <h4 className="reviewer-name">{review.writerName}</h4>
                                                        <p className="review-date">{review.date}</p>
                                                    </div>
                                                </div>
                                                <div className="review-rating">
                                                    <div className="stars">
                                                        <div className="empty-stars">★★★★★</div>
                                                        <div
                                                            className="filled-stars"
                                                            style={{ width: `${(review.rating / 5) * 100}%` }}
                                                        >
                                                            ★★★★★
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                            <div className="review-content">
                                                <p>{review.contents}</p>
                                            </div>
                                        </div>
                                    ))}
                                    {reviews.length === 0 && <p>아직 작성된 평가가 없습니다.</p>}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </main>
    );
};

export default UserDetail;
