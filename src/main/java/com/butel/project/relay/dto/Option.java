package com.butel.project.relay.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
public class Option {

    private String title;
    private List<Series> series;

    public static Option createOption(String title, Map <Long, Long> distribution) {
        if (Objects.isNull(distribution))
            return null;
        Option option = new Option();
        option.setTitle(title);
        List<Series> seriesList = new LinkedList<>();
        option.setSeries(seriesList);
        Series series = new Series("pie");
        seriesList.add(series);
        distribution.entrySet().stream()
                .forEach(entry -> series.addPieData(entry.getValue().intValue(), entry.getKey().toString()));
        return option;
    }
}
