package com.atguigu.gmall.common.constant;

public class SysRedisConst {

    public static final String NULL_VALUE = "X";
    public static final String LOCK_SKU_DETAIL = "lock:sku:detail:";
    public static final Long NULL_VALUE_TTL = 60*30L; //三十分钟
    public static final Long SKUDETAIL_TTL = 60*60*24*7L; //七天
    public static final String SKU_INFO_PREFIX ="sku:info:" ;
    public static final String BLOOM_SKUID = "bloom:skuid";
    public static final String LOCK_PREFIX = "lock:";
    public static final String CACHE_CATEGORYS = "categorys";
    public static final int SEARCH_PAGESIZE = 10;
    public static final String SKU_HOTSCORE_PREFIX = "sku:hotscore:";
    public static final String LOGIN_USER = "user:login:";
    public static final String USERID_HEADER = "userid" ;
}
