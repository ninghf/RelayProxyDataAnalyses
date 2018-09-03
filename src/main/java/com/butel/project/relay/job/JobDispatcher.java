package com.butel.project.relay.job;

import com.butel.project.relay.constant.StatObjType;
import com.butel.project.relay.meeting.MeetingOriginalData;
import com.butel.project.relay.service.IMeetingStatDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
public class JobDispatcher {

    private static final int bound = 60 * 1000;// 边界
    private static final long slice30 = 30 * 1000;// 30s一个切片
    private static final long slice600 = 10 * 60 * 1000;// 10 分钟一个切片 10*60=600s
    private static final String pattern = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    private IMeetingStatDataService service;

    /**
     * 分配规则：
     * 1、原始数据：10min一个颗粒度（预估：最小单元 1/s 10分钟=10*60*1/s=600/s）
     * 2、前端页面显示：30s一个颗粒度
     * @param startTime
     * @param endTime
     * @param transTime
     * @param objType
     * @param objId
     * @return
     */
    public List<Job> dispatcher(long startTime, long endTime, int transTime, StatObjType objType, String objId) {
        // 原始数据任务
        List<Long[]> sliceList_originalData = slice(JobDispatcher.slice600, startTime, endTime);
        MeetingOriginalData meetingOriginalData = sliceList_originalData.parallelStream()
                .collect(MeetingOriginalData::new,
                        (originalData, sli) -> service.decode(originalData, sli[0], sli[1], bound, objType, objId),
                        (originalData, originalData2) -> originalData.merge(originalData2));
        meetingOriginalData.setStartTime(startTime);
        meetingOriginalData.setEndTime(endTime);
        // 前端页面任务
        List<Long[]> sliceList = slice(JobDispatcher.slice600, startTime, endTime);
        LinkedList<Job> jobs = sliceList.parallelStream().collect(
                        LinkedList::new,
                        (list, sli) -> {
                            Job job = new Job(sli[0], sli[1], transTime, objType, objId, meetingOriginalData);
                            job.generateAnalysesData();
                            list.add(job);
                        },
                        (list, list2) -> list.addAll(list2));
        return jobs;
    }

    /**
     * 切分时间：
     * @param slice 颗粒度
     * @param startTime
     * @param endTime
     * @return arr[0]=start arr[1]=end
     */
    public List<Long[]> slice(long slice, long startTime, long endTime) {
        if (endTime < startTime)
            throw new IllegalArgumentException();
        List<Long[]> slices = new LinkedList<>();
        long total = endTime - startTime;
        long total_slices = 0;
        if (total % slice == 0) {
            total_slices = total / slice;
        } else {
            total_slices = total / slice + 1;
        }
        log.debug("时间段【{}】-【{}】准备切分【{}】段", DateFormatUtils.format(startTime, pattern), DateFormatUtils.format(endTime, pattern), total_slices);
        for (int i = 0; i < total_slices; i++) {
            long start = startTime + i * slice;
            long end = start + slice;
            if (end > endTime)
                end = endTime;
            log.debug("第【{}】段【{}】-【{}】", i + 1, DateFormatUtils.format(start, pattern), DateFormatUtils.format(end, pattern));
            Long[] sli = new Long[2];
            sli[0] = start;
            sli[1] = end;
            slices.add(sli);
        }
        return slices;
    }

    public static void main(String[] args) {
        long startTime = 1534843172000L;
        long endTime = 1534843595000L;
        new JobDispatcher().slice(slice30, startTime, endTime);
    }

}
