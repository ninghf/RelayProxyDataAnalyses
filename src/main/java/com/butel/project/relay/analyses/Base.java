package com.butel.project.relay.analyses;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/7/16
 * @description TODO
 */
@Slf4j
@Getter
@Setter
public class Base {

    protected int yAxisSend;
    protected int yAxisRecv;

    protected long timestampSend;
    protected long timestampRecv;
    protected int idxSend;
    protected int idxRecv;

    // 数值有效性校验
    protected boolean isValid(long value) {
        if (value != -1)
            return true;
        return false;
    }
}
