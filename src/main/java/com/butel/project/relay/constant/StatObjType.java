package com.butel.project.relay.constant;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/6/28
 * @description TODO
 */
public enum StatObjType {
    nul(0), super_socket(121), path(122), pLink(1),
    XBOX_meetingType(210), XBOX_meetingUserId(211), XBOX_meetingSourceId(212);

    private int type;

    StatObjType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
