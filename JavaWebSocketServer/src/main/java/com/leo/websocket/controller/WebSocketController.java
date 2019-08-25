package com.leo.websocket.controller;

import com.leo.websocket.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * spring 或 springboot 的 websocket 里面使用 @Autowired 注入 service 或 bean 时，
 * 报空指针异常，service 为 null（并不是不能被注入）。
 *
 * 本质原因：spring管理的都是单例（singleton），和 websocket （多对象）相冲突。
 * 详细解释：项目启动时初始化，会初始化 websocket （非用户连接的），spring 同时会为其注入 service,
 * 该对象的 service 不是 null，被成功注入。但是，由于 spring 默认管理的是单例，所以只会注入一次 service。
 * 当新用户进入聊天时，系统又会创建一个新的 websocket 对象，这时矛盾出现了：spring 管理的都是单例，
 * 不会给第二个 websocket 对象注入 service，所以导致只要是用户连接创建的 websocket 对象，都不能再注入了。
 *
 * 像 controller 里面有 service， service 里面有 dao。因为 controller，service ，dao 都有是单例，所以注入时不会报 null。
 * 但是 websocket 不是单例，所以使用spring注入一次后，后面的对象就不会再注入了，会报null。
 *
 */

/**
 * 解决方法：
 * 1. 将要注入的 service 改成 static，就不会为null了。
 * 2. 使用 applicationContext.getBean(WebSocketService.class); 获取service实例
 * p.s.当前类使用的第二种解决方案
 * */
//@Controller
//@ServerEndpoint(value="/chatSocket")
//public class ChatSocket {
//	//  这里使用静态，让 service 属于类
//	private static ChatService chatService;
//
//	// 注入的时候，给类的 service 注入
//	@Autowired
//	public void setChatService(ChatService chatService) {
//		ChatSocket.chatService = chatService;
//	}
//}


/**
 *
 * @author leo
 * @date: 2019/08/25
 */
@Slf4j
@ServerEndpoint(value = "/websocket/{userId}")
@Component
public class WebSocketController {

	/**
	 * 跟前端约定心跳包信息
	 */
	private final String PING_MSG = "heartbeat";
	private final String PONG_MSG = "pong";

	/**
	 * concurrent包的线程安全Map，用来存放每个客户端对应的MyWebSocket对象。
	 */
	private static Map<String, WebSocketController> connections = new ConcurrentHashMap<>();


	/**
	 * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
	 */
	private static int onlineUser;

	/**
	 * 解决无法注入的
	 */
	private static ApplicationContext applicationContext;

	/**
	 * service
	 */
	private WebSocketService webSocketService;

	/**
	 * 与某个客户端的连接会话，需要通过它来给客户端发送数据
	 */
	private Session session;

	/**
	 * 当前用户id
	 */
	private String userId;

	/**
	 * 连接建立成功调用的方法
	 */
	@OnOpen
	public void onOpen(@PathParam("userId") String userId, Session session) {
		onlineUser++;
		this.session = session;
		this.userId = userId;

		// 注入 service
		webSocketService = applicationContext.getBean(WebSocketService.class);

		// 把自己的信息加入map
		connections.put(userId, this);
		log.info("有新连接加入！当前在线人数为：" + onlineUser);

//		webSocketService.xxx
//		sendMessage(userId, "hello");
	}

	/**
	 * 接收客户端发送的消息
	 *
	 * @param userId  发送消息的ID
	 * @param message 消息
	 * @param session
	 */
	@OnMessage
	public void OnMessage(@PathParam("userId") String userId, String message, Session session) {
		log.info("userId" + userId + " -> " +  message);

		// 处理心跳包信息
		if (PING_MSG.equals(message)) {
			sendMessage(userId, PONG_MSG);
		} else {
			// 处理其他信息
			// webSocketService.xxx
			sendMessage(userId, "Hi");
		}
	}

	/**
	 * 向客户端发送消息
	 *
	 * @param userId  当前用户ID
	 * @param message 消息内容
	 */
	public void sendMessage(String userId, String message) {
		try {
			if (connections.containsKey(userId)) {
				this.session.getBasicRemote().sendText(message);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 向所有在线用户通知
	 */
	public void sendMessageAll(String message) {
		try {
			for (WebSocketController item : connections.values()) {
				/// item.session.getAsyncRemote().sendText(message);
				item.session.getBasicRemote().sendText(message);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 用户关闭连接
	 *
	 * @param userId 移除当前ID的session
	 */
	@OnClose
	public void onClose(@PathParam("userId") String userId) {
		onlineUser--;
		log.info(userId + " 已掉线！");
		connections.remove(userId);
	}

	/**
	 * 发送错误！！！
	 *
	 * @param userId
	 * @param session
	 * @param error   移除当前ID的session
	 */
	@OnError
	public void onError(@PathParam("userId") String userId, Session session, Throwable error) {
		log.info("webSocket error -> " + userId);
		connections.remove(userId);
		error.printStackTrace();
	}

	public static synchronized int getOnlineUser() {
		return onlineUser;
	}

	public static void setApplicationContext(ApplicationContext context) {
		applicationContext = context;
	}

	/**
	 * 内部类可以做心跳, 建议在前端做心跳
	 */
	class MyTask extends TimerTask {
		private String message;

		public MyTask(String m) {
			this.message = m;
		}

		@Override
		public void run() {
            for (WebSocketController item: connections.values()) {
                item.sendMessage(item.userId, message);
            }
		}
	}

}

