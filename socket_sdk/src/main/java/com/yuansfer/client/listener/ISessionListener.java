package com.yuansfer.client.listener;

import com.yuansfer.client.connect.PosSession;


/**
 * @Author Fly-Android
 * @CreateDate 2019/7/1 11:47
 * @Desciption 连接会话监听
 */
public interface ISessionListener {

    /**
     * 会话加入
     *
     * @param session 会话
     */
    void onSessionAdd(PosSession session);

    /**
     * 会话移除
     *
     * @param session 会话
     */
    void onSessionRemove(PosSession session);

    /**
     * 消息已送达
     *
     * @param session 会话
     * @param msg     消息内容
     */
    void onMessageSent(PosSession session, Object msg);

    /**
     * 消息发送失败
     *
     * @param msg    消息内容
     * @param reason 失败原由
     */
    void onMessageSendFail(Object msg, String reason);

    /**
     * 消息已到达
     *
     * @param session 会话
     * @param msg     消息内容
     */
    void onMessageReceive(PosSession session, Object msg);
}
