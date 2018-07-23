package com.butel.project.relay.service.impl;

import com.butel.project.relay.service.IAnalysesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/7/16
 * @description TODO
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AnalysesServiceImplTest {

    @Autowired
    private IAnalysesService service;

    @Test
    public void generateOriginalData() {
//        service.generateOriginalData(1531468719000L, 1531470649000L, "11811295409737206439");
    }

    @Test
    public void generateAnalysesData() {
//        service.generateAnalysesData(1531468719000L, 1531469449000L, "11811295409737206439");
    }
}