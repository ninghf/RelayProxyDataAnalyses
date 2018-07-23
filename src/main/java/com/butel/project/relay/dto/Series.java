package com.butel.project.relay.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
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
public class Series {

    private final String type;
    private List<Data> data;

    public Series(String type) {
        this.type = type;
    }

    public void addPieData(int value, String name) {
        if (!type.equals("pie"))
            throw new IllegalArgumentException("this is pie");
        if (Objects.isNull(data))
            data = new LinkedList <>();
        data.add(new PieData(value, name));
    }

    public void addLineData(int x, int y) {
        if (!type.equals("line"))
            throw new IllegalArgumentException("this is line");
        if (Objects.isNull(data))
            data = new LinkedList <>();
        data.add(new LineData(x, y));
    }

    @Getter
    @Setter
    class Data {

    }

    @Getter
    @Setter
    class PieData extends Data {
        int value;
        String name;
        public PieData(int value, String name) {
            this.value = value;
            this.name = name;
        }
    }

    @Getter
    @Setter
    @JSONType(serialzeFeatures=SerializerFeature.BeanToArray, parseFeatures= Feature.SupportArrayToBean)
    class LineData extends Data {
        int x;
        int y;

        public LineData(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static void main(String[] args) {
        Series series = new Series("line");
        for (int i = 0; i < 5; i++) {
            series.addLineData(i, i+1);
        }

        log.debug("{}", JSON.toJSONString(series));
    }
}
