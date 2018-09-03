package com.butel.project.relay.service;

import com.butel.project.relay.constant.StatObjType;
import com.butel.project.relay.meeting.MeetingIndex;
import com.butel.project.relay.meeting.MeetingOriginalData;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/8/10
 * @description TODO
 */
public interface IMeetingStatDataService {

    void decode(MeetingOriginalData originalData, MeetingIndex idx);
    void decode(MeetingOriginalData originalData, long startTime, long endTime, int bound, StatObjType objType, String objId);
}
