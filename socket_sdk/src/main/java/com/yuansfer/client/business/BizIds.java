package com.yuansfer.client.business;

/**
 * @Author Fly-Android
 * @CreateDate 2019/6/27 16:37
 * @Desciption 业务ID集合，注意这里的名称不能随便变更，需与Server一致
 */
public enum BizIds {

    //显示消息
    ShowMessage,
    //预付款订单
    OrderPay,
    //定单退款
    OrderRefund,
    //订单详情
    OrderDetail,
    //信用卡消费打印
    PrintCreditSale,
    //二维码消费打印
    PrintQrCodeSale
}
