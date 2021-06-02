package com.example.demo.dataImpl;

import com.example.demo.VO.*;
import com.example.demo.data.MyMapper;
import com.example.demo.tool.Tool;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Repository
public class MyMapperImpl implements MyMapper {
    @Autowired
    private Jdbc jdbc;

    @Autowired
    private RestTemplate restTemplate;


    @Override
    public ResponseVO validate(LoginVO loginVO) throws SQLException {
        Connection connection = jdbc.getConnection();
        Statement statement = connection.createStatement();
        String sql = "select * from account where acc=\"" + loginVO.getAccount() + "\" and passwd=\"" + loginVO.getPassword() + "\"";
        ResultSet resultSet = statement.executeQuery(sql);
        if (resultSet.next()) {
            return ResponseVO.buildSuccess(2);
        } else {
            sql = "select * from account where acc=\"" + loginVO.getAccount() + "\"";
            resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                return ResponseVO.buildFailure("账户密码错误");
            } else {
                sql = "select * from student where Sno=\"" + loginVO.getAccount() + "\" and Pwd=\"" + loginVO.getPassword() + "\" and Sde=\"地海\"";
                resultSet = statement.executeQuery(sql);
                if (resultSet.next()) {
                    return ResponseVO.buildSuccess(1);
                } else {
                    sql = "select * from student where Sno=\"" + loginVO.getAccount() + "\" and Sde=\"地海\"";
                    resultSet = statement.executeQuery(sql);
                    if (resultSet.next()) {
                        return ResponseVO.buildFailure("账户密码错误");
                    } else {
                        return ResponseVO.buildFailure("账号不存在");
                    }
                }
            }
        }
    }

    @Override
    public ResponseVO getAdmInfo(String account) throws SQLException {
        Connection connection = jdbc.getConnection();
        Statement statement = connection.createStatement();
        String sql = "select * from account where acc=\"" + account + "\"";
        ResultSet resultSet = statement.executeQuery(sql);
        if (resultSet.next()) {
            AdminVO adminVO = new AdminVO();
            adminVO.setAccount(account);
            adminVO.setPassword(resultSet.getString("passwd"));
            return ResponseVO.buildSuccess(adminVO);
        } else {
            return ResponseVO.buildFailure("出现错误");
        }
    }

    @Override
    public ResponseVO getStuInfoForAdm() throws SQLException {
        Connection connection = jdbc.getConnection();
        Statement statement = connection.createStatement();
        String sql = "select * from student where Sde=\"地海\"";
        ResultSet resultSet = statement.executeQuery(sql);
        ArrayList<StudentVO> studentVOS = new ArrayList<>();
        while (resultSet.next()) {
            StudentVO studentVO = new StudentVO();
            studentVO.setName(resultSet.getString("Snm"));
            studentVO.setDepartment(resultSet.getString("Sde"));
            studentVO.setSex(resultSet.getString("Sex"));
            studentVO.setId(resultSet.getString("Sno"));
            studentVOS.add(studentVO);
        }
        return ResponseVO.buildSuccess(studentVOS);
    }

    @Override
    public ResponseVO getLessonInfoForAdm() throws SQLException {
        Connection connection = jdbc.getConnection();
        Statement statement = connection.createStatement();
        String sql = "select * from course";
        ResultSet resultSet = statement.executeQuery(sql);
        ArrayList<CourseVO> courseVOS = new ArrayList<>();
        while (resultSet.next()) {
            CourseVO courseVO = new CourseVO();
            String cno = resultSet.getString("Cno");
            courseVO.setLesson_name(resultSet.getString("Cnm"));
            courseVO.setLesson_id(cno);
            courseVO.setLesson_point(resultSet.getInt("Cpt"));
            courseVO.setClassroom(resultSet.getString("Pla"));
            courseVO.setLesson_time(resultSet.getInt("Ctm"));
            courseVO.setTeacher(resultSet.getString("Tec"));
            courseVO.setIsShared(resultSet.getString("Share"));
            int chooseCount = 0;
            sql = "select count(*) as count from choice where Cno=\"" + cno + "\"";
            Statement statement1 = connection.createStatement();
            ResultSet resultSet1 = statement1.executeQuery(sql);
            if (resultSet1.next()) {
                chooseCount += resultSet1.getInt("count");
            }
            courseVO.setChooseCount(chooseCount);
            courseVOS.add(courseVO);
        }
        return ResponseVO.buildSuccess(courseVOS);
    }

    @Override
    public ResponseVO changeAdmPassword(AdminVO adminVO) throws SQLException {
        if (adminVO.getPassword().length() > 12) {
            return ResponseVO.buildFailure("新密码长度不能超过12");
        }
        if (adminVO.getPassword().length() == 0) {
            return ResponseVO.buildFailure("新密码不能为空");
        }
        Connection connection = jdbc.getConnection();
        Statement statement = connection.createStatement();
        String sql = "select * from account where acc=\"" + adminVO.getAccount() + "\" and passwd=\"" + adminVO.getPassword() + "\"";
        ResultSet resultSet = statement.executeQuery(sql);
        if (resultSet.next()) {
            return ResponseVO.buildFailure("密码不能与现密码一致");
        } else {
            sql = "update account set passwd=\"" + adminVO.getPassword() + "\" where acc=\"" + adminVO.getAccount() + "\"";
            statement.executeUpdate(sql);
            return ResponseVO.buildSuccess();
        }
    }

    @Override
    public ResponseVO changeAdmLesson(CourseVO courseVO) throws SQLException {
        Connection connection = jdbc.getConnection();
        Statement statement = connection.createStatement();
        String sql = "select * from course where Cno=\"" + courseVO.getLesson_id() + "\"";
        ResultSet resultSet = statement.executeQuery(sql);
        if (resultSet.next()) {
            sql = "update course set Cnm=\"" + courseVO.getLesson_name() + "\", Ctm=" + courseVO.getLesson_time() + ", Cpt=" + courseVO.getLesson_point() + ", Tec=\"" + courseVO.getTeacher() + "\", Pla=\"" + courseVO.getClassroom() + "\", Share=\"" + courseVO.getIsShared() + "\" where Cno=\"" + courseVO.getLesson_id() + "\"";
            statement.executeUpdate(sql);
            return ResponseVO.buildSuccess();
        } else {
            try {
                sql = "insert into course set Cnm=\"" + courseVO.getLesson_name() + "\", Ctm=" + courseVO.getLesson_time() + ", Cpt=" + courseVO.getLesson_point() + ", Tec=\"" + courseVO.getTeacher() + "\", Pla=\"" + courseVO.getClassroom() + "\", Share=\"" + courseVO.getIsShared() + "\", Cno=\"" + courseVO.getLesson_id() + "\"";
                statement.execute(sql);
                return ResponseVO.buildSuccess();
            } catch (Exception e) {
                return ResponseVO.buildFailure(e.getMessage());
            }
        }
    }

    @Override
    public ResponseVO deleteLesson(String courseId) throws SQLException {
        Connection connection= jdbc.getConnection();
        Statement statement=connection.createStatement();
        String sql="delete from course where Cno='"+courseId+"'";
        statement.execute(sql);
        return ResponseVO.buildSuccess();
    }

    @Override
    public ResponseVO getStuInfo(String account) throws SQLException {
        Connection connection = jdbc.getConnection();
        Statement statement = connection.createStatement();
        String sql = "select * from student where Sno=\"" + account + "\"";
        ResultSet resultSet = statement.executeQuery(sql);
        StudentVO studentVO = new StudentVO();
        if (resultSet.next()) {
            studentVO.setId(account);
            studentVO.setAccount(account);
            studentVO.setName(resultSet.getString("Snm"));
            studentVO.setDepartment(resultSet.getString("Sde"));
            studentVO.setSex(resultSet.getString("Sex"));
            studentVO.setPassword(resultSet.getString("Pwd"));
        }
        return ResponseVO.buildSuccess(studentVO);
    }

    @Override
    public ResponseVO getLessonInfo() throws SQLException, DocumentException {
        Connection connection = jdbc.getConnection();
        Statement statement = connection.createStatement();
        String sql = "select * from course";
        ResultSet resultSet = statement.executeQuery(sql);
        ArrayList<CourseVO> courseVOS = new ArrayList<>();
        while (resultSet.next()) {
            CourseVO courseVO = new CourseVO();
            courseVO.setTeacher(resultSet.getString("Tec"));
            courseVO.setLesson_id(resultSet.getString("Cno"));
            courseVO.setLesson_time(resultSet.getInt("Ctm"));
            courseVO.setLesson_name(resultSet.getString("Cnm"));
            courseVO.setLesson_point(resultSet.getInt("Cpt"));
            courseVO.setIsShared(resultSet.getString("Share"));
            courseVO.setClassroom(resultSet.getString("Pla"));
            courseVOS.add(courseVO);
        }
        //TODO 服务端传来其他课程的数据
        String url="http://localhost:8093/integrate/api/getShareLessonFromAandB";
        ResponseEntity<ResponseVO> responseEntity=restTemplate.getForEntity(url,ResponseVO.class);
        ResponseVO responseVO=responseEntity.getBody();
        String xml=(String) responseVO.getContent();
        Tool tool=new Tool();
        tool.stringToFile(xml,"doc/1.xml");
        SAXReader reader=new SAXReader();
        Document document=reader.read(new File("doc/1.xml"));
        Element classes=document.getRootElement();
        Iterator iterator=classes.elementIterator();
        while (iterator.hasNext()){
            CourseVO courseVO=new CourseVO();
            Element class1=(Element) iterator.next();
            Iterator iterator1=class1.elementIterator();
            while (iterator1.hasNext()){
                Element element=(Element) iterator1.next();
                switch (element.getName()){
                    case "Cno":
                        courseVO.setLesson_id(element.getStringValue());
                        break;
                    case "Cnm":
                        courseVO.setLesson_name(element.getStringValue());
                        break;
                    case "Ctm":
                        courseVO.setLesson_time(Integer.valueOf(element.getStringValue()));
                        break;
                    case "Cpt":
                        courseVO.setLesson_point(Integer.valueOf(element.getStringValue()));
                        break;
                    case "Tec":
                        courseVO.setTeacher(element.getStringValue());
                        break;
                    case "Pla":
                        courseVO.setClassroom(element.getStringValue());
                        break;
                    case "Share":
                        courseVO.setIsShared(element.getStringValue());
                }
            }
            courseVOS.add(courseVO);
        }
        return ResponseVO.buildSuccess(courseVOS);
    }

    @Override
    public ResponseVO changePassword(LoginVO loginVO) throws SQLException {
        if (loginVO.getPassword().length() > 12) {
            return ResponseVO.buildFailure("新密码长度不能超过12");
        }
        if (loginVO.getPassword().length() == 0) {
            return ResponseVO.buildFailure("新密码不能为空");
        }
        Connection connection = jdbc.getConnection();
        Statement statement = connection.createStatement();
        String sql = "select * from student where Sno=\"" + loginVO.getAccount() + "\" and Pwd=\"" + loginVO.getPassword() + "\"";
        ResultSet resultSet = statement.executeQuery(sql);
        if (resultSet.next()) {
            return ResponseVO.buildFailure("密码不能与现密码一致");
        } else {
            sql = "update student set Pwd=\"" + loginVO.getPassword() + "\" where Sno=\"" + loginVO.getAccount() + "\"";
            statement.executeUpdate(sql);
            return ResponseVO.buildSuccess();
        }
    }

    @Override
    public ResponseVO chooseLesson(ChooseVO chooseVO) throws SQLException {
        Connection connection = jdbc.getConnection();
        Statement statement = connection.createStatement();
        String sql = "select * from course where Cno=\"" + chooseVO.getCourseId() + "\"";
        ResultSet resultSet = statement.executeQuery(sql);
        if (resultSet.next()) {
            sql = "insert into choice set Cno=\"" + chooseVO.getCourseId() + "\",Sno=\"" + chooseVO.getStudentId() + "\"";
            Statement statement1 = connection.createStatement();
            statement1.execute(sql);
            return ResponseVO.buildSuccess();
        } else {
            //TODO 交给集成服务端处理其他院系的选课
            Document document=DocumentHelper.createDocument();
            document.setXMLEncoding("UTF-8");
            Element choices=document.addElement("choices");
            Element choice=choices.addElement("choice");
            choice.addElement("Sno").addText(chooseVO.getStudentId());
            choice.addElement("Cno").addText(chooseVO.getCourseId());
            choice.addElement("Grd").addText(String.valueOf(chooseVO.getGrade()));
            String xml=document.asXML();
            String url="http://localhost:8093/integrate/api/chooseLessonAandB";
            restTemplate.postForEntity(url,xml,ResponseVO.class);
            return ResponseVO.buildSuccess();
        }
    }

    @Override
    public ResponseVO getChoosedLesson(String stuId) throws SQLException, DocumentException {
        Connection connection = jdbc.getConnection();
        Statement statement = connection.createStatement();
        String sql = "select * from choice where Sno=\"" + stuId + "\"";
        ResultSet resultSet = statement.executeQuery(sql);
        ArrayList<CourseVO> courseVOS = new ArrayList<>();
        while (resultSet.next()) {
            Statement statement1 = connection.createStatement();
            sql = "select * from course where Cno=\"" + resultSet.getString("Cno") + "\"";
            ResultSet resultSet1 = statement1.executeQuery(sql);
            if (resultSet1.next()) {
                CourseVO courseVO = new CourseVO();
                courseVO.setTeacher(resultSet1.getString("Tec"));
                courseVO.setLesson_id(resultSet1.getString("Cno"));
                courseVO.setLesson_time(resultSet1.getInt("Ctm"));
                courseVO.setLesson_name(resultSet1.getString("Cnm"));
                courseVO.setLesson_point(resultSet1.getInt("Cpt"));
                courseVO.setIsShared(resultSet1.getString("Share"));
                courseVO.setClassroom(resultSet1.getString("Pla"));
                courseVOS.add(courseVO);
            }
        }
        //TODO 从其他院系得到该同学的选课记录
        String url="http://localhost:8093/integrate/api/getChoosedLessonAandB/"+stuId;
        String xml=(String) restTemplate.getForEntity(url,ResponseVO.class).getBody().getContent();
        Tool tool=new Tool();
        tool.stringToFile(xml,"doc/1.xml");
        SAXReader reader=new SAXReader();
        Document document=reader.read(new File("doc/1.xml"));
        Element classes=document.getRootElement();
        Iterator iterator=classes.elementIterator();
        while (iterator.hasNext()){
            CourseVO courseVO=new CourseVO();
            Element class1=(Element) iterator.next();
            Iterator iterator1=class1.elementIterator();
            while (iterator1.hasNext()) {
                Element element = (Element) iterator1.next();
                switch (element.getName()) {
                    case "Cno":
                        courseVO.setLesson_id(element.getStringValue());
                        break;
                    case "Cnm":
                        courseVO.setLesson_name(element.getStringValue());
                        break;
                    case "Ctm":
                        courseVO.setLesson_time(Integer.valueOf(element.getStringValue()));
                        break;
                    case "Cpt":
                        courseVO.setLesson_point(Integer.valueOf(element.getStringValue()));
                        break;
                    case "Tec":
                        courseVO.setTeacher(element.getStringValue());
                        break;
                    case "Pla":
                        courseVO.setClassroom(element.getStringValue());
                        break;
                    case "Share":
                        courseVO.setIsShared(element.getStringValue());
                }
            }
            courseVOS.add(courseVO);
        }
        return ResponseVO.buildSuccess(courseVOS);
    }

    @Override
    public ResponseVO dropLesson(ChooseVO chooseVO) throws SQLException, DocumentException {
        Connection connection = jdbc.getConnection();
        Statement statement = connection.createStatement();
        String sql = "delete from choice where Sno=\"" + chooseVO.getStudentId() + "\" and Cno=\"" + chooseVO.getCourseId() + "\"";
        statement.execute(sql);
        sql = "select * from course where Cno=\"" + chooseVO.getCourseId() + "\"";
        ResultSet resultSet = statement.executeQuery(sql);
        CourseVO courseVO = new CourseVO();
        if (resultSet.next()) {
            courseVO.setClassroom(resultSet.getString("Pla"));
            courseVO.setLesson_id(resultSet.getString("Cno"));
            courseVO.setTeacher(resultSet.getString("Tec"));
            courseVO.setLesson_time(resultSet.getInt("Ctm"));
            courseVO.setLesson_point(resultSet.getInt("Cpt"));
            courseVO.setIsShared(resultSet.getString("Share"));
            courseVO.setLesson_name(resultSet.getString("Cnm"));
        }
        else {
            //TODO 有可能数据在其他院系，向集成服务器发送信息
            String xml="";
            Document document=DocumentHelper.createDocument();
            document.setXMLEncoding("UTF-8");
            Element choices=document.addElement("choices");
            Element choice=choices.addElement("choice");
            choice.addElement("Sno").addText(chooseVO.getStudentId());
            choice.addElement("Cno").addText(chooseVO.getCourseId());
            choice.addElement("Grd").addText(String.valueOf(chooseVO.getGrade()));
            xml=document.asXML();
            String url="http://localhost:8093/integrate/api/dropLessonAandB";
            ResponseEntity<ResponseVO> responseEntity=restTemplate.postForEntity(url,xml,ResponseVO.class);
            String result=(String) responseEntity.getBody().getContent();
            Tool tool=new Tool();
            tool.stringToFile(result,"doc/1.xml");
            SAXReader reader=new SAXReader();
            Document document1=reader.read(new File("doc/1.xml"));
            Element classes=document1.getRootElement();
            Iterator iterator=classes.elementIterator();
            while (iterator.hasNext()){
                Element class1=(Element) iterator.next();
                Iterator iterator1=class1.elementIterator();
                while (iterator1.hasNext()) {
                    Element element = (Element) iterator1.next();
                    switch (element.getName()) {
                        case "Cno":
                            courseVO.setLesson_id(element.getStringValue());
                            break;
                        case "Cnm":
                            courseVO.setLesson_name(element.getStringValue());
                            break;
                        case "Ctm":
                            courseVO.setLesson_time(Integer.valueOf(element.getStringValue()));
                            break;
                        case "Cpt":
                            courseVO.setLesson_point(Integer.valueOf(element.getStringValue()));
                            break;
                        case "Tec":
                            courseVO.setTeacher(element.getStringValue());
                            break;
                        case "Pla":
                            courseVO.setClassroom(element.getStringValue());
                            break;
                        case "Share":
                            courseVO.setIsShared(element.getStringValue());
                    }
                }
            }
        }
        return ResponseVO.buildSuccess(courseVO);
    }

    @Override
    public ResponseVO dropLesson_clientC(String xml) throws DocumentException, SQLException {
        Tool tool = new Tool();
        tool.stringToFile(xml, "doc/1.xml");
        SAXReader reader = new SAXReader();
        //2.加载xml
        Document document = reader.read(new File("doc/1.xml"));
        //3.获取根节点
        Element rootElement = document.getRootElement();
        Iterator iterator = rootElement.elementIterator();
        ChooseVO chooseVO = new ChooseVO();
        while (iterator.hasNext()) {
            Element choose = (Element) iterator.next();
            Iterator iterator1 = choose.elementIterator();
            while (iterator1.hasNext()) {
                Element stuChild = (Element) iterator1.next();
                switch (stuChild.getName()) {
                    case "Cno":
                        chooseVO.setCourseId(stuChild.getStringValue());
                        break;
                    case "Sno":
                        chooseVO.setStudentId(stuChild.getStringValue());
                        break;
                }
            }
        }
        Connection connection = jdbc.getConnection();
        Statement statement = connection.createStatement();
        String sql = "delete from choice where Sno=\"" + chooseVO.getStudentId() + "\" and Cno=\"" + chooseVO.getCourseId() + "\"";
        statement.execute(sql);
        sql = "select * from course where Cno=\"" + chooseVO.getCourseId() + "\"";
        ResultSet resultSet = statement.executeQuery(sql);
        String requestXml = "";
        if (resultSet.next()) {
            Document document1 = DocumentHelper.createDocument();
            document1.setXMLEncoding("UTF-8");
            Element classes = document1.addElement("classes");
            Element class1 = classes.addElement("class");
            class1.addElement("Cno").addText(resultSet.getString("Cno"));
            class1.addElement("Cnm").addText(resultSet.getString("Cnm"));
            class1.addElement("Ctm").addText(String.valueOf(resultSet.getInt("Ctm")));
            class1.addElement("Cpt").addText(String.valueOf(resultSet.getInt("Cpt")));
            class1.addElement("Tec").addText(resultSet.getString("Tec"));
            class1.addElement("Pla").addText(resultSet.getString("Pla"));
            class1.addElement("Share").addText(resultSet.getString("Share"));
            requestXml = document1.asXML();
        }
        return ResponseVO.buildSuccess(requestXml);
    }

    @Override
    public ResponseVO getChoosedLesson_clientC(String stuId) throws SQLException {
        Connection connection = jdbc.getConnection();
        Statement statement = connection.createStatement();
        String sql = "select * from choice where Sno=\"" + stuId + "\"";
        ResultSet resultSet = statement.executeQuery(sql);
        ArrayList<CourseVO> courseVOS = new ArrayList<>();
        while (resultSet.next()) {
            CourseVO courseVO = new CourseVO();
            String cno = resultSet.getString("Cno");
            Statement statement1 = connection.createStatement();
            sql = "select * from course where Cno=\"" + cno + "\"";
            ResultSet resultSet1 = statement1.executeQuery(sql);
            if (resultSet1.next()) {
                courseVO.setLesson_name(resultSet1.getString("Cnm"));
                courseVO.setLesson_id(resultSet1.getString("Cno"));
                courseVO.setLesson_point(resultSet1.getInt("Cpt"));
                courseVO.setLesson_time(resultSet1.getInt("Ctm"));
                courseVO.setClassroom(resultSet1.getString("Pla"));
                courseVO.setTeacher(resultSet1.getString("Tec"));
                courseVO.setIsShared(resultSet1.getString("Share"));
                courseVOS.add(courseVO);
            }
        }
        String requestXml = "";
        Document document1 = DocumentHelper.createDocument();
        document1.setXMLEncoding("UTF-8");
        Element classes = document1.addElement("classes");
        for (int i = 0; i < courseVOS.size(); i++) {
            Element class1 = classes.addElement("class");
            CourseVO courseVO = courseVOS.get(i);
            class1.addElement("Cno").addText(courseVO.getLesson_id());
            class1.addElement("Cnm").addText(courseVO.getLesson_name());
            class1.addElement("Ctm").addText(String.valueOf(courseVO.getLesson_time()));
            class1.addElement("Cpt").addText(String.valueOf(courseVO.getLesson_point()));
            class1.addElement("Tec").addText(courseVO.getTeacher());
            class1.addElement("Pla").addText(courseVO.getClassroom());
            class1.addElement("Share").addText(courseVO.getIsShared());
        }
        requestXml = document1.asXML();
        return ResponseVO.buildSuccess(requestXml);
    }

    @Override
    public ResponseVO chooseLesson_clientC(String xml) throws DocumentException, SQLException {
        Tool tool = new Tool();
        tool.stringToFile(xml, "doc/1.xml");
        SAXReader reader = new SAXReader();
        //2.加载xml
        Document document = reader.read(new File("doc/1.xml"));
        //3.获取根节点
        Element rootElement = document.getRootElement();
        Iterator iterator = rootElement.elementIterator();
        ChooseVO chooseVO = new ChooseVO();
        while (iterator.hasNext()) {
            Element choose = (Element) iterator.next();
            Iterator iterator1 = choose.elementIterator();
            while (iterator1.hasNext()) {
                Element stuChild = (Element) iterator1.next();
                switch (stuChild.getName()) {
                    case "Cno":
                        chooseVO.setCourseId(stuChild.getStringValue());
                        break;
                    case "Sno":
                        chooseVO.setStudentId(stuChild.getStringValue());
                        break;
                }
            }
        }
        Connection connection = jdbc.getConnection();
        Statement statement = connection.createStatement();
        String sql = "select * from course where Cno=\"" + chooseVO.getCourseId() + "\"";
        ResultSet resultSet = statement.executeQuery(sql);
        if (resultSet.next()) {
            sql = "select * from student where Sno=\"" + chooseVO.getStudentId() + "\"";
            ResultSet resultSet1=statement.executeQuery(sql);
            if(!resultSet1.next()){
                sql = "insert into student set Sno=\"" + chooseVO.getStudentId() + "\"";
                statement.execute(sql);
            }
            sql = "insert into choice set Cno=\"" + chooseVO.getCourseId() + "\", Sno=\"" + chooseVO.getStudentId() + "\"";
            statement.execute(sql);
        }
        return ResponseVO.buildSuccess();
    }

    @Override
    public ResponseVO getShareLesson_clientC() throws SQLException {
        Connection connection = jdbc.getConnection();
        Statement statement = connection.createStatement();
        String sql = "select * from course where Share=\"1\"";
        ResultSet resultSet = statement.executeQuery(sql);
        String string = "";
        Document document = DocumentHelper.createDocument();
        document.setXMLEncoding("UTF-8");
        Element classes = document.addElement("classes");
        while (resultSet.next()) {
            Element class1 = classes.addElement("class");
            class1.addElement("Cno").addText(resultSet.getString("Cno"));
            class1.addElement("Cnm").addText(resultSet.getString("Cnm"));
            class1.addElement("Ctm").addText(resultSet.getString("Ctm"));
            class1.addElement("Cpt").addText(resultSet.getString("Cpt"));
            class1.addElement("Tec").addText(resultSet.getString("Tec"));
            class1.addElement("Pla").addText(resultSet.getString("Pla"));
            class1.addElement("Share").addText(resultSet.getString("Share"));
        }
        string = document.asXML();
        return ResponseVO.buildSuccess(string);
    }
}
