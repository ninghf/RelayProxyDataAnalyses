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
    private Map<Long, List<String>> zoom;

    public void toSummary(AnalysesData analysesData) {
        if (Objects.nonNull(analysesData.getTransTimeDistributionDetail()))
            zoom = analysesData.getTransTimeDistributionDetail();
        if (Objects.isNull(tables))
            tables = new LinkedList <>();
        Table table = new Table();
        List<Item> items = new LinkedList <>();
        items.add(new Item("端到端").builder(
                analysesData.getSendTotal(), analysesData.getRecvTotal(),
                analysesData.getNonRepeatSendTotal(), analysesData.getNonRepeatRecvTotal(),
                analysesData.getLossRate(), analysesData.getFecLossRate(), analysesData.getFecRate(),
                analysesData.getRepeatSpendRate(), analysesData.getRepeatWasteRate()));
        table.setItems(items);
        tables.add(table);

        if (Objects.isNull(options))
            options = new LinkedList <>();
        // 数据包落点追踪
        options.add(Option.createOption(analysesData.getPacketList(), analysesData.getAgents()));
        options.add(Option.createOption("端到端延时分布", analysesData.getTransTimeDistribution()));
        options.add(Option.createOption("首次延时分布", analysesData.getOnceTransTimeDistribution()));
        options.add(Option.createOption("重复延时分布", analysesData.getRepeatTransTimeDistribution()));
        options.add(Option.createOption("成功发送次数分布", analysesData.getSendSuccessDistribution()));
        options.add(Option.createOption("失败发送次数分布", analysesData.getSendFailureDistribution()));
        options.add(Option.createOption("重复成功发送次数分布", analysesData.getRepeatSuccessDistribution()));
        options.add(Option.createOption("重复失败发送次数分布", analysesData.getRepeatFailureDistribution()));
        List <AnalysesData.Agent> agents = analysesData.getAgents();
        if (Objects.nonNull(agents)) {
            int agentIdx = 1;
            for (int i = 0; i < agents.size(); i++) {
                AnalysesData.Agent agent = agents.get(i);
                items.add(new Item("Client->Agent" + agentIdx).builder(agent.getSendToAgentCount(), agent.getRecvFromClientCount(), agent.getPathId()));
                items.add(new Item("Agent" + agentIdx + "中转").builder(agent.getRecvFromClientCount(), agent.getSendToRelayCount(), agent.getPathId()));
                items.add(new Item("Agent" + agentIdx + "->Relay").builder(agent.getSendToRelayCount(), agent.getRecvFromAgentCount(), agent.getPathId()));
                options.add(Option.createOption("Agent" + agentIdx + "延时分布", "pathId:" + agent.getPathId(), agent.getTransTimeOnAgent()));
                agentIdx++;
            }
        }
    }
}
