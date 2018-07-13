package com.butel.project.relay.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
public class DelayedReq extends BaseReqDto {

    private long transTime;

    public void decode(JSONObject json) {
        JSONObject data = json.getJSONObject("data");
        super.decode(data);
        transTime = data.getLongValue("TransTime");
    }

    @Override
    public long getReqId() {
        String reqIdStr = getSuperSocketId() + getStartTime() + getEndTime() + getTransTime() + isDetail();
        return reqIdStr.hashCode();
    }
}
