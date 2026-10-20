package com.xuyouyu.harness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class HarnessServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HarnessServerApplication.class, args);
	}

}
