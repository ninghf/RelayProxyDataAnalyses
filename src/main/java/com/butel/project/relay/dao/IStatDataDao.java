package com.butel.project.relay.dao;

import com.butel.project.relay.constant.StatDataType;
import com.butel.project.relay.constant.StatObjType;
import com.butel.project.relay.entity.StatDataEntity;

import java.util.List;
import java.util.Set;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/6/27
 * @description TODO
 */
public interface IStatDataDao extends IBaseDao {


    List<StatDataEntity> queryStatData(long startTime, long endTime, String superSocketID, StatObjType statObjType, StatDataType statDataType);
    List<StatDataEntity> queryStatDataByPaths(long startTime, long endTime, Set<String> paths, StatObjType statObjType, StatDataType statDataType);
}
