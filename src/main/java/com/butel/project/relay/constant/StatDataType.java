package com.butel.project.relay.constant;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/6/28
 * @description TODO
 */
public enum StatDataType {
    nul(0), super_socket_send(121), super_socket_recv(122);

    private int type;

    StatDataType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
