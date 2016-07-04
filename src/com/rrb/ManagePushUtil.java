package cn.com.chebao.util;

import cn.jpush.api.JPushClient;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.audience.AudienceTarget;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import org.apache.log4j.Logger;

/**
 * 极光推送服务端，相关处理
 * 极光官网:https://www.jpush.cn/
 * Created by zxnui on 16-5-13.
 */
public class ManagePushUtil {

    private static Logger logger = Logger.getLogger(ManagePushUtil.class);

    /**
     * 极光推送使用，需要的基本信息
     */
    private static final String appKey="e6606f0c4bcd3134902440a7";
    private static final String secret="1e59d5c5f906124de95210cf";

    /**
     * 点击通知，跳转到车险支付
     */
    public static final Integer PUSH_TYPE_PAY=0;
    public static final String  PUSH_TYPE_PAY_STRING="您的订单已经精准报价，请付款";

    /**
     * 点击通知，跳转到行车历史
     */
    public static final Integer PUSH_TYPE_MILE=1;

    /**
     * 点击通知，跳转到通知页面
     */
    public static final Integer PUSH_TYPE_NOTI=2;

    /**
     * 点击通知，跳转到办理加油卡
     */
    public static final Integer PUSH_TYPE_GAS_INSERT=3;

    /**
     * 点击通知，跳转到加油充值
     */
    public static final Integer PUSH_TYPE_GAS_UPDATE=4;

    /**
     * 点击通知，跳转到加油充值记录
     */
    public static final Integer PUSH_TYPE_GAS_SELECT=5;

    /**
     * 点击通知，跳转到优惠券
     */
    public static final Integer PUSH_TYPE_COUPON=6;

    /**
     * 点击通知，跳转到推广提现
     */
    public static final Integer PUSH_TYPE_MONEY=7;

    /**
     * 点击通知，跳转到快修
     */
    public static final Integer PUSH_TYPE_QUERY_REPAIR=8;
    public static final String PUSH_TPPE_QUERY_STRING_MSG="尊敬的用户，修博士已为您的车损咨询提出一份初步建议！";
    public static final String PUSH_TPPE_QUERY_STRING_PRICE="尊敬的用户，手机车宝客服已针对您的车损咨询提出一份精准报价！";

    /**
     * ios平台
     */
    public static final String PLATFORM_IOS = "ios";

    /**
     * android平台
     */
    public static final String PLATFORM_ANDROID = "android";

    /**
     * 所有平台
     */
    public static final String PLATFORM_ALL_ANDROID = "all_android";
    public static final String PLATFORM_ALL_IOS = "all_IOS";

    /**
     * 极光推送，ios平台   生产模式：true 测试模式：false
     */
    private static final Boolean IOS_MODEL = true;

    /**
     * 对所有用户进行推送
     * @param msg   需要推送的消息
     * @param type  app点击通知，跳转的页面
     */
    public static void SendMsgToAll(String msg,Integer type,String platform) throws Exception{
        JPushClient jpushClient = new JPushClient(secret, appKey, 3);
        PushPayload payload = BuildMsg(msg, null, platform, type);
        jpushClient.sendPush(payload);
    }

    /**
     * 针对个人推送信息
     * @param message   需要推送的消息
     * @param userId    用户id
     * @param platform  平台
     * @param type      app点击通知，跳转的页面
     */
    public static void SendMsgToOne(String message,String userId,String platform,Integer type){
        try {
            JPushClient jpushClient = new JPushClient(secret, appKey, 3);
            PushPayload payload = BuildMsg(message, userId, platform, type);
            jpushClient.sendPush(payload);
        }catch (Exception e){
            //原因是：推送必须是在app集成了sdk，用户登录过app之后，才能进行推送。若用户没有使用最新版本登录过，推送就会出错
            logger.info("推送失败-------------------------------");
        }
    }

    /**
     * 极光推送参数组合
     * @param message   消息主题
     * @param userId    tag推送，tag=userId
     * @param platform  平台:android,ios,all
     * @param type      app点击通知，跳转的页面
     * @return          组合好的消息体
     */
    private static PushPayload BuildMsg(String message,String userId,String platform,Integer type){
        if (platform.equals(PLATFORM_ANDROID)) {
            return PushPayload.newBuilder()
                    .setPlatform(Platform.android())
                    .setAudience(Audience.newBuilder()
                            .addAudienceTarget(AudienceTarget.tag(userId)).build())
                    .setNotification(Notification.newBuilder()
                            .addPlatformNotification(AndroidNotification.newBuilder()
                                .setAlert(message)
                                .addExtra("type",type)
                                .build())
                            .build())
                    .build();
        }else if(platform.equals(PLATFORM_IOS)){
            return PushPayload.newBuilder()
                    .setPlatform(Platform.ios())
                    .setAudience(Audience.newBuilder()
                            .addAudienceTarget(AudienceTarget.tag(userId)).build())
                    .setNotification(Notification.newBuilder()
                            .addPlatformNotification(IosNotification.newBuilder()
                                    .setAlert(message)
                                    .addExtra("type",type)
                                    .setSound("default")
                                    .build())
                            .build())
                    //使用最新的推送接口，ios开发环境和生产环境设置必须通过接口参数来确定，ApnsProduction true：生产环境，false：开发环境
                    .setOptions(Options.newBuilder().setApnsProduction(IOS_MODEL).build())
                    .build();
        }else if(platform.equals(PLATFORM_ALL_ANDROID)){
            return PushPayload.newBuilder()
                    .setPlatform(Platform.android())
                    .setAudience(Audience.all())
                    .setNotification(Notification.newBuilder()
                            .addPlatformNotification(AndroidNotification.newBuilder()
                                .setAlert(message)
                                .addExtra("type",type)
                                .build())
                            .build())
                    .build();
        }else{
            return PushPayload.newBuilder()
                    .setPlatform(Platform.ios())
                    .setAudience(Audience.all())
                    .setNotification(Notification.newBuilder()

                    //使用最新的推送接口，ios开发环境和生产环境设置必须通过接口参数来确定，ApnsProduction true：生产环境，false：开发环境
                    .setOptions(Options.newBuilder().setApnsProduction(IOS_MODEL).build())
                    .build();
        }
    }

    public static void main(String args[]) throws Exception {
        String msg = "亲爱的月先生，手机车宝已经通过您的爱车核保价格，详细为：[第三者责任险30万:0;车船税:2000.00;交强险:5000.00;不计免赔(车辆损失险):0;车辆损失险:0;不计免赔(第三者责任险):0;第三者责任险30万:0;车船税:0;交强险:0;不计免赔(车辆损失险):0;车辆损失险:0;不计免赔(第三者责任险):0;]。如果确认，直接通过手机车宝app就可完成在线支付。全心全意，创新不止手机,车宝期待为您服务！若有任何问题，均可通过手机车宝App在线客服，或手机车宝客服电话：4000-361-362获得帮助。谢谢您。";
        SendMsgToOne(msg,"1780389",PLATFORM_ANDROID,0);
    }

}
