package org.wiselot.RPiContact.DataPool;

import org.jetbrains.annotations.NotNull;

import java.sql.*;

public class DataBase {

    private Connection connection = null;

    public DataBase(String driver)
    {
        // 加载驱动
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public DataBase connect(String host,String user,String passwd)
    {
        try {
            connection = DriverManager.getConnection(host, user, passwd);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return this;
    }
    public ResultSet selectObjects(@NotNull Object[] objects, String sql) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for(int i = 0;i<objects.length;i++){
            statement.setObject(i+1,objects[i]);
        }
        return statement.executeQuery();
    }

    public int updateObjects(@NotNull Object[] objects, String sql) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for(int i = 0;i<objects.length;i++){
            statement.setObject(i+1,objects[i]);
        }
        return statement.executeUpdate();
    }

    public void disconnect() throws SQLException {
        if(connection!=null){
            connection.close();
        }
    }

}