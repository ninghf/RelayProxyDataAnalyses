package com.butel.project.relay.service;

import com.butel.project.relay.analyses.Axis;
import com.butel.project.relay.analyses.AnalysesData;
import com.butel.project.relay.analyses.Packet;
import com.butel.project.relay.dto.DelayedReq;
import com.butel.project.relay.dto.LossReq;

import java.util.List;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/6/28
 * @description TODO
 */
public interface IAnalysesService {

    AnalysesData generateDelayed(long reqId, DelayedReq req);
    AnalysesData generateLoss(long reqId, LossReq req);

    List<Packet> getDelayedPackets(long startTime, long endTime, long transTime,
                                   String superSocketID, boolean isDetail, Axis axis);
    List<Packet> getLossPackets(long startTime, long endTime, List<Long> packetIds, int limit,
                                String superSocketID, boolean isDetail, Axis axis);
}
