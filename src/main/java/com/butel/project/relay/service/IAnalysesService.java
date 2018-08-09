package com.butel.project.relay.service;

import com.butel.project.relay.analyses.AnalysesData;
import com.butel.project.relay.analyses.OriginalData;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/6/28
 * @description TODO
 */
public interface IAnalysesService {

    AnalysesData generateAnalysesData(long key, long startTime, long endTime, int transTime, String superSocketId);
    OriginalData generateOriginalData(long startTime, long endTime, String superSocketId);
}
