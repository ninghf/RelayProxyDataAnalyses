package com.butel.project.relay.dao;

import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/6/28
 * @description TODO
 */
public interface IBaseDao {

    void save();
    void update();
    void delete();
    <T> List<T> find(Query query, Class<T> entityClass, String collectionName);
    void find(int skip, int limit);
}
