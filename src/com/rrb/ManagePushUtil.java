package com.rrb;

import cn.jpush.api.JPushClient;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.audience.AudienceTarget;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;

/**
 * 极光推送服务端，相关处理
 * 极光官网:https://www.jpush.cn/
 * Created by zxnui on 16-5-13.
 */
public class ManagePushUtil {

    /**
     * 极光推送使用，需要的基本信息
     */
    private static final String appKey="e6606f0c4bcd3134902440a7";
    private static final String secret="1e59d5c5f906124de95210cf";

    //通知类型
    public static final Integer PUSH_TYPE_PAY=0;
    public static final String  PUSH_TYPE_PAY_STRING="您的订单已经精准报价，请付款";

    public static final Integer PUSH_TYPE_MILE=1;
    public static final String  PUSH_TYPE_MILE_STRING="您的订单已经精准报价，请付款";

    public static final Integer PUSH_TYPE_NOTI=2;
    public static final String  PUSH_TYPE_NOTI_STRING="您的订单已经精准报价，请付款";

    public static final Integer PUSH_TYPE_GAS=3;
    public static final String  PUSH_TYPE_GAS_STRING="您的订单已经精准报价，请付款";

    /**
     * ios平台
     */
    public static final String PLATFORM_IOS = "ios";

    /**
     * android平台
     */
    public static final String PLATFORM_ANDROID = "android";

    /**
     * 针对个人推送信息
     * @param message   需要推送的消息
     * @param userId    用户id
     * @param platform  平台
     * @param type      消息类型
     */
    public static void SendMsgToOne(String message,String userId,String platform,Integer type){
        try {
            JPushClient jpushClient = new JPushClient(secret, appKey, 3);
            PushPayload payload = BuildMsg(message, userId, platform, type);
            jpushClient.sendPush(payload);
        }catch (Exception e){
            //原因是：推送必须是在app集成了sdk，用户登录过app之后，才能进行推送。若用户没有使用最新版本登录过，推送就会出错
        }
    }

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
                    .setMessage(Message.content(""))
                    .build();
        }else{
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
                    .setOptions(Options.newBuilder().setApnsProduction(true).build())
//                    .setMessage(Message.content(""))ios发送通知的时候，不能带上message,否则ios端会已message为主，忽视通知
                    .build();
        }
    }

    public static void main(String args[]) throws Exception {
        SendMsgToOne(PUSH_TYPE_PAY_STRING,"1798323",PLATFORM_IOS,PUSH_TYPE_PAY);
    }

}