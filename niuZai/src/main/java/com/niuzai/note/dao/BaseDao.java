package com.niuzai.note.dao;

import com.niuzai.note.util.DBUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by HMF on 2021/07/11 15:14
 */

/**
 * 基础的JDBC操作类：
 * --更新操作（添加、修改、删除）：
 * <p>
 * --查询操作：
 * ----1、查询一个字段（只会返回一条记录且只有一个字段；常用场景：查询总数量）
 * ----2、查询集合
 * ----3、查询某个对象
 */
public class BaseDao {
    /**
     * 更新操作：
     * --添加、修改、删除：
     * --1、得到数据库连接
     * --2、定义sql语句（包括添加语句、修改语句、删除语句）
     * --3、预编译
     * --4、如果有参数，则设置参数，下标从1开始（数组或集合、循环设置参数）
     * --5、执行更新，返回受影响行数
     * --6、关闭资源
     * <p>
     * 注：需要两个参数：sql语句、所需参数的集合
     *
     * @param sql
     * @param params
     * @return
     */
    public static int executeUpdate(String sql, List<Object> params) {
        int row = 0; // 返回受影响行数
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            // 得到数据库连接
            connection = DBUtil.getConnection();
            // 预编译
            preparedStatement = connection.prepareStatement(sql);
            // 如果有参数，则设置参数，下标从1开始
            if (params != null && params.size() > 0) {
                // 循环设置参数，设置参数类型为 Object
                for (int i = 0; i < params.size(); i++) {
                    // 给sql设置参数，下标从1开始
                    preparedStatement.setObject(i + 1, params.get(i));
                }
            }
            // 执行更新， 返回受影响行数
            row = preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(null, preparedStatement, connection);
        }

        return row;
    }

    /**
     * 查询操作：
     * --1、查询一个字段（只会返回一条记录且只有一个字段；常用场景：查询总数量）
     * 1、得到数据库连接
     * 2、定义sql语句
     * 3、预编译
     * 4、如果有参数，则设置参数，下标从0开始（数组或集合、循环设置参数）
     * 5、执行查询，返回结果集
     * 6、判断并分析结果集
     * 7、关闭资源
     * <p>
     * 注：需要两个参数：sql语句、所需参数的集合
     *
     * @param sql
     * @param params
     * @return
     */
    public static Object findSingleValue(String sql, List<Object> params) {
        Object object = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // 得到数据库连接
            connection = DBUtil.getConnection();
            // 预编译
            preparedStatement = connection.prepareStatement(sql);
            // 如果有参数，则设置参数，下标从1开始
            if (params != null && params.size() > 0) {
                // 循环设置参数，设置参数类型为 Object
                for (int i = 0; i < params.size(); i++) {
                    // 给sql设置参数，下标从1开始
                    preparedStatement.setObject(i + 1, params.get(i));
                }
            }
            // 执行查询，返回结果集
            resultSet = preparedStatement.executeQuery();
            // 判断并分析结果集
            if (resultSet.next()) {
                object = resultSet.getObject(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            DBUtil.close(resultSet, preparedStatement, connection);
        }

        return object;
    }

    /**
     * 查询操作：(JavaBean中的字段与数据库中表的字段对应）
     * --2、查询集合
     * 1、获取数据库连接
     * 2、定义sql语句
     * 3、预编译
     * 4、如果有参数，则设置参数，下标从1开始（数组或集合、循环设置参数）
     * 5、执行查询，得到结果集
     * 6、得到结果集的元数据对象（查询到的字段数量以及查询了那些字段）
     * 7、判断并分析结果集
     * ----8、实例化对象
     * ----9、遍历查询的字段数量，得到数据库中查询到的每一个列名
     * ----10、通过反射，使用列名得到对应的field对象
     * ----11、拼接set方法，得到字符串
     * ----12、通过反射，将set方法的字符串反射成类中的指定set方法
     * ----13、通过invoke调用set方法
     * ----14、将对应的JavaBean设置到集合中
     * 15、关闭资源
     *
     * @param sql
     * @param params
     * @param cls    // 用于反射操作的类
     * @return
     */
    public static List queryRows(String sql, List<Object> params, Class cls) {
        List list = new ArrayList(); // 返回的结果集合
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // 获取数据库连接
            connection = DBUtil.getConnection();
            // 预编译sql语句
            preparedStatement = connection.prepareStatement(sql);
            // 如果有参数，则设置参数，下标从1开始
            if (params != null && params.size() > 0) {
                // 循环设置参数，设置参数类型为 Object
                for (int i = 0; i < params.size(); i++) {
                    // 给sql设置参数，下标从1开始
                    preparedStatement.setObject(i + 1, params.get(i));
                }
            }
            // 执行查询，返回结果集
            resultSet = preparedStatement.executeQuery();

            // 得到结果集的元数据对象（查询到的字段数量以及查询了那些字段）
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            // 得到查询的字段数量
            int fieldNum = resultSetMetaData.getColumnCount();

            // 判断并分析结果集
            while (resultSet.next()) {
                // 通过反射，实例化对象 --------------
                Object object = cls.newInstance();
                // 遍历查询的字段数量，得到数据库中查询的每一个列名
                for (int i = 1; i <= fieldNum; i++) { // 下标从1开始
                    // 得到查询的每一个列名
                    // getColumnLabel(): 获取列名或别名
                    // getColumnName(): 获取列名
                    String columnName = resultSetMetaData.getColumnLabel(i); // 例如：如果是tb_user表，则获取 userId字段
                    // 通过反射，使用列名得到对应的field对象，Field类在java.lang.reflect.Field包中
                    Field field = cls.getDeclaredField(columnName);
                    // 拼接set方法，得到字符串
                    String setMethod = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
                    // 通过反射，将set方法字符串反射成类中对应的set方法
                    Method method = cls.getDeclaredMethod(setMethod, field.getType());
                    // 得到查询的每一个字段对应的值
                    Object value = resultSet.getObject(columnName);
                    // 通过invoke方法调用set方法
                    method.invoke(object, value);
                }
                // 将JavaBean设置到结合中
                list.add(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            DBUtil.close(resultSet, preparedStatement, connection);
        }

        return list;
    }

    /**
     * 查询集合：
     * --3、查询某个对象（其实就是查一个集合中的第一条记录）
     * 通过调用查询集合的方法，然后获取第一条记录，即为查询某个对象
     *
     * @param sql
     * @param params
     * @param cls
     * @return
     */
    public static Object queryRow(String sql, List<Object> params, Class cls) {
        // 查一个对象，其实就是查一个集合中的第一条记录
        List list = queryRows(sql, params, cls);
        Object object = null;
        // 如果集合不为空，则获取查询的第一条数据
        if (list != null && list.size() > 0) {
            object = list.get(0);
        }
        return object;
    }
}