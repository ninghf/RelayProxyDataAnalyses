package com.butel.project.relay.job;

import com.butel.project.relay.constant.StatObjType;
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

    private static final long slice = 30 * 1000;// 3 分钟一个切片
    private static final String pattern = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    private IMeetingStatDataService service;

    public List<Job> dispatcher(long startTime, long endTime, int transTime, StatObjType objType, String objId) {
        List<Job> jobs = new LinkedList<>();
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
            jobs.add(new Job(start, end, transTime, objType, objId, service));
        }
        jobs.forEach(Job::doTask);
        return jobs;
    }

    public static void main(String[] args) {
        long startTime = 1534843172000L;
        long endTime = 1534843595000L;
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

        }
    }

}
