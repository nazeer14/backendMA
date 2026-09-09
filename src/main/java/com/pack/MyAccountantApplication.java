package com.pack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication@EnableSpringDataWebSupport(
		pageSerializationMode =
				EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO
)
public class MyAccountantApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyAccountantApplication.class, args);
	}

}
