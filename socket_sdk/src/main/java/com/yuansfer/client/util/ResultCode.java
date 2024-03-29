package com.yuansfer.client.util;

/**
 * @author fly
 * @date 2019-08-16
 * @desc 状态码
 */
public class ResultCode {

    /**
     * 请求成功
     */
    public static final int REQUEST_SUCCESS = 0;
    /**
     * 会话已关闭
     */
    public static final int SESSION_CLOSED = -1;
    /**
     * 请求参数错误
     */
    public static final int PARAM_ERROR = -2;
    /**
     * 请求接口失败
     */
    public static final int API_FAIL = -3;
    /**
     * 解析错误
     */
    public static final int PARSE_ERROR = -4;
    /**
     * 用户取消
     */
    public static final int USER_CANCELLED = -5;
    /**
     * 网络不在线
     */
    public static final int NETWORK_OFFLINE = -6;
    /**
     * 未知错误
     */
    public static final int UNKNOWN_ERROR = -8;

}