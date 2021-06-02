package com.example.demo.serviceImpl;

import com.example.demo.VO.*;
import com.example.demo.data.MyMapper;
import com.example.demo.service.MyService;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.sql.SQLException;

@Service
public class MyServiceImpl implements MyService {
    @Autowired
    private MyMapper myMapper;


    @Override
    public ResponseVO validate(LoginVO loginVO) throws SQLException {
        return myMapper.validate(loginVO);
    }

    @Override
    public ResponseVO getAdmInfo(String account) throws SQLException {
        return myMapper.getAdmInfo(account);
    }

    @Override
    public ResponseVO getStuInfoForAdm() throws SQLException {
        return myMapper.getStuInfoForAdm();
    }

    @Override
    public ResponseVO getLessonInfoForAdm() throws SQLException {
        return myMapper.getLessonInfoForAdm();
    }

    @Override
    public ResponseVO changeAdmPassword(AdminVO adminVO) throws SQLException {
        return myMapper.changeAdmPassword(adminVO);
    }

    @Override
    public ResponseVO changeAdmLesson(CourseVO courseVO) throws SQLException {
        return myMapper.changeAdmLesson(courseVO);
    }

    @Override
    public ResponseVO deleteLesson(String courseId) throws SQLException {
        return myMapper.deleteLesson(courseId);
    }

    @Override
    public ResponseVO getStuInfo(String account) throws SQLException {
        return myMapper.getStuInfo(account);
    }

    @Override
    public ResponseVO getLessonInfo() throws SQLException, DocumentException {
        return myMapper.getLessonInfo();
    }

    @Override
    public ResponseVO changePassword(LoginVO loginVO) throws SQLException {
        return myMapper.changePassword(loginVO);
    }

    @Override
    public ResponseVO chooseLesson(ChooseVO chooseVO) throws SQLException {
        return myMapper.chooseLesson(chooseVO);
    }

    @Override
    public ResponseVO getChoosedLesson(String stuId) throws SQLException, DocumentException {
        return myMapper.getChoosedLesson(stuId);
    }

    @Override
    public ResponseVO dropLesson(ChooseVO chooseVO) throws SQLException, DocumentException {
        return myMapper.dropLesson(chooseVO);
    }

    @Override
    public ResponseVO dropLesson_clientC(String xml) throws DocumentException, SQLException {
        return myMapper.dropLesson_clientC(xml);
    }

    @Override
    public ResponseVO getChoosedLesson_clientC(String stuId) throws SQLException {
        return myMapper.getChoosedLesson_clientC(stuId);
    }

    @Override
    public ResponseVO chooseLesson_clientC(String xml) throws SQLException, DocumentException {
        return myMapper.chooseLesson_clientC(xml);
    }

    @Override
    public ResponseVO getShareLesson_clientC() throws SQLException {
        return myMapper.getShareLesson_clientC();
    }
}
