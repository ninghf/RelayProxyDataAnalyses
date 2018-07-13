package com.butel.project.relay.dao.impl;

import com.butel.project.relay.constant.StatDataType;
import com.butel.project.relay.constant.StatObjType;
import com.butel.project.relay.dao.IStatDataDao;
import com.butel.project.relay.entity.StatDataEntity;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;

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
    public List<StatDataEntity> queryStatData(long startTime, long endTime, String superSocketID, StatObjType statObjType, StatDataType statDataType) {
        Query query = new Query();
        query.addCriteria(Criteria.where("timestamp").gte(startTime).lte(endTime));
        if (!StringUtils.isEmpty(superSocketID))
            query.addCriteria(Criteria.where("objID").is(superSocketID));
        query.addCriteria(Criteria.where("objType").is(statObjType.getType()));
        query.addCriteria(Criteria.where("statType").is(statDataType.getType()));
        return find(query, StatDataEntity.class, collectionName);
    }

    @Override
    public List <StatDataEntity> queryStatDataByPaths(long startTime, long endTime, Set<String> paths,
                                                      StatObjType statObjType, StatDataType statDataType) {
        Query query = new Query();
        query.addCriteria(Criteria.where("timestamp").gte(startTime).lte(endTime));
        query.addCriteria(Criteria.where("objID").in(paths));
        query.addCriteria(Criteria.where("objType").is(statObjType.getType()));
        query.addCriteria(Criteria.where("statType").is(statDataType.getType()));
        return find(query, StatDataEntity.class, collectionName);
    }
}
