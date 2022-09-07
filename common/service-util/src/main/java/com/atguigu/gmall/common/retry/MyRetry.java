package com.atguigu.gmall.common.retry;

import feign.RetryableException;
import feign.Retryer;

public class MyRetry implements Retryer {
    private int cur = 0;
    private int max = 2;
    public MyRetry(){
        cur = 0;
        max = 2;
    }

    /**
     * 自定义重试次数
     * @param e
     */
    @Override
    public void continueOrPropagate(RetryableException e) {
        //Retryer.NEVER_RETRY.continueOrPropagate(e);
        throw e;
    }

    @Override
    public Retryer clone() {
        return this;
    }
}
