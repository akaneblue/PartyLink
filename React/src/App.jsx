import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import './Style.css';
import Home from "./page/Home";
import BaseLayout from "./layout/BaseLayout";
import Login from "./page/Login";
import Signup from "./page/Signup";
import Mypage from "./page/Mypage";
import UserDetail from "./page/UserDetail";
import CrewList from "./page/CrewList";
import CrewDetail from "./page/CrewDetail";
import CrewCreate from "./page/CrewCreate";
import MyCrew from "./page/MyCrew";
import CrewPage from "./page/CrewPage";
import NoticeForm from "./page/NoticeForm";
import ScheduleForm from "./page/ScheduleForm";
import PostCreate from "./page/PostCreate";
import PostDetail from "./page/PostDetail";
import AppList from "./page/AppList";
import ApplicationForm from "./page/ApplicationForm";
import Recommend from "./page/Recommend";

function App() {
  return (
    <Router>
      <Routes>
        <Route element={<BaseLayout />}>
          <Route path="/" element={<Home />} />
          <Route path="/user/login" element={<Login />} />
          <Route path="/user/signup" element={<Signup />} />
          <Route path="/user/mypage" element={<Mypage />} />
          <Route path="user/userdetail" element={<UserDetail />} />
          <Route path="/crew" element={<CrewList />} />
          <Route path="/crew/detail" element={<CrewDetail />} />
          <Route path="/crew/create" element={<CrewCreate />} />
          <Route path="/crew/mycrew" element={<MyCrew/>} />
          <Route path="/crew/crewpage" element={<CrewPage/>} />
          <Route path="/crew/crewpage/notice" element={<NoticeForm/>} />
          <Route path="/crew/crewpage/schedule" element={<ScheduleForm/>} />
          <Route path="/crew/crewpage/posts" element={<PostCreate/>} />
          <Route path="/crew/crewpage/post" element={<PostDetail/>} />
          <Route path="/appli" element={<AppList/>} />
          <Route path="/appli/create" element={<ApplicationForm/>} />
          <Route path="crew/recommend" element={<Recommend />} />
        </Route>
      </Routes>
    </Router>
  );
}

export default App;
