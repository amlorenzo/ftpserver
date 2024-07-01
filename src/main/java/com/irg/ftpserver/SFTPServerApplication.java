package com.irg.ftpserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;



@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SFTPServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SFTPServerApplication.class, args);
	}

}
