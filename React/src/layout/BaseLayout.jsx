import React, { useState, useEffect, useRef } from 'react';
import { Link, Outlet } from 'react-router-dom';
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faBars, faSun, faMoon, faBell } from "@fortawesome/free-solid-svg-icons";
import '../Style.css';

const BaseLayout = () => {
  const [isDarkTheme, setIsDarkTheme] = useState(false);
  const [isDropdownActive, setIsDropdownActive] = useState(false);
  const [isMobileMenuVisible, setIsMobileMenuVisible] = useState(false);
  const [notifOpen, setNotifOpen] = useState(false);

  const notificationRef = useRef(null);

  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userId, setUserId] = useState(null);

  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    if (isLoggedIn) {
      fetch("/api/notifications", {
        credentials: "include"
      })
        .then(res => res.json())
        .then(data => {
          setNotifications(data);
          const unread = data.filter(n => !n.isRead).length;
          setUnreadCount(unread);
        })
        .catch(err => console.error("알림 가져오기 실패", err));
    }
  }, [isLoggedIn]);

  useEffect(() => {
    fetch("/api/user/status", {
      credentials: "include",
    })
      .then(res => res.json())
      .then(data => {
        console.log(data);
        if (data.isLoggedIn) {
          setIsLoggedIn(true);
          setUserId(data.userId);
        } else {
          setIsLoggedIn(false);
        }
      });
  }, []);

  useEffect(() => {
    const savedTheme = localStorage.getItem("theme");
    const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
    if (savedTheme === "dark" || (!savedTheme && prefersDark)) {
      document.documentElement.classList.add("dark-theme");
      setIsDarkTheme(true);
    }
  }, []);

  const toggleTheme = () => {
    const newTheme = !isDarkTheme;
    setIsDarkTheme(newTheme);
    document.documentElement.classList.toggle("dark-theme", newTheme);
    localStorage.setItem("theme", newTheme ? "dark" : "light");
  };

  const toggleDropdown = (e) => {
    e.stopPropagation();
    setIsDropdownActive(!isDropdownActive);
  };

  const toggleMobileMenu = () => {
    setIsMobileMenuVisible(!isMobileMenuVisible);
  };

  const toggleNotif = (e) => {
    e.stopPropagation();
    setNotifOpen(!notifOpen);
  };

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (isDropdownActive && !e.target.closest('.dropdown')) {
        setIsDropdownActive(false);
      }
      if (notifOpen && notificationRef.current && !notificationRef.current.contains(e.target)) {
        setNotifOpen(false);
      }
    };

    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, [isDropdownActive, notifOpen]);

  
  return (
    <>
      <header className="header">
        <div className="container">
          <div className="header-content">
            <Link to="/" className="logo">PartyLink</Link>
            <nav className="main-nav" style={{ display: isMobileMenuVisible ? 'flex' : '' }}>
              <ul>
                <li><Link to="/crew">모임찾기</Link></li>
                <li><Link to="/crew/mycrew">내 모임</Link></li>
                <li><Link to="/appli">신청상태</Link></li>
              </ul>
            </nav>
            <div className="header-actions">
              {/* 🌙 테마 토글 */}
              <button className="theme-toggle" onClick={toggleTheme}>
                <FontAwesomeIcon icon={isDarkTheme ? faSun : faMoon} />
              </button>

              {/* 🔔 알림 드롭다운 */}
              {isLoggedIn && (
                <div className={`dropdown ${notifOpen ? 'active' : ''}`} ref={notificationRef}>
                  <button className="dropdown-toggle" onClick={toggleNotif}>
                    <FontAwesomeIcon icon={faBell} />
                    {unreadCount > 0 && <span className="badge">{unreadCount}</span>}
                  </button>
                  {notifOpen && (
                    <div className="dropdown-menu notification-menu">
                      {notifications.length > 0 ? (
                        notifications.map(n => (
                          <Link
                            key={n.id}
                            to={`/crew/detail?id=${n.crewId}`}
                            className="dropdown-item"
                          >
                            {n.message}
                          </Link>
                        ))
                      ) : (
                        <span className="dropdown-item">알림이 없습니다</span>
                      )}
                    </div>
                  )}

                </div>
              )}

              {/* 🍔 메뉴 드롭다운 */}
              <div className={`dropdown ${isDropdownActive ? 'active' : ''}`}>
                <button className="dropdown-toggle" onClick={toggleDropdown}>
                  <FontAwesomeIcon icon={faBars} />
                </button>
                <div className="dropdown-menu">
                  {/* 로그인 상태일 때 */}
                  {isLoggedIn ? (
                    <>
                      <a href="http://localhost:8080/api/user/logout" className="dropdown-item">로그아웃</a>
                    </>
                  ) : (
                    <Link to="/user/login" className="dropdown-item">로그인</Link>
                  )}
                  <Link to="/user/mypage" className="dropdown-item">개인정보 수정</Link>
                  <Link to={`/user/userdetail?userId=${userId}`} className="dropdown-item">마이페이지</Link>

                </div>
              </div>

            </div>
          </div>
        </div>
      </header>

      <main>
        <Outlet />
      </main>

      <footer className="footer">
        <div className="container">
          <div className="footer-content">
            <div className="footer-logo">PartyLink</div>
            <div className="footer-info">
              <p>©2019301081 한규원</p>
              <p>서경대학교 소프트웨어학과</p>
            </div>
            <div></div>
          </div>
        </div>
      </footer>
    </>
  );
};

export default BaseLayout;
