package com.futu.openapi.utils;

import java.util.ArrayList;
import java.util.List;

public class MACDUtil {
    public static void calcEMA(List<Double> input, int n, List<Double> output) {
        int inputSize = input.size();
        if (inputSize > 0) {
            double lastEMA = input.get(0);
            output.add(lastEMA);
            for (int i = 1; i < inputSize; i++) {
                double curEMA = (input.get(i) * 2 + lastEMA * (n - 1)) / (n + 1);
                output.add(curEMA);
                lastEMA = curEMA;
            }
        }
    }

    public static void calcMACD(List<Double> closeList, int shortPeriod, int longPeriod, int smoothPeriod,
                         List<Double> difList, List<Double> deaList, List<Double> macdList) {
        difList.clear();
        deaList.clear();
        macdList.clear();
        List<Double> shortEMA = new ArrayList<>();
        List<Double> longEMA = new ArrayList<>();
        calcEMA(closeList, shortPeriod, shortEMA);
        calcEMA(closeList, longPeriod, longEMA);
        int shortCount = shortEMA.size();
        int longCount = longEMA.size();
        for (int i = 0; i < shortCount && i < longCount; i++) {
            difList.add(shortEMA.get(i) - longEMA.get(i));
        }

        calcEMA(difList, smoothPeriod, deaList);
        int difCount = difList.size();
        int deaCount = deaList.size();
        for (int i = 0; i < difCount && i < deaCount; i++) {
            macdList.add((difList.get(i) - deaList.get(i)) * 2);
        }
    }
}

