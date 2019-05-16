package com.jd.journalq.broker.limit.config;

import com.jd.journalq.toolkit.config.PropertySupplier;

/**
 * LimitConfig
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public class LimitConfig {

    public static final int DELAY_DYNAMIC = -1;

    private PropertySupplier propertySupplier;

    public LimitConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public boolean isEnable() {
        return propertySupplier.getValue(LimitConfigKey.ENABLE);
    }

    public int getDelay() {
        return propertySupplier.getValue(LimitConfigKey.DELAY);
    }

    public int getMaxDelay() {
        return propertySupplier.getValue(LimitConfigKey.MAX_DELAY);
    }

    public String getRejectedStrategy() {
        return propertySupplier.getValue(LimitConfigKey.REJECTED_STRATEGY);
    }
}