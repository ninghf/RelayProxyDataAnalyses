package com.butel.project.relay.entity;

import com.butel.project.relay.constant.StatDataType;
import com.butel.project.relay.protobuf.SDNMessage.ProtoStatItemValue;
import com.butel.project.relay.protobuf.SDNMessage.ProtoStatItemValue.ProtoDataValue;
import com.butel.project.relay.protobuf.SDNMessage.ProtoStatItemValue.ProtoDataValue.ProtoBitmapValue;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/6/28
 * @description TODO
 */
@Slf4j
@Getter
@Setter
@Document(collection = "stat_data")
public class StatDataEntity {

    private ObjectId _id;
    private StatObjKey statObjKey;
    private int ownerId;
    private int ownerType;
    private Binary itemValue;
    private long timestamp;
    private Time time;

    /**
     * message ProtoStatItemValue {

     message ProtoDataValue {

     message ProtoBitmapValue {
     required uint32 index = 1;      //4 字节全局索引，bitmap 采用定长结构，目前定义 64 个包为一个单元，索引号直接可标识包 ID 的范围
     required uint64 bit_map = 2;    //8 字节：二进制位标识，1 代表数据包已接收，0 代表数据包未接收
     }

     required uint32 index = 1;                      //统计周期内索引
     required uint32 sum = 2;                        //统计个数
     required uint32 size = 3;                       //统计大小，单位 KByte
     repeated ProtoBitmapValue bitmap_values = 4;    //包 ID bitmap 记录
     }

     message ProtoStatObjKey {

     message ProtoStatObj {
     required string obj_id = 1;     //统计对象 ID
     required uint32 obj_type = 2;   //统计对象类型
     }

     required ProtoStatObj self = 1;         //统计对象
     repeated ProtoStatObj associates = 2;   //关联的统计对象
     required uint32 stat_type = 3;          //统计类型
     }

     required ProtoStatObjKey obj_key = 1;
     repeated ProtoDataValue  data_values = 2;
     required uint32 stat_interval = 3;          //统计周期，单位 s
     required uint32 sub_stat_interval = 4;      //分片统计周期，单位 ms
     required uint64 timestamp = 5;              //统计的起始时间（在统计时写入，未经校正的客户端时间）
     }
     */
    @Transient
    private static final int unit = 64;
    @Transient
    private static final long bit = 1;
    @Transient
    private HashMap<Long, LinkedList<Long>> packetIds;

    public void parseStatData() {
        byte[] data = itemValue.getData();
        if (data.length == 0) return;
        try {
            ProtoStatItemValue statItemValue = ProtoStatItemValue.parseFrom(data);
            if (Objects.nonNull(statItemValue)) {
                final int subStatInterval = statItemValue.getSubStatInterval();
                final long tickTimestamp = statItemValue.getTimestamp();
//                final long adjustTime = time.adjust();
                // 统计小周期
                List<ProtoDataValue> dataValuesList = statItemValue.getDataValuesList();
                if (Objects.nonNull(dataValuesList) && !dataValuesList.isEmpty()) {
                    packetIds = dataValuesList.parallelStream()
                            .filter(dataValue -> Objects.nonNull(dataValue))
                            .collect(HashMap::new, (hashMap, dataValue) -> {
                                long time = tickTimestamp + subStatInterval * dataValue.getIndex();
//                                long time = adjustTime + subStatInterval * dataValue.getIndex();
                                // 统计小周期--数据包列表
                                List<ProtoBitmapValue> bitmapValuesList = dataValue.getBitmapValuesList();
                                if (Objects.nonNull(bitmapValuesList) && !bitmapValuesList.isEmpty()) {
                                    LinkedList<Long> packetIds = bitmapValuesList.parallelStream()
                                            .collect(LinkedList::new, (list, bitmapValue) -> {
                                                long index = bitmapValue.getIndex();
                                                long bitMap = bitmapValue.getBitMap();
                                                for (int j = 0; j < unit; j++) {
                                                    if ((bitMap & (bit << j)) != 0) {
                                                        long packetId = index * unit + j;
                                                        list.add(packetId);
                                                    }
                                                }
                                            }, (list1, list2) -> list1.addAll(list2));
                                    hashMap.put(time, packetIds);
                                }
                            }, (hashMap, hashMap2) -> hashMap.putAll(hashMap2));
                }
            }
        } catch (InvalidProtocolBufferException e) {
            log.error("解析【{}】【统计数据包】异常：", this, e);
        }
    }

    public boolean comparedWithStatDataType(StatDataType type) {
        return statObjKey.getStatType() == type.getType();
    }

}
