package com.panini.fututrade.profile;

import com.futu.openapi.utils.MD5Util;

/**
 * @author shuyun
 */
public class ProfileConfig {
    /*
     牛牛号
     */
    public static long userID = 30036762;
    /*
    解锁交易密码的md5
     */
    public static String unlockTradePwdMd5 = MD5Util.calcMD5("Neal2142");
}
