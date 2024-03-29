package test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.yuansfer.app.R;
import com.yuansfer.client.business.bean.OrderDetailBean;
import com.yuansfer.client.business.request.OrderDetailRequest;
import com.yuansfer.client.business.request.OrderPayRequest;
import com.yuansfer.client.business.request.OrderRefundRequest;
import com.yuansfer.client.business.request.PrintCreditSaleRequest;
import com.yuansfer.client.business.request.PrintQrCodeSaleRequest;
import com.yuansfer.client.business.response.BaseResponse;
import com.yuansfer.client.business.response.OrderDetailResponse;
import com.yuansfer.client.business.response.OrderPayResponse;
import com.yuansfer.client.business.response.OrderRefundResponse;
import com.yuansfer.client.connect.PosClientManager;
import com.yuansfer.client.connect.PosSession;
import com.yuansfer.client.listener.IConnectStateListener;
import com.yuansfer.client.listener.IMsgReplyListener;
import com.yuansfer.client.listener.ISessionListener;


public class MainActivity extends AppCompatActivity {
    EditText etRet, etRefundNo, etDetailNo;
    OrderDetailBean orderDetailBean;
    int seqNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etRet = findViewById(R.id.tv_ret);
        etRet.setMovementMethod(ScrollingMovementMethod.getInstance());
        etRefundNo = findViewById(R.id.edt_no_one);
        etDetailNo = findViewById(R.id.edt_no_two);
        final EditText etIP = findViewById(R.id.edt_ip);
        final EditText etPort = findViewById(R.id.edt_port);
        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PosClientManager.getInstance().startDeviceConnect(MainActivity.this,
                        etIP.getText().toString(), Integer.parseInt(etPort.getText().toString()));
            }
        });
        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PosClientManager.getInstance().stopDeviceConnect(MainActivity.this);
            }
        });
        findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preOrder(0.01);
            }
        });
        findViewById(R.id.btn4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PosClientManager.getInstance().showMessage("show message on yuansfer pos terminal");
            }
        });
        findViewById(R.id.btn5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etRefundNo.getText().toString())) {
                    Toast.makeText(MainActivity.this, "请输入订单号", Toast.LENGTH_LONG).show();
                    return;
                }
                orderRefund();
            }
        });
        findViewById(R.id.btn6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etDetailNo.getText().toString())) {
                    Toast.makeText(MainActivity.this, "请输入订单号", Toast.LENGTH_LONG).show();
                    return;
                }
                orderDetail();
            }
        });
        findViewById(R.id.btn7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printCredit();
            }
        });
        findViewById(R.id.btn8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printQrCode();
            }
        });
        PosClientManager.getInstance().setOnConnectStateListener(new IConnectStateListener() {
            @Override
            public void onDeviceConnected() {
                setTextMessage("P2P服务已连接");
            }

            @Override
            public void onDeviceDisconnected() {
                setTextMessage("P2P服务已关闭");
            }
        });

        PosClientManager.getInstance().setOnSessionListener(new ISessionListener() {
            @Override
            public void onSessionAdd(PosSession session) {
                appendTextMessage(String.format("P2P客户端已连接服务器%s", session.getRemoteAddress()));
            }

            @Override
            public void onSessionRemove(PosSession session) {
                appendTextMessage(String.format("P2P客户端已退出服务器%s", session.getRemoteAddress()));
            }

            @Override
            public void onMessageSent(PosSession session, Object msg) {
                appendTextMessage(String.format("P2P客户端发送了消息：%s", msg.toString()));
            }

            @Override
            public void onMessageSendFail(Object msg, String reason) {
                appendTextMessage(String.format("P2P客户发送消息失败：%s", reason));
            }

            @Override
            public void onMessageReceive(PosSession session, Object msg) {
                appendTextMessage(String.format("P2P客户端收到服务端发来的消息：%s", msg.toString()));
            }
        });
    }

    private void preOrder(double amount) {
        PosClientManager.getInstance().sendMessage(new OrderPayRequest("32", amount)
                , new IMsgReplyListener<OrderPayResponse>() {
                    @Override
                    public void onSuccess(OrderPayResponse response) {
                        appendTextMessage("支付成功，订单：" + response.getOrderPay());
                    }

                    @Override
                    public void onFail(int errCode, String errMsg) {
                        appendTextMessage("支付失败：" + errMsg);
                    }
                });
    }

    private void orderRefund() {
        OrderRefundRequest request = new OrderRefundRequest();
        request.setTransactionNo(etRefundNo.getText().toString());
        request.setRefundAdmAccId("3000140017");
        request.setRefundAdmPassword("111111");
        request.setRefundAmount(0.01);
        PosClientManager.getInstance().sendMessage(request, new IMsgReplyListener<OrderRefundResponse>() {
            @Override
            public void onSuccess(OrderRefundResponse response) {
                appendTextMessage("退款成功，订单：" + response.getOrderRefund());
            }

            @Override
            public void onFail(int errorCode, String errorMsg) {
                appendTextMessage("退款失败：" + errorMsg);
            }
        });
    }

    private void orderDetail() {
        OrderDetailRequest request = new OrderDetailRequest(etDetailNo.getText().toString());
        PosClientManager.getInstance().sendMessage(request, new IMsgReplyListener<OrderDetailResponse>() {
            @Override
            public void onSuccess(OrderDetailResponse response) {
                orderDetailBean = response.getOrderDetail();
                appendTextMessage("查询订单成功：" + orderDetailBean);
            }

            @Override
            public void onFail(int errorCode, String errorMsg) {
                appendTextMessage("查询订单失败：" + errorMsg);
            }
        });
    }

    private void printCredit() {
        if (orderDetailBean == null) {
            Toast.makeText(this, "请先获取订单详情", Toast.LENGTH_LONG).show();
            return;
        }
        PrintCreditSaleRequest request = new PrintCreditSaleRequest();
        request.setAmount(orderDetailBean.getAmount());
        request.setConvenientFee(orderDetailBean.getConvenientFee());
        request.setConvenientFeeRemovedAmount(orderDetailBean.getConvenientFeeRemovedAmount());
        request.setCurrency(orderDetailBean.getCurrency());
        request.setSupplierPayTime(orderDetailBean.getSupplierPayTime());
        request.setSupplierTransactionNo(orderDetailBean.getSupplierTransactionNo());
        request.setTax(orderDetailBean.getTax());
        request.setTaxRemovedAmount(orderDetailBean.getTaxRemovedAmount());
        request.setUserLoginId(orderDetailBean.getUserLoginId());
        request.setTransactionNo(orderDetailBean.getTransactionNo());
        OrderDetailBean.TsysExtraInfoBean tSys = orderDetailBean.getTsysExtraInfo();
        if (tSys != null) {
            request.setAid(tSys.getAid());
            request.setAuthCode(tSys.getAuthCode());
            request.setCredDebitType(tSys.getCredDebitType());
            request.setCredType(tSys.getCredType());
            request.setEntryModel(tSys.getEntryModel());
        }
        PosClientManager.getInstance().sendMessage(request, new IMsgReplyListener<BaseResponse>() {
            @Override
            public void onSuccess(BaseResponse response) {
                Toast.makeText(MainActivity.this, "打印成功", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFail(int errorCode, String errorMsg) {
                Toast.makeText(MainActivity.this, "打印失败:" + errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void printQrCode() {
        if (orderDetailBean == null) {
            Toast.makeText(this, "请先获取订单详情", Toast.LENGTH_LONG).show();
            return;
        }
        PrintQrCodeSaleRequest request = new PrintQrCodeSaleRequest();
        request.setAmount(orderDetailBean.getAmount());
        request.setConvenientFee(orderDetailBean.getConvenientFee());
        request.setConvenientFeeRemovedAmount(orderDetailBean.getConvenientFeeRemovedAmount());
        request.setCurrency(orderDetailBean.getCurrency());
        request.setSupplierUserLoginId(orderDetailBean.getUserLoginId());
        request.setSupplierPayTime(orderDetailBean.getSupplierPayTime());
        request.setSupplierTransId(orderDetailBean.getSupplierTransactionNo());
        request.setTax(orderDetailBean.getTax());
        request.setTaxRemovedAmount(orderDetailBean.getTaxRemovedAmount());
        request.setCashierNo(orderDetailBean.getCashierNo());
        request.setExchangeRate(orderDetailBean.getExchangeRate());
        request.setNetReceivable(orderDetailBean.getNetReceivable());
        request.setPaymentChannelValue(orderDetailBean.getPaymentChannelValue());
        request.setRefundAmount(orderDetailBean.getRefundAmount());
        request.setTransactionNo(orderDetailBean.getTransactionNo());
        request.setTip(orderDetailBean.getTip());
        PosClientManager.getInstance().sendMessage(request, new IMsgReplyListener<BaseResponse>() {
            @Override
            public void onSuccess(BaseResponse response) {
                Toast.makeText(MainActivity.this, "打印成功", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFail(int errorCode, String errorMsg) {
                Toast.makeText(MainActivity.this, "打印失败:" + errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setTextMessage(String msg) {
        seqNo = 1;
        etRet.setText(String.format("%s->%s\n", seqNo, msg));
        etRet.setSelection(etRet.length());
    }

    private void appendTextMessage(String msg) {
        etRet.append(String.format("%s->%s\n", ++seqNo, msg));
        etRet.setSelection(etRet.length());
    }

}
