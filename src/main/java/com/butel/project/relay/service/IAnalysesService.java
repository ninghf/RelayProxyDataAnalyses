package com.butel.project.relay.service;

import com.butel.project.relay.constant.StatObjType;
import com.butel.project.relay.meeting.MeetingAnalysesData;
import com.butel.project.relay.meeting.MeetingOriginalData;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/6/28
 * @description TODO
 */
public interface IAnalysesService {

    MeetingAnalysesData generateAnalysesData(long key, long startTime, long endTime, int transTime, StatObjType objType, String objId);
    MeetingOriginalData generateOriginalData(long startTime, long endTime, StatObjType objType, String objId);
    void generateOriginalData(String meetingId);
}
