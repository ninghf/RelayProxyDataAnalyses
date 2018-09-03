package com.butel.project.relay.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/7/12
 * @description TODO
 */
@Slf4j
@Getter
@Setter
public class BaseReqDto {

    private long startTime;
    private long endTime;

    private String superSocketId;
    private int transTime;
    private int limit;
    private int currentPage;

    public void decode(JSONObject data) {
        startTime = data.getLongValue("StartTime");
        endTime = data.getLongValue("EndTime");
        // 分析当前时间的前5分钟数据
        long curTime = System.currentTimeMillis();
        long _endTime = curTime - 5 * 60 * 1000;
        if (endTime > _endTime)
            endTime = _endTime;

        superSocketId = data.getString("SuperSocketID").trim();
        transTime = data.getIntValue("TransTime");

        limit = data.getIntValue("Limit");
        currentPage = data.getJSONObject("Pagination").getIntValue("CurrentPage");
    }

    public long getKey() {
//        return (superSocketId + startTime + endTime).hashCode();
        return new Random().nextLong();
    }
}
