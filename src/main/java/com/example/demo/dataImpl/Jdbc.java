package com.example.demo.dataImpl;

import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Service
public class Jdbc {
    private Connection connection;

    public Jdbc() {
        try {
            // 注册 JDBC 驱动
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 打开链接
            System.out.println("连接数据库...");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/elt", "root", "root");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            System.out.println(throwables.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
