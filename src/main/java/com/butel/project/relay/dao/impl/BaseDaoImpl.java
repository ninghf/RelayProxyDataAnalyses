package com.butel.project.relay.dao.impl;

import com.butel.project.relay.dao.IBaseDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/6/28
 * @description TODO
 */
@Repository
public class BaseDaoImpl implements IBaseDao {

    @Autowired
    private MongoTemplate mongo;

    @Override
    public void save() {

    }

    @Override
    public void update() {

    }

    @Override
    public void delete() {

    }

    @Override
    public <T> List<T> find(Query query, Class <T> entityClass, String collectionName) {
        return mongo.find(query, entityClass, collectionName);
    }

    @Override
    public void find(int skip, int limit) {

    }
}
