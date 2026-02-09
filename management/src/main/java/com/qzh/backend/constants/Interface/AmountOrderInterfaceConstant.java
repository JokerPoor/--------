package com.qzh.backend.constants.Interface;

public interface AmountOrderInterfaceConstant {

    // 分页查询门店下的所有金额单（GET /amount/order/list）
    String AMOUNT_ORDER_LIST_GET = "GET:/amount/order/list";

    // 分页查询自己为付款人的金额单（GET /amount/order/list/payer）
    String AMOUNT_ORDER_LIST_PAYER_GET = "GET:/amount/order/list/payer";

    // 分页查询自己为收款人的金额单（GET /amount/order/list/payee）
    String AMOUNT_ORDER_LIST_PAYEE_GET = "GET:/amount/order/list/payee";

    // 查询金额单详情（GET /amount/order/{id}）
    String AMOUNT_ORDER_DETAIL_GET = "GET:/amount/order/:id";

    // 支付金额单（POST /amount/order/payorder/{id}）
    String AMOUNT_ORDER_PAY_POST = "POST:/amount/order/payorder/:id";

    // 取消金额单（POST /amount/order/cancelorder/{id}）
    String AMOUNT_ORDER_CANCEL_POST = "POST:/amount/order/cancelorder/:id";

    // 金额单支付回调通知（POST /amount/order/notify）
    String AMOUNT_ORDER_NOTIFY_POST = "POST:/amount/order/notify";
}