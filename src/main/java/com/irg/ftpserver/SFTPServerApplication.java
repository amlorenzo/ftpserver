package com.irg.ftpserver;

import com.irg.ftpserver.service.SFTPConfigurationService;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.DependsOn;

import java.util.TimeZone;

@SpringBootApplication
@AllArgsConstructor
public class SFTPServerApplication {

	private static final Logger logger = LoggerFactory.getLogger(SFTPServerApplication.class);

	private final SFTPConfigurationService sftpConfigurationService;
	public static void main(String[] args) {SpringApplication.run(SFTPServerApplication.class, args);}

	@PostConstruct
	@DependsOn({"SFTPInitialConfigService"})
	public void initTimeZone() {
		String timeZone = sftpConfigurationService.getLatestConfiguration().getTimeZone();
		TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
		logger.info("Timezone set to: " + timeZone);
	}

}
