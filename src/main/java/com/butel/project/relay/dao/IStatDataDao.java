package com.butel.project.relay.dao;

import com.butel.project.relay.constant.StatObjType;
import com.butel.project.relay.entity.StatDataEntity;

import java.util.List;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/6/27
 * @description TODO
 */
public interface IStatDataDao extends IBaseDao {

    List<StatDataEntity> queryStatData(long startTime, long endTime, StatObjType statObjType, String... objIds);
}
