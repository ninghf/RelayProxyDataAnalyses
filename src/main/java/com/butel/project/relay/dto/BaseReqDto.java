package com.butel.project.relay.dto;

import com.alibaba.fastjson.JSONObject;
import com.butel.project.relay.analyses.Axis;
import com.butel.project.relay.analyses.Packet;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
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
    private boolean detail;
    private int limit;
    private int currentPage;

    public void decode(JSONObject data) {
        startTime = data.getLongValue("StartTime");
        endTime = data.getLongValue("EndTime");

        superSocketId = data.getString("SuperSocketID").trim();
        detail = data.getBoolean("IsDetail");

        limit = data.getIntValue("Limit");
        currentPage = data.getJSONObject("Pagination").getIntValue("CurrentPage");
    }

    public long getKey() {
//        return (superSocketId + startTime + endTime).hashCode();
        return new Random().nextLong();
    }
}
