package com.ben.core.connector.jdbc;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.*;
import java.util.function.Function;

/**
 * @author learn
 * @date 2022/7/29 16:39
 */
@Slf4j
public class JdbcStatementFunction {
    private final JdbcConnectionPool pool;
    public JdbcStatementFunction(JdbcConnectionPool pool) {
        this.pool = pool;
    }
    public List<Map<String,Object>> runQuery(String sql) throws SQLException {
        Connection connection = pool.getConnection();
        //log.info("------------获取的连接池连接: {}",connection);
        List<Map<String,Object>> result = new ArrayList<>();

        Statement statement = connection.createStatement();

        ResultSet results = statement.executeQuery(sql);

        ResultSetMetaData rsmd = results.getMetaData();
        while(results.next()){
            Map<String,Object> row = new HashMap<>();
            for(int i = 1;i<=rsmd.getColumnCount();i++){
                Object putValue;
                if(rsmd.getColumnClassName(i).equals("java.sql.Array")){
                    Array arr = results.getArray(i);
                    putValue = arr.getArray();

                }else{
                    putValue = results.getObject(rsmd.getColumnName(i));
                }
                row.put(rsmd.getColumnName(i),putValue);

            }
            result.add(row);
        }

        statement.close();
        results.close();
        connection.close();

        return result;
    }
    public int runUpdate(String sql) throws SQLException {
        Connection connection = pool.getConnection();
        Statement statement;
        int results;

        statement = connection.createStatement();
        results = statement.executeUpdate(sql);

        statement.close();
        connection.close();

        return results;
    }

    public <T> int[] runBatchUpdate(Iterable<T> it, Function<T, String> f) throws SQLException {
        Connection connection = pool.getConnection();
        Statement statement;
        int[] results;

        statement = connection.createStatement();
        for (T t : it) {
            statement.addBatch(f.apply(t));
        }
        results = statement.executeBatch();

        statement.close();
        connection.close();

        return results;
    }


    public int add(String sql, List<Object[]> data) throws SQLException {
        Connection connection = pool.getConnection();
        PreparedStatement preparedStatement = null;
        int[] result;
        try {
            preparedStatement = connection.prepareStatement(sql);
            for (Object obj : data) {
                Object[] field = (Object[]) obj;
                for (int j = 0; j < field.length; j++) {
                    preparedStatement.setObject(j + 1, field[j]);
                }
                preparedStatement.addBatch();
            }

            result = preparedStatement.executeBatch();
        } finally {
            if (preparedStatement != null){
                preparedStatement.close();
            }
            connection.close();
        }
        return data.size() == Arrays.stream(result).sum() ? data.size() : -1;
    }
    public boolean runExecute(String sql) throws SQLException {
        Connection connection = pool.getConnection();
        //log.info("------------获取的连接池连接: {}",connection);

        Statement statement = connection.createStatement();

        boolean results = statement.execute(sql);

        statement.close();
        connection.close();

        return results;
    }

}
