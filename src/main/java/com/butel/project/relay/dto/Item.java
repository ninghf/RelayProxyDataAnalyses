package com.butel.project.relay.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/7/17
 * @description TODO
 */
@Slf4j
@ToString
@Getter
@Setter
public class Item {

    private static final String delimiter = "/";
    private static final String percent = "%";

    private String stat;
    private String total;
    private String nonRepeatTotal;
    private String lossRate;
    private String sendRate;
    private String fecLossRate;
    private String fecRate;
    private String repeatSpendRate;
    private String repeatWasteRate;
    private String tooltips;

    public Item(String stat) {
        this.stat = stat;
    }

    public Item total(long sendTotal, long recvTotal) {
        total = toStr(sendTotal, recvTotal);
        return this;
    }

    public Item nonRepeatTotal(long nonRepeatSendTotal, long nonRepeatRecvTotal) {
        nonRepeatTotal = toStr(nonRepeatSendTotal, nonRepeatRecvTotal);
        return this;
    }

    public Item lossRate(double lossRate) {
        this.lossRate = toStr(lossRate);
        return this;
    }

    public Item sendRate(double sendRate) {
        this.sendRate = toStr(sendRate);
        return this;
    }

    public Item fecLossRate(double fecLossRate) {
        this.fecLossRate = toStr(fecLossRate);
        return this;
    }

    public Item fecRate(double fecRate) {
        this.fecRate = toStr(fecRate);
        return this;
    }

    public Item repeatSpendRate(double repeatSpendRate) {
        this.repeatSpendRate = toStr(repeatSpendRate);
        return this;
    }

    public Item repeatWasteRate(double repeatWasteRate) {
        this.repeatWasteRate = toStr(repeatWasteRate);
        return this;
    }

    public Item tooltips(String tooltips) {
        this.tooltips = tooltips;
        return this;
    }

    public Item builder(long sendTotal, long recvTotal, String tooltips) {
        total(sendTotal, recvTotal);
        tooltips(tooltips);
        if (recvTotal > 0 && sendTotal > 0)
            lossRate(percentage_1(recvTotal, sendTotal));
        return this;
    }

    /**
     * p/q * 100
     * @param p
     * @param q
     * @return
     */
    public static double percentage(long p, long q) {
        return keepFourDecimalPlaces(divide(p, q) * 100, 2);
    }

    /**
     * (1 - p/q) * 100
     * @param p
     * @param q
     * @return
     */
    public static double percentage_1(long p, long q) {
        return keepFourDecimalPlaces((1 - divide(p, q)) * 100, 2);
    }

    /**
     * p/q
     * @param p
     * @param q
     * @return
     */
    public static double divide(long p, long q) {
        return (double) p/q;
    }

    /**
     * 保留[newScale]位小数,不进行四舍五入
     * @param d
     * @return
     */
    public static double keepFourDecimalPlaces(double d, int newScale) {
        BigDecimal decimal = new BigDecimal(d);
        double scaleValue = decimal.setScale(newScale, BigDecimal.ROUND_FLOOR).doubleValue();
        return scaleValue;
    }

    public Item builder(long sendTotal, long recvTotal, long nonRepeatSendTotal, long nonRepeatRecvTotal, double lossRate, double sendRate, double fecLossRate, double fecRate, double repeatSpendRate, double repeatWasteRate) {
        total(sendTotal, recvTotal);
        nonRepeatTotal(nonRepeatSendTotal, nonRepeatRecvTotal);
        lossRate(lossRate);
        sendRate(sendRate);
        fecLossRate(fecLossRate);
        fecRate(fecRate);
        repeatSpendRate(repeatSpendRate);
        repeatWasteRate(repeatWasteRate);
        return this;
    }

    public String toStr(long send, long recv) {
        StringBuilder builder = new StringBuilder();
        builder.append(send);
        builder.append(delimiter);
        builder.append(recv);
        return builder.toString();
    }

    public String toStr(double rate) {
        StringBuilder builder = new StringBuilder();
        builder.append(rate);
        builder.append(percent);
        return builder.toString();
    }
}
