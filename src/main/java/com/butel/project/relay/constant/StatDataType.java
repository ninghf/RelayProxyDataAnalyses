package com.butel.project.relay.constant;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/6/28
 * @description TODO
 */
public enum StatDataType {
    nul(0), super_socket_send(121), super_socket_recv(122), super_socket_send_repeat(125), super_socket_recv_repeat(123),
    rtt_super_socket_recv(201), rtt_super_socket_recv_delay(202),
    XBOX_send(210), XBOX_recv(212), XBOX_send_fail(211);
    private int type;

    StatDataType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
