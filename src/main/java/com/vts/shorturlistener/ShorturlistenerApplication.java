package com.vts.shorturlistener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

//@ComponentScan(basePackages = { "com.vts.rcs", "com.vts.shorturlistener" })
@SpringBootApplication(scanBasePackages = { "com.vts.rcs", "com.vts.shorturlistener" })
public class ShorturlistenerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShorturlistenerApplication.class, args);
	}
}