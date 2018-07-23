package com.butel.project.relay.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/7/17
 * @description TODO
 */
@Slf4j
@ToString
@Getter
@Setter
public class Item {

    private String stat;
    private long sender;
    private long recver;

    public Item(String stat, long sender, long recver) {
        this.stat = stat;
        this.sender = sender;
        this.recver = recver;
    }
}
