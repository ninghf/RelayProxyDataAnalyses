package com.butel.project.relay.job;

import com.butel.project.relay.constant.StatObjType;
import com.butel.project.relay.meeting.MeetingAnalysesData;
import com.butel.project.relay.meeting.MeetingOriginalData;
import com.butel.project.relay.service.IMeetingStatDataService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@Data
public class Job {

    public static final int bound = 1 * 60 * 1000;// 边界

    private long startTime;
    private long endTime;
    private int transTime;
    private StatObjType objType;
    private String objId;

    private IMeetingStatDataService service;
    private MeetingAnalysesData analysesData;
    private MeetingOriginalData originalData;

    public Job(long startTime, long endTime, int transTime, StatObjType objType, String objId, IMeetingStatDataService service) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.transTime = transTime;
        this.objType = objType;
        this.objId = objId;
        this.service = service;
        originalData = new MeetingOriginalData(startTime, endTime);
        analysesData = new MeetingAnalysesData();
    }

    public void doTask() {
        generateOriginalData();
        generateAnalysesData();
    }

    public void generateAnalysesData() {
        if (Objects.isNull(originalData))
            return;
        analysesData.processOriginalData(originalData, transTime);
    }

    public void generateOriginalData() {
        service.decode(originalData, startTime, endTime, bound, objId);
    }
}
