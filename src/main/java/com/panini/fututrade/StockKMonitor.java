package com.panini.fututrade;

import com.futu.openapi.sample.TestQot;

public class StockKMonitor {
    public static void main(String[] args) {
        TestQot testQot = new TestQot();
        testQot.start(false);
        testQot.getGlobalState();
        testQot.sub();
        testQot.getSubInfo();
        testQot.requestHistoryKL();
        testQot.getBasicQot();
    }
}
