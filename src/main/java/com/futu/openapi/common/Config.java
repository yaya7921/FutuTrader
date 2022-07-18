package com.futu.openapi.common;

import com.futu.openapi.pb.TrdCommon;
import com.futu.openapi.utils.MD5Util;;

public class Config {
    /*
     牛牛号
     */
    public static long userID = 12345;
    /*
    业务账号，每个市场都有独立的业务账号，可以通过getAccList获取到
     */
    public static long trdAcc = 123456L;
    /*
    解锁交易密码的md5
     */
    public static String unlockTradePwdMd5 = MD5Util.calcMD5("123456");
    /*
    trdAcc所属券商
     */
    public static TrdCommon.SecurityFirm securityFirm = TrdCommon.SecurityFirm.SecurityFirm_FutuSecurities;
    /*
    请求方ip
     */
    public static String opendIP = "127.0.0.1";
    /*
    端口
     */
    public static short opendPort = 11111;
    /*
    RSA私钥文件路径，用于加密和OpenD的连接
     */
    public static String rsaKeyFilePath = "";
}
