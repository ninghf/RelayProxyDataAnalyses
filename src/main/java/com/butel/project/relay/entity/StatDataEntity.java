package com.butel.project.relay.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/6/28
 * @description TODO
 */
@Getter
@Setter
@ToString
public class StatDataEntity {

    private ObjectId _id;
    private int ownerId;
    private int ownerType;
    private String objID;
    private int objType;
    private int statType;
    private Binary itemValue;
    private long timestamp;
}
