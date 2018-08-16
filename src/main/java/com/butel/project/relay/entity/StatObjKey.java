package com.butel.project.relay.entity;

import com.butel.project.relay.constant.StatDataType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/8/12
 * @description TODO
 */
@Slf4j
@Getter
@Setter
public class StatObjKey implements Cloneable {

    private ObjKey self;
    private List<ObjKey> associates;
    private int statType;

    public void self(String objId, int objType) {
        self = new ObjKey(objId, objType);
    }

    public void addAssociate(String objId, int objType) {
        if (Objects.isNull(associates))
            associates = new LinkedList<>();
        associates.add(new ObjKey(objId, objType));
    }

    public List<StatObjKey> getStatObjKeys(StatDataType... types) {
        List<StatObjKey> statObjKeys = new LinkedList<>();
        if (types.length == 0)
            return statObjKeys;
        for (int i = 0; i < types.length; i++) {
            try {
                StatDataType type = types[i];
                this.setStatType(type.getType());
                StatObjKey clone = (StatObjKey)this.clone();
                statObjKeys.add(clone);
            } catch (Exception e) {
                log.error("Copy is error!", e);
            }

        }
        return statObjKeys;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Data
    @AllArgsConstructor
    public class ObjKey {
        String objId;
        int objType;
    }
}
