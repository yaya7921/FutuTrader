package com.futu.openapi;
import com.futu.openapi.pb.*;

/***
 * 交易请求返回的回调函数
 */
public interface FTSPI_Trd {
    default void onReply_GetAccList(FTAPI_Conn client, int nSerialNo, TrdGetAccList.Response rsp){} //获取交易账户列表回调
    default void onReply_UnlockTrade(FTAPI_Conn client, int nSerialNo, TrdUnlockTrade.Response rsp){} //解锁回调
    default void onReply_SubAccPush(FTAPI_Conn client, int nSerialNo, TrdSubAccPush.Response rsp){} //订阅接收推送数据的交易账户回调
    default void onReply_GetFunds(FTAPI_Conn client, int nSerialNo, TrdGetFunds.Response rsp){} //获取账户资金回调
    default void onReply_GetPositionList(FTAPI_Conn client, int nSerialNo, TrdGetPositionList.Response rsp){} //获取账户持仓回调
    default void onReply_GetMaxTrdQtys(FTAPI_Conn client, int nSerialNo, TrdGetMaxTrdQtys.Response rsp){} //获取最大交易数量回调
    default void onReply_GetOrderList(FTAPI_Conn client, int nSerialNo, TrdGetOrderList.Response rsp){} //获取当日订单列表回调
    default void onReply_PlaceOrder(FTAPI_Conn client, int nSerialNo, TrdPlaceOrder.Response rsp){} //下单回调
    default void onReply_ModifyOrder(FTAPI_Conn client, int nSerialNo, TrdModifyOrder.Response rsp){} //修改订单回调
    default void onReply_GetOrderFillList(FTAPI_Conn client, int nSerialNo, TrdGetOrderFillList.Response rsp){} //获取当日成交列表回调
    default void onReply_GetHistoryOrderList(FTAPI_Conn client, int nSerialNo, TrdGetHistoryOrderList.Response rsp){} //获取历史订单列表回调
    default void onReply_GetHistoryOrderFillList(FTAPI_Conn client, int nSerialNo, TrdGetHistoryOrderFillList.Response rsp){} //获取历史成交列表回调
    default void onReply_GetMarginRatio(FTAPI_Conn client, int nSerialNo, TrdGetMarginRatio.Response rsp){} //获取融资融券数据
    default void onPush_UpdateOrder(FTAPI_Conn client, TrdUpdateOrder.Response rsp){} //推送订单变化
    default void onPush_UpdateOrderFill(FTAPI_Conn client, TrdUpdateOrderFill.Response rsp){} //推送订单成交
}

