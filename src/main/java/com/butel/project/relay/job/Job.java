package com.butel.project.relay.job;

import com.butel.project.relay.constant.StatObjType;
import com.butel.project.relay.meeting.MeetingAnalysesData;
import com.butel.project.relay.meeting.MeetingOriginalData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@Data
public class Job {

    private long startTime;
    private long endTime;
    private int transTime;
    private StatObjType objType;
    private String objId;

    private MeetingAnalysesData analysesData;
    private MeetingOriginalData originalData;

    public Job(long startTime, long endTime, int transTime, StatObjType objType, String objId, MeetingOriginalData originalData) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.transTime = transTime;
        this.objType = objType;
        this.objId = objId;
        this.originalData = originalData;
        analysesData = new MeetingAnalysesData();
    }

    public void generateAnalysesData() {
        if (Objects.isNull(originalData))
            return;
        analysesData.processOriginalData(originalData, transTime, startTime, endTime);
    }
}
