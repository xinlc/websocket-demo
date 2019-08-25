package com.leo.websocket;

import com.leo.websocket.controller.WebSocketController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 *
 * @author leo
 * @date 2019/8/22
 */
@SpringBootApplication
public class JavaWebSocketServerApplication {

	public static void main(String[] args) {
///		SpringApplication.run(JavaWebSocketServerApplication.class, args);

		SpringApplication springApplication = new SpringApplication(JavaWebSocketServerApplication.class);

		ConfigurableApplicationContext configurableApplicationContext = springApplication.run(args);

		// 解决WebSocket不能注入的问题
		WebSocketController.setApplicationContext(configurableApplicationContext);
	}

}
