package com.butel.project.relay.service;

import com.butel.project.relay.analyses.AnalysesData;
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

    MeetingAnalysesData generateAnalysesData(long key, long startTime, long endTime, int transTime, String superSocketId);
    MeetingOriginalData generateOriginalData(long startTime, long endTime, String superSocketId);
    void generateOriginalData(String meetingId);
}
