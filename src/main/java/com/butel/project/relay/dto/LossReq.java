package com.butel.project.relay.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
public class LossReq extends BaseReqDto {

    private List<Long> packetIds;

    public void decode(JSONObject json) {
        JSONObject data = json.getJSONObject("data");
        super.decode(data);
        String packetIDStr = data.getString("PacketIDs");
        packetIds = new ArrayList<>();
        if (!StringUtils.isEmpty(packetIDStr)) {
            String[] packetIDsStr = packetIDStr.split(",");
            for (int i = 0; i < packetIDsStr.length; i++) {
                try {
                    packetIds.add(Long.parseLong(packetIDsStr[i].trim()));
                } catch (NumberFormatException e) {
                    log.error("解析数据包ID时异常：", e);
                }
            }
        }
    }

    @Override
    public long getReqId() {
        String reqIdStr = getSuperSocketId() + getStartTime() + getEndTime() + getPacketIds() + isDetail();
        return reqIdStr.hashCode();
    }
}
