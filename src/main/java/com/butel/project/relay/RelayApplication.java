package com.butel.project.relay;

import com.butel.project.relay.filter.GzipFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableCaching
public class RelayApplication {

	public static void main(String[] args) {
		SpringApplication.run(RelayApplication.class, args);
	}

	@Bean
	public GzipFilter gzipFilter() {
        return new GzipFilter();
    }
}
