package com.butel.project.relay;

import com.butel.project.relay.service.IAnalysesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RelayApplicationTests {

    @Autowired
    private IAnalysesService analysesService;

	@Test
	public void contextLoads() {
	}

}
