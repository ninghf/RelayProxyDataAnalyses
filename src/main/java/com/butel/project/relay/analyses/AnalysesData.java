package com.butel.project.relay.analyses;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/7/12
 * @description TODO
 */
@Slf4j
@Getter
@Setter
public class AnalysesData {

    private long id;
    private Axis axis;
    private List<Packet> packets;

}
