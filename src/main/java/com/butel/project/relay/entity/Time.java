package com.butel.project.relay.entity;

import lombok.Data;

@Data
public class Time {

    private long timestamp;
    private long sendTime;
    private long recvTime;
    private long adjustedTime;

    public long adjust() {
        return adjustTime(recvTime, sendTime, timestamp);
    }

    /**
     * 校准日志时间
     *
     * @param sendTime
     *            日志发送时间
     * @param happenedTime
     *            日志真是发生时间
     * @return
     */
    private long adjustTime(long currentTime, long sendTime, long happenedTime) {
        if (happenedTime > sendTime) {// 发生时间大于发送时间用系统当前时间;
            return currentTime;
        }
        long time_diff = sendTime - happenedTime;
        return adjustTime(currentTime, time_diff);
    }

    private long adjustTime(long currentTime, long time_diff) {
        if (time_diff < 0) {
            return currentTime;
        }
        long time2 = currentTime - time_diff;
        return time2;
    }
}
