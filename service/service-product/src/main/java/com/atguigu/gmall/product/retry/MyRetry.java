package com.atguigu.gmall.product.retry;

import feign.RetryableException;
import feign.Retryer;

public class MyRetry implements Retryer {

    @Override
    public void continueOrPropagate(RetryableException e) {
        Retryer.NEVER_RETRY.continueOrPropagate(e);
    }

    @Override
    public Retryer clone() {
        return new MyRetry();
    }
}
