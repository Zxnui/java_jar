package com.rrb;

import org.apache.log4j.Logger;

import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * 极光单独手动推送一些信息
 * Created by zxnui on 16-5-20.
 */
public class Main {

    /**
     * 日志
     */
    private static Logger logger = Logger.getLogger(Main.class);


    /**
     * 连接驱动
     */
    private static final String DRIVER = "com.mysql.jdbc.Driver";

    /**
     * 连接路径
     */
    private static final String URL = "jdbc:mysql://192.168.0.123:3306/cbubi?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&relaxAutoCommit=true&zeroDateTimeBehavi";    // 用户名

    /**
     * 用户
     */
    private static final String USERNAME = "tangjuan";

    /**
     * 密码
     */
    private static final String PASSWORD = "ApiDevTang*&$juan001";

    /**
     * 加载驱动
     */
    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取数据库连接
     * @return
     */
    private static Connection getConnection() {
        Connection conn = null;
        logger.info("开始连接数据库");
        try{
            conn=DriverManager.getConnection(URL, USERNAME, PASSWORD);
        }catch(SQLException e){
            e.printStackTrace();
            logger.info("数据库连接失败！");
        }
        logger.info("数据库连接成功");
        return conn;
    }

    /**
     * 关闭数据库连接
     * @param rs
     * @param ps
     * @param conn
     */
    private static void close(ResultSet rs, Statement ps, Connection conn) {
        if(rs!=null){
            try{
                rs.close();
                rs=null;
            }catch(SQLException e){
                e.printStackTrace();
                logger.info("关闭ResultSet失败");
            }
        }
        if(ps!=null){
            try{
                ps.close();
                ps=null;
            }catch(SQLException e){
                e.printStackTrace();
                logger.info("关闭PreparedStatement失败");
            }
        }
        if(conn!=null){
            try{
                conn.close();
                conn=null;
            }catch(SQLException e){
                e.printStackTrace();
                logger.info("关闭Connection失败");
            }
        }
    }

    /**
     * 睡眠，确保满足极光推送频率限制
     */
    private static void sleepSomeTime(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        String msg = "【手机车宝】绑定加油卡就可获得10元加油金,木有油卡肿么办？打开手机车宝App，60秒免费办理加油卡！";

        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        String sql;

        //未加油用户
        sql = "select user_id from v3_cb_user where user_id not in(select user_id from v3_cb_user_oil_card) and user_id>749";
//        sql = "select user_id from v3_cb_user where user_id = 2024014";
        ResultSet set = stmt.executeQuery(sql);
        LinkedList<Integer> userId = new LinkedList<>();
        while(set.next()){
            userId.add(set.getInt("user_id"));
        }
        set.close();

        //用户登录方式
        Date time = new Date();
        Double count = 1.0d;
        for(int i = 0;i < userId.size(); i++ ) {

            //当每分钟发送推送次数大于600次时，睡眠一段时间，确保发送频率不能超过规定的次数，既每分钟最多600次推送
            if(i/(599*count)>1){
                Date now = new Date();
                long diff = (long) (60000*count-(now.getTime()-time.getTime()));
                if(diff>0){
                    sleepSomeTime(diff);
                }
                count++;
            }
            logger.info("user_id = " + userId.get(i)+"还剩下  "+(int)(userId.size()-i)+"  个用户正在推送");
            sql = "select source_type from v3_cb_user_login_log where user_id = "+userId.get(i)+" ORDER By id desc limit 1";
            set = stmt.executeQuery(sql);
            Integer type=null;
            while (set.next()) {
                type = set.getInt("source_type");
            }
            set.close();
            if (type!=null&&type!=3){
                if(type==0){//android推送
                    ManagePushUtil.SendMsgToOne(msg,userId.get(i)+"",ManagePushUtil.PLATFORM_ANDROID,ManagePushUtil.PUSH_TYPE_GAS);
                }else if(type==1){//ios推送
                    ManagePushUtil.SendMsgToOne(msg,userId.get(i)+"",ManagePushUtil.PLATFORM_IOS,ManagePushUtil.PUSH_TYPE_GAS);
                }

            }
        }

        close(set,stmt,conn);

    }
}