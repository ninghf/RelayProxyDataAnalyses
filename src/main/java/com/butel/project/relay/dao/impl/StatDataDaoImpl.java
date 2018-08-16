package com.butel.project.relay.dao.impl;

import com.butel.project.relay.constant.StatDataType;
import com.butel.project.relay.constant.StatObjType;
import com.butel.project.relay.dao.IStatDataDao;
import com.butel.project.relay.entity.StatDataEntity;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/6/27
 * @description TODO
 */
@Repository
public class StatDataDaoImpl extends BaseDaoImpl implements IStatDataDao {

    public static final String collectionName = "stat_data";


    @Override
    public List <StatDataEntity> queryStatData(long startTime, long endTime, StatObjType statObjType, String... objIds) {
        Query query = new Query();
        query.addCriteria(Criteria.where("timestamp").gte(startTime).lte(endTime));
        query.addCriteria(Criteria.where("statObjKey.self.objId").in(objIds));
        query.addCriteria(Criteria.where("statObjKey.self.objType").is(statObjType.getType()));
        query.addCriteria(Criteria.where("statObjKey.statType").in(
                StatDataType.super_socket_send.getType(), StatDataType.super_socket_recv.getType(),
                StatDataType.super_socket_send_repeat.getType(), StatDataType.super_socket_recv_repeat.getType()));
        return find(query, StatDataEntity.class, collectionName);
    }
}
