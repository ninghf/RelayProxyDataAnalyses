package com.butel.project.relay.dto;

import com.butel.project.relay.analyses.AnalysesData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
public class Summary extends BaseRespDto {

    private List<Table> tables;
    private List<Option> options;

    public void toSummary(AnalysesData analysesData) {
        if (Objects.isNull(tables))
            tables = new LinkedList <>();
        Table table = new Table();
        List<Item> items = new LinkedList <>();
        items.add(new Item("理论", analysesData.getPacketTotal(), analysesData.getPacketTotal()));
        items.add(new Item("实际", analysesData.getSenderTotal(), analysesData.getRecverTotal()));
        items.add(new Item("原始丢包", analysesData.getLossSenderTotal(), analysesData.getLossRecverTotal()));
        items.add(new Item("纠错后丢包", analysesData.getFecLossSenderTotal(), analysesData.getFecLossRecverTotal()));
        items.add(new Item("重复包", analysesData.getRepeatSendPacketTotal(), analysesData.getRepeatRecvPacketTotal()));
        table.setItems(items);
        tables.add(table);

        if (Objects.isNull(options))
            options = new LinkedList <>();
        options.add(Option.createOption("延时分布", analysesData.getTransTimeDistribution()));
        options.add(Option.createOption("首次延时分布", analysesData.getOnceTransTimeDistribution()));
        options.add(Option.createOption("重复延时分布", analysesData.getRepeatTransTimeDistribution()));
        options.add(Option.createOption("成功发送次数分布", analysesData.getRepeatSuccessDistribution()));
        options.add(Option.createOption("失败发送次数分布", analysesData.getRepeatFailureDistribution()));
    }
}
