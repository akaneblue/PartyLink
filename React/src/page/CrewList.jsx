import axios from "axios";
import React, {useEffect, useState} from "react"
import { useNavigate, Link, useSearchParams } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCalendar, faLocationDot, faSearch, faUser } from "@fortawesome/free-solid-svg-icons";

const CrewList=() => {
    const [searchParams, setSearchParams] = useSearchParams();
    const page = parseInt(searchParams.get("page")) || 0;
    const keyword = searchParams.get("keyword") || "";
    const category = searchParams.get("category") || "";

    const [crewList, setCrewsList] = useState([]);
    const [user, setUser] = useState(null);

    const navigate = useNavigate();

    useEffect(() => {
        let url = `/api/crew?page=${page}`;
        if(keyword) url += `&keyword=${keyword}`;
        else if (category) url+= `&category=${category}`;

        axios.get(url).then(res => {
            setCrewsList(res.data.crewList || []);
            setUser(res.data.user);
        }).catch(err => console.error(err));
    },[page, keyword, category]);

    const handleSearch = (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        const kw = formData.get("keyword");
        setSearchParams({ keyword: kw});
    };

    return(
        <main>
      <section className="page-header">
        <div className="container">
          <h1>모임 찾기</h1>
          <div className="search-filter">
            <div className="search-box">
              <form onSubmit={handleSearch} style={{ display: "flex", flex: 1 }}>
                <input type="text" name="keyword" defaultValue={keyword} placeholder="모임 검색" />
                <button type="submit">
                  <FontAwesomeIcon icon={faSearch} />
                </button>
              </form>
            </div>
            <div className="filter-options">
              <Link className={`filter-btn ${!category ? 'active' : ''}`} to="/crew">전체</Link>
              {user?.interest?.map((int) => (
                <Link key={int.id}
                      className={`filter-btn ${category === int.name ? 'active' : ''}`}
                      to={`/crew?category=${int.name}`}>
                  {int.name}
                </Link>
              ))}
            </div>
          </div>
        </div>
      </section>

      <section className="meetings-list">
        <div className="container">
          <div className="meetings-grid">
            {crewList.map((crew) => (
              <div className="meeting-card" key={crew.id} onClick={() => navigate(`/crew/detail?id=${crew.id}`)}>
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

          {/* 페이지네이션 */}
          <div className="pagination">
            {page > 0 && (
              <Link className="page-link prev"
                    to={`/meetings?page=${page - 1}${category ? `&category=${category}` : ''}`}>
                <i className="fa-solid fa-chevron-left"></i>
              </Link>
            )}
            <span className="page-link active">{page + 1}</span>
            {crewList.length === 15 && (
              <Link className="page-link next"
                    to={`/meetings?page=${page + 1}${category ? `&category=${category}` : ''}`}>
                <i className="fa-solid fa-chevron-right"></i>
              </Link>
            )}
          </div>
        </div>
      </section>
    </main>
    );
};

export default CrewList;