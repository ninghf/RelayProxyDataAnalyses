package com.butel.project.relay.dto;

import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
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
public class Series {

    private final String type;
    private String color = "#4B45C0";
    private List<Data> data;

    public Series(String type) {
        this.type = type;
    }

    public void addPieData(int value, String name) {
        if (!type.equals("pie"))
            throw new IllegalArgumentException("this is pie");
        if (Objects.isNull(data))
            data = new LinkedList <>();
        data.add(new PieData(value, name, null));
    }

    public void addLineData(int x, int y, Map<String, String> extras) {
        if (!type.equals("line"))
            throw new IllegalArgumentException("this is line");
        if (extras.get("repeat").equals("true"))
            color = "#FF0000";
        if (Objects.isNull(data))
            data = new LinkedList <>();
        data.add(new LineData(x, y, extras));
    }

    @Getter
    @Setter
    class Data {
        protected Map<String, String> extras;

        public Data(Map <String, String> extras) {
            this.extras = extras;
        }
    }

    @Getter
    @Setter
    class PieData extends Data {
        int y;
        String name;
        public PieData(int value, String name, Map<String, String> extras) {
            super(extras);
            this.y = value;
            this.name = name;
        }
    }

    @Getter
    @Setter
    @JSONType(serialzeFeatures=SerializerFeature.BeanToArray, parseFeatures= Feature.SupportArrayToBean)
    class LineData extends Data {
        int x;
        int y;

        public LineData(int x, int y, Map<String, String> extras) {
            super(extras);
            this.x = x;
            this.y = y;
        }
    }
}
