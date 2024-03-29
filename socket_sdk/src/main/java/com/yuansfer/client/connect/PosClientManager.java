package com.yuansfer.client.connect;

import android.content.Context;
import android.util.ArrayMap;

import com.google.gson.Gson;
import com.yuansfer.client.business.request.BaseRequest;
import com.yuansfer.client.business.request.ShowMessageRequest;
import com.yuansfer.client.business.response.BaseResponse;
import com.yuansfer.client.protocol.PosMessage;
import com.yuansfer.client.service.PosClientService;
import com.yuansfer.client.listener.IConnectStateListener;
import com.yuansfer.client.listener.IMsgReplyListener;
import com.yuansfer.client.listener.ISessionListener;
import com.yuansfer.client.util.ResultCode;

import org.apache.mina.core.session.IoSession;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @Author Fly-Android
 * @CreateDate 2019/7/1 12:04
 * @Desciption Socket 连接及会话管理器
 */
public class PosClientManager {

    private static PosClientManager sInstance;
    private IConnectStateListener mConnectStateListener;
    private ISessionListener mSessionListener;
    private IoSession mSession;
    private ArrayMap<String, IMsgReplyListener> mRespListenerMap;
    private Gson mGson;

    private PosClientManager() {
        mGson = new Gson();
        mRespListenerMap = new ArrayMap<>();
    }

    public static PosClientManager getInstance() {
        if (sInstance == null) {
            synchronized (PosClientManager.class) {
                if (sInstance == null) {
                    sInstance = new PosClientManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 是否已与服务端连接
     *
     * @return
     */
    private boolean isConnSuccess() {
        return mSession != null && mSession.isConnected();
    }

    /**
     * 在server设备上显示消息
     *
     * @param message 消息内容
     * @return 是否发送成功
     */
    public boolean showMessage(String message) {
        if (isConnSuccess()) {
            return mSession.write(PosMessage.obtain(mGson.toJson(new ShowMessageRequest(message)))).isWritten();
        } else {
            mSessionListener.onMessageSendFail(PosMessage.obtain(mGson.toJson(new ShowMessageRequest(message))), "not found server session");
            return false;
        }
    }

    /**
     * 发送消息
     *
     * @param request 请求消息
     * @return 是否发送成功
     */
    public <T extends BaseRequest> boolean sendMessage(T request) {
        if (isConnSuccess()) {
            return mSession.write(PosMessage.obtain(mGson.toJson(request))).isWritten();
        } else {
            mSessionListener.onMessageSendFail(PosMessage.obtain(mGson.toJson(request)), "not found server session");
            return false;
        }
    }

    /**
     * 发送消息
     *
     * @param request  请求消息
     * @param listener 响应回调
     * @return 是否发送成功
     */
    public <T extends BaseRequest, R extends BaseResponse> boolean sendMessage(T request, IMsgReplyListener<R> listener) {
        if (isConnSuccess()) {
            if (listener != null && request.isNeedResponse()) {
                //需要server反馈时加入回调集合
                mRespListenerMap.put(request.getRequestId(), listener);
            }
            return mSession.write(PosMessage.obtain(mGson.toJson(request))).isWritten();
        } else {
            mSessionListener.onMessageSendFail(PosMessage.obtain(mGson.toJson(request)), "not found server session");
            if (listener != null) {
                listener.onFail(ResultCode.SESSION_CLOSED, "not found server session");
            }
            return false;
        }
    }

    /**
     * 保存连接session
     *
     * @param session
     */
    void saveSession(IoSession session) {
        if (session != null) {
            mSession = session;
        }
    }

    /**
     * 移除连接session
     */
    void deleteSession() {
        if (mSession != null) {
            mSession.closeOnFlush();
            mSession = null;
        }
    }

    /**
     * 启动连接
     *
     * @param context    Context
     * @param remoteAddr 远程地址
     */
    public void startDeviceConnect(Context context, String remoteAddr) {
        PosClientService.startService(context, remoteAddr);
    }

    /**
     * 启动连接
     *
     * @param context    Context
     * @param remoteAddr 远程地址
     * @param remotePort 远程端口
     */
    public void startDeviceConnect(Context context, String remoteAddr, int remotePort) {
        PosClientService.startService(context, remoteAddr, remotePort);
    }

    /**
     * 启动连接
     *
     * @param context Context
     * @param config  配置项
     */
    public void startDeviceConnect(Context context, PosConnectConfig config) {
        PosClientService.startService(context, config);
    }

    /**
     * 停止连接
     *
     * @param context Context
     */
    public void stopDeviceConnect(Context context) {
        PosClientService.stopService(context);
    }

    /**
     * 监听Socket连接状态
     *
     * @param listener 监听器
     */
    public void setOnConnectStateListener(IConnectStateListener listener) {
        this.mConnectStateListener = listener;
    }

    /**
     * 监听会话状态
     *
     * @param sessionListener 监听器
     */
    public void setOnSessionListener(ISessionListener sessionListener) {
        this.mSessionListener = sessionListener;
    }

    void notifySocketCreated() {
        if (mConnectStateListener != null) {
            mConnectStateListener.onDeviceConnected();
        }
    }

    void notifySocketDestroyed() {
        mSession = null;
        if (mConnectStateListener != null) {
            mConnectStateListener.onDeviceDisconnected();
        }
    }

    void notifySessionCreated(IoSession session) {
        if (mSession == null) {
            mSession = session;
        }
        if (mSessionListener != null) {
            mSessionListener.onSessionAdd(new PosSession(session.isConnected()
                    , session.getRemoteAddress(), session.getLocalAddress()));
        }
    }

    void notifySessionDestroyed(IoSession session) {
        mSession = null;
        if (mSessionListener != null) {
            mSessionListener.onSessionRemove(new PosSession(session.isConnected()
                    , session.getRemoteAddress(), session.getLocalAddress()));
        }
    }

    void notifySessionMessageSent(IoSession session, Object message) {
        if (mSessionListener != null) {
            mSessionListener.onMessageSent(new PosSession(session.isConnected()
                    , session.getRemoteAddress(), session.getLocalAddress()), message);
        }
    }

    void notifySessionMessageReceive(IoSession session, Object message) {
        IMsgReplyListener responseListener = null;
        if (mSessionListener != null) {
            mSessionListener.onMessageReceive(new PosSession(session.isConnected()
                    , session.getRemoteAddress(), session.getLocalAddress()), message);
        }
        try {
            if (message instanceof PosMessage) {
                PosMessage socketMsg = (PosMessage) message;
                BaseResponse response = mGson.fromJson(socketMsg.getContent(), BaseResponse.class);
                responseListener = mRespListenerMap.remove(response.getRequestId());
                if (responseListener != null) {
                    if (response.getRetCode() == ResultCode.REQUEST_SUCCESS) {
                        responseListener.onSuccess(mGson.fromJson(socketMsg.getContent(), genGenericInstance(responseListener.getClass())));
                    } else {
                        responseListener.onFail(response.getRetCode(), response.getRetMsg());
                    }
                }
            }
        } catch (Exception e) {
            if (responseListener != null) {
                responseListener.onFail(ResultCode.PARSE_ERROR, e.getMessage());
            }
            e.printStackTrace();
        }
    }

    <V extends BaseResponse> Class<V> genGenericInstance(Class clazz) throws ClassNotFoundException {
        Class<V> response;
        final Type[] types = clazz.getGenericInterfaces();
        if (types == null || types.length == 0 || !(types[0] instanceof ParameterizedType)) {
            return null;
        }
        final ParameterizedType type = (ParameterizedType) types[0];
        final String actArgs = type.getActualTypeArguments()[0].toString();
        if (actArgs.startsWith("class ")) {
            response = (Class<V>) Class.forName(actArgs.substring("class ".length()));
        } else {
            response = (Class<V>) Class.forName(actArgs);
        }
        return response;
    }

}
