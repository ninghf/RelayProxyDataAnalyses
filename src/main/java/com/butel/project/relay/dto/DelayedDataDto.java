package com.butel.project.relay.dto;

import com.butel.project.relay.analyses.Axis;
import com.butel.project.relay.analyses.Packet;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

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
public class DelayedDataDto extends BaseRespDto {

    private EchartsOption<?> echartsOption;
    private HashMap<String, Integer> axis;

    public static DelayedDataDto createInstance(List <Packet> vPackets, Axis axis, boolean isDetail, int limit, int currentPage) {
        DelayedDataDto delayedDataDto = new DelayedDataDto();
        EchartsOption<long[]> option = new EchartsOption<>();
        List<DataSet<long[]>> dataSet = new ArrayList <>();
        if (Objects.nonNull(vPackets) && !vPackets.isEmpty()) {
            Collections.sort(vPackets);
            createDataSet(vPackets, dataSet, isDetail, limit, currentPage, axis);
            delayedDataDto.setTotal(vPackets.size());
        } else {
            delayedDataDto.setTotal(0);
        }
        option.setDataset(dataSet);
//        List<List<String>> items = new LinkedList <>();
//        if (Objects.nonNull(vPackets) && !vPackets.isEmpty()) {
//            createData(vPackets, items);
//        }
//        option.setData(items);
        delayedDataDto.setEchartsOption(option);
        delayedDataDto.setAxis(axis.getAxis());
        return delayedDataDto;
    }

    public static void createData(List <Packet> vPackets, List<List<String>> items) {
        Collections.sort(vPackets);
        for (int i = 0; i < vPackets.size(); i++) {
            Packet packet = vPackets.get(i);
            List<String> item = new LinkedList <>();
            item.add(Long.toString(packet.getPacketID()));
            item.add(Long.toString(packet.getTransTime()));
            items.add(item);
        }
    }

    public static void createEncode(EchartsOption<Packet> option) {
        // Encode
        Encode encode = new Encode();
        List<String> x = new ArrayList <>();
        x.add("sendTime");
        x.add("recvTime");
        List<String> y = new ArrayList <>();
        y.add("sendSuperSocket");
        y.add("recvSuperSocket");
        encode.setX(x);
        encode.setY(y);
        option.setEncode(encode);
    }

    public static void createLegend(EchartsOption<Packet> option) {
        List<String> legend = new ArrayList <>();
        legend.add("sendSuperSocket");
        legend.add("recvSuperSocket");
        option.setLegend(legend);
    }

    public static void createDataSet(List <Packet> vPackets, List <DataSet <long[]>> dataSet, boolean isDetail, int limit, int currentPage, Axis axis) {
        int skip = currentPage == 0 ? 0 : (currentPage - 1) * limit;
        for (int i = skip; i < (vPackets.size() - limit > 0 ? skip + limit : vPackets.size()); i++) {
            Packet packet = vPackets.get(i);
            DataSet<long[]> series = new DataSet <>();
            List<long[]> source = packet.toLongArray(isDetail, axis);
            series.setSource(source);
            dataSet.add(series);
        }
    }
}
