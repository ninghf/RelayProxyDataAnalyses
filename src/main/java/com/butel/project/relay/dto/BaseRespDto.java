package com.butel.project.relay.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/7/1
 * @description TODO
 */
@Slf4j
@Getter
@Setter
public class BaseRespDto {

    private long time;
    private int status;
    private int total;
}
