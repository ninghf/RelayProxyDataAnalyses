package com.butel.project.relay.repository;

import com.butel.project.relay.entity.StatDataEntity;
import com.butel.project.relay.entity.StatObjKey;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatDataRepository extends MongoRepository<StatDataEntity, StatObjKey> {

    /**
     *
     * @param self 统计对象
     * @param associates 关联对象（用户ID、数据源ID）
     * @param statType 统计类型
     * @return
     */
    @Query(value = "{'statObjKey.self':?0, 'statObjKey.associates':{'$all':?1}, 'statObjKey.statType':{'$in':?2}}")
    List<StatDataEntity> findAllByStatObjKey_SelfAndStatObjKey_AssociatesAndStatObjKey_StatTypeIn
            (StatObjKey.ObjKey self, Iterable<StatObjKey.ObjKey> associates, Integer... statType);

    /**
     *
     * @param from 时间范围开始值
     * @param to 时间范围结束值
     * @param self 统计对象
     * @param statType 统计类型
     * @return
     */
    List<StatDataEntity> findAllByTimestampBetweenAndStatObjKey_SelfAndStatObjKey_StatTypeIn
            (long from, long to, StatObjKey.ObjKey self, Integer... statType);

    /**
     * 查询关联信息
     * @param from
     * @param to
     * @param selfs
     * @param statType
     * @return
     */
    List<StatDataEntity> findAllByTimestampBetweenAndStatObjKey_SelfInAndStatObjKey_StatTypeIn
            (long from, long to, Iterable<StatObjKey.ObjKey> selfs, Integer... statType);
}
