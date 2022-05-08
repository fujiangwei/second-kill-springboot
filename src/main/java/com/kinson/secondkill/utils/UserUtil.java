package com.kinson.secondkill.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kinson.secondkill.domain.RespBean;
import com.kinson.secondkill.domain.UserEntity;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @description: 用户工具
 * @author:
 * @date:
 **/
@Slf4j(topic = "UserUtil")
public class UserUtil {

    public static void createUser(Long start, int count) throws Exception {
        List<UserEntity> users = new ArrayList<>(count);
        // 生成用户
        for (int i = 0; i < count; i++) {
            UserEntity user = new UserEntity();
            user.setId(13000000000L + i);
            user.setLoginCount(1);
            user.setNickname("user" + i);
            user.setRegisterDate(new Date());
            user.setSalt("1a2b3c4d");
            user.setPassword(MD5Util.inputPassToDBPass("111111", user.getSalt()));
            users.add(user);
        }
        log.info("create user");
        // 插入数据库
        Connection conn = getConn();
        String sql = "insert into t_user(login_count, nickname, register_date, salt, password, id) values(?,?,?,?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < users.size(); i++) {
            UserEntity user = users.get(i);
            pstmt.setInt(1, user.getLoginCount());
            pstmt.setString(2, user.getNickname());
            pstmt.setTimestamp(3, new Timestamp(user.getRegisterDate().getTime()));
            pstmt.setString(4, user.getSalt());
            pstmt.setString(5, user.getPassword());
            pstmt.setLong(6, user.getId());
            pstmt.addBatch();
        }
        pstmt.executeBatch();
        pstmt.close();
        conn.close();
        log.info("insert to db over");
    }

    public static void createUserTicket(List<UserEntity> users) throws Exception {

        // 登录，生成token
        String urlString = "http://127.0.0.1:8080/login/doLogin";
        File file = new File("D:\\hs\\soft\\apache-jmeter-5.3\\秒杀\\config.txt");
        if (file.exists()) {
            file.delete();
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        file.createNewFile();
        raf.seek(0);
        for (int i = 0; i < users.size(); i++) {
            UserEntity user = users.get(i);
            URL url = new URL(urlString);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("POST");
            httpConn.setDoOutput(true);
            OutputStream out = httpConn.getOutputStream();
            String params = "mobile=" + user.getId() + "&password=" + MD5Util.inputPassToFormPass("111111");
            out.write(params.getBytes());
            out.flush();
            InputStream inputStream = httpConn.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte buff[] = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buff)) >= 0) {
                bout.write(buff, 0, len);
            }
            inputStream.close();
            bout.close();
            String response = new String(bout.toByteArray());
            ObjectMapper mapper = new ObjectMapper();
            RespBean respBean = mapper.readValue(response, RespBean.class);
            String userTicket = ((String) respBean.getObject());
            log.info("create userTicket : " + user.getId());

            String row = user.getId() + "," + userTicket;
            raf.seek(raf.length());
            raf.write(row.getBytes());
            raf.write("\r\n".getBytes());
            log.info("write to file : " + user.getId());
        }
        raf.close();

        log.info("write to file over");
    }

    private static Connection getConn() throws Exception {
        String url = "jdbc:mysql://127.0.0.1:3306/seckill?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "root";
        String driver = "com.mysql.cj.jdbc.Driver";
        Class.forName(driver);
        return DriverManager.getConnection(url, username, password);
    }
}
