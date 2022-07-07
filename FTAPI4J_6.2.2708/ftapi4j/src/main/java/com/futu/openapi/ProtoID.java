package com.futu.openapi;

public interface ProtoID {
    int INIT_CONNECT = 1001; //初始化连接
    int GETGLOBALSTATE = 1002; //获取全局状态
    int NOTIFY = 1003; //推送通知
    int KEEP_ALIVE = 1004; //心跳
    int QOT_SUB = 3001; //订阅或者反订阅
    int QOT_REGQOTPUSH = 3002; //注册推送
    int QOT_GETSUBINFO = 3003; //获取订阅信息
    int QOT_GETTICKER = 3010; //获取逐笔,调用该接口前需要先订阅(订阅位：Qot_Common.SubType_Ticker)
    int QOT_GETBASICQOT = 3004; //获取基本行情,调用该接口前需要先订阅(订阅位：Qot_Common.SubType_Basic)
    int QOT_GETORDERBOOK = 3012; //获取摆盘,调用该接口前需要先订阅(订阅位：Qot_Common.SubType_OrderBook)
    int QOT_GETKL = 3006; //获取K线，调用该接口前需要先订阅(订阅位：Qot_Common.SubType_KL_XXX)
    int QOT_GETRT = 3008; //获取分时，调用该接口前需要先订阅(订阅位：Qot_Common.SubType_RT)
    int QOT_GETBROKER = 3014; //获取经纪队列，调用该接口前需要先订阅(订阅位：Qot_Common.SubType_Broker)
    // int QOT_GETHISTORYKL = 3100; //获取本地历史K线
    // int QOT_GETHISTORYKLPOINTS = 3101; //获取多股票多点本地历史K线
    int QOT_GETREHAB = 3102; //获取本地历史复权信息
    int QOT_REQUESTREHAB = 3105; //在线请求历史复权信息，不读本地历史数据DB
    int QOT_REQUESTHISTORYKL = 3103; //在线请求历史K线，不读本地历史数据DB
    int QOT_REQUESTHISTORYKLQUOTA = 3104; //获取历史K线已经用掉的额度
    int QOT_GETTRADEDATE = 3200; //获取交易日
    int QOT_GETSTATICINFO = 3202; //获取静态信息
    int QOT_GETSECURITYSNAPSHOT = 3203; //获取股票快照
    int QOT_GETPLATESET = 3204; //获取板块集合下的板块
    int QOT_GETPLATESECURITY = 3205; //获取板块下的股票
    int QOT_GETREFERENCE = 3206; //获取相关股票
    int QOT_GETOWNERPLATE = 3207; //获取股票所属的板块
    int QOT_GETHOLDINGCHANGELIST = 3208; //获取大股东持股变化列表
    int QOT_GETOPTIONCHAIN = 3209; //筛选期权
    int QOT_GETWARRANT = 3210; //筛选窝轮
    int QOT_GETCAPITALFLOW = 3211; //获取资金流向
    int QOT_GETCAPITALDISTRIBUTION = 3212; //获取资金分布
    int QOT_GETUSERSECURITY = 3213; //获取自选股分组下的股票
    int QOT_MODIFYUSERSECURITY = 3214; //修改自选股分组下的股票
    int QOT_UPDATEBASICQOT = 3005; //推送基本行情
    int QOT_UPDATEKL = 3007; //推送K线
    int QOT_UPDATERT = 3009; //推送分时
    int QOT_UPDATETICKER = 3011; //推送逐笔
    int QOT_UPDATEORDERBOOK	= 3013; //推送买卖盘
    int QOT_UPDATEBROKER = 3015; //推送经纪队列
    int QOT_UPDATEPRICEREMINDER = 3019; //到价提醒通知
    int QOT_STOCKFILTER = 3215;  //获取条件选股
    int QOT_GETCODECHANGE = 3216;   //获取股票代码变化信息
    int QOT_GETIPOLIST = 3217;      //获取新股Ipo
    int QOT_GETFUTUREINFO = 3218;   //获取期货合约资料
    int QOT_REQUESTTRADEDATE = 3219; //在线拉取交易日
    int QOT_SETPRICEREMINDER = 3220;  //设置到价提醒
    int QOT_GETPRICEREMINDER = 3221;  // 获取到价提醒
    int QOT_GETUSERSECURITYGROUP = 3222; //获取自选股分组
    int QOT_GETMARKETSTATE = 3223; //获取指定品种的市场状态
    int QOT_GETOPTIONEXPIRATIONDATE = 3224;  //获取期权到期日

    public static final int TRD_GETACCLIST = 2001; //获取交易账户列表
    public static final int TRD_UNLOCKTRADE = 2005; //解锁
    public static final int TRD_SUBACCPUSH = 2008; //订阅接收推送数据的交易账户
    public static final int TRD_GETFUNDS = 2101; //获取账户资金
    public static final int TRD_GETPOSITIONLIST = 2102; //获取账户持仓
    public static final int TRD_GETMAXTRDQTYS = 2111; //获取最大交易数量
    public static final int TRD_GETORDERLIST = 2201; //获取当日订单列表
    public static final int TRD_PLACEORDER = 2202; //下单
    public static final int TRD_MODIFYORDER = 2205; //修改订单
    public static final int TRD_UPDATEORDER = 2208; // 订单状态变动通知(推送)
    public static final int TRD_GETORDERFILLLIST = 2211; //获取当日成交列表
    public static final int TRD_UPDATEORDERFILL	= 2218;  //成交通知(推送)
    public static final int TRD_GETHISTORYORDERLIST = 2221; //获取历史订单列表
    public static final int TRD_GETHISTORYORDERFILLLIST = 2222; //获取历史成交列表
    public static final int TRD_GETMARGINRATIO = 2223;  //获取融资融券数据

    public static boolean isPushProto(int protoID) {
        return protoID == QOT_UPDATEBASICQOT ||
                protoID == QOT_UPDATEBROKER ||
                protoID == QOT_UPDATEKL ||
                protoID == QOT_UPDATEORDERBOOK ||
                protoID == QOT_UPDATEPRICEREMINDER ||
                protoID == QOT_UPDATERT ||
                protoID == QOT_UPDATETICKER ||
                protoID == TRD_UPDATEORDER ||
                protoID == TRD_UPDATEORDERFILL ||
                protoID == NOTIFY;
    }
}

