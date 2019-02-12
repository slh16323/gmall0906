package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.annotations.LoginRequried;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.conf.AlipayConfig;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PlaymetController {

    @Autowired
    AlipayClient alipayClient;

    @Reference
    OrderService orderService;

    @Autowired
    PaymentInfoService paymentInfoService;

    @RequestMapping("/alipay/callback/return")
    public String callBackReturn(HttpServletRequest request, Map<String,String> paramsMap){

        String trade_no = request.getParameter("trade_no");
        String notify_type = request.getParameter("notify_type");
        String out_trade_no = request.getParameter("out_trade_no");

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus("订单已支付");
        paymentInfo.setAlipayTradeNo(trade_no);
        paymentInfo.setCallbackContent(paramsMap.toString());
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setOutTradeNo(out_trade_no);

        paymentInfoService.updata(paymentInfo);

        return "finish";
    }

    @RequestMapping("/alipay/submit")
    @ResponseBody
    @LoginRequried(autoRedirect = true)
    public String alipayIndex(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request) {

        OrderInfo orderInfo = orderService.getOrderByOutTradeNo(outTradeNo);

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        String skuName = orderDetailList.get(1).getSkuName();


        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("out_trade_no", outTradeNo);
        resultMap.put("product_code", "FAST_INSTANT_TRADE_PAY");
        resultMap.put("total_amount", "0.01");
        resultMap.put("subject", skuName);


        alipayRequest.setBizContent(JSON.toJSONString(resultMap));//填充业务参数
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        PaymentInfo paymentInfo = new PaymentInfo();

        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setOutTradeNo(outTradeNo);
        paymentInfo.setPaymentStatus("订单未支付");
        paymentInfo.setSubject(skuName);
        paymentInfo.setTotalAmount(totalAmount);

        paymentInfoService.sava(paymentInfo);


        return form;
    }

    @RequestMapping("paymentIndex")
    @LoginRequried(autoRedirect = true)
    public String playmentIndex(HttpServletRequest request, String outTradeNo, BigDecimal totalAmount, ModelMap map) {

        String userId = (String)request.getAttribute("userId");

        map.put("outTradeNo", outTradeNo);
        map.put("totalAmount", totalAmount);

        return "paymentindex";
    }
}
