package com.butel.project.relay.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/7/2
 * @description Echarts option
 */
@Slf4j
@Getter
@Setter
public class EchartsOption<T> {

    private Encode encode;
    private List<DataSet<T>> dataset;
    private List<List<String>> data;
    private List<String> legend;
}
