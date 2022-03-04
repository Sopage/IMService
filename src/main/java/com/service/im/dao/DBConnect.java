package com.service.im.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DBConnect {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBConnect.class);
    private DataSource dataSource;

    public DBConnect() {
        try {
            dataSource = new HikariDataSource(new HikariConfig());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("数据库链接异常!!!", e);
        }
    }

    public void login(long uid, String token) {
        try {
            Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM `user` WHERE `id`=? AND `token`=?");
            ps.setObject(1, uid);
            ps.setObject(2, token);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                uid = rs.getLong("id");
                token = rs.getString("token");
                LOGGER.info("uid={} token={}", uid, token);
            }
            rs.close();
            ps.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
