package com.seckill;

import org.apache.calcite.avatica.AvaticaConnection;
import org.apache.calcite.avatica.AvaticaStatement;

import java.sql.DriverManager;
import java.sql.ResultSet;

public class DruidTest {
    public static void main(String[] args) throws Exception {
        //链接地址
        String url = "jdbc:avatica:remote:url=http://192.168.220.128:8082/druid/v2/sql/avatica/";
        AvaticaConnection connection = (AvaticaConnection) DriverManager.getConnection(url);

        //SQL语句,查询2020-4-10 11:50:30之后的访问uri和访问数量
        String sql="SELECT * FROM logsitems";

        //创建Statment
        AvaticaStatement statement = connection.createStatement();

        //执行查询
        ResultSet resultSet = statement.executeQuery(sql);

        while (resultSet.next()) {
            //获取uri
            String uri = resultSet.getString("uri");
            System.out.println(uri+"--------->"+uri);
        }
        resultSet.close();
        statement.close();
        connection.close();
    }
}
