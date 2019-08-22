package com.leo.websocket.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * web socket util
 * 处理了心跳包
 *
 * @author leo
 * @date 2019/8/22
 */
@Slf4j
@ServerEndpoint(value = "/websocket2")
@Component
public class WebSocketUtil2 {
	private final String PING_MSG = "heartbeat";
	private final String PONG_MSG = "pong";

	private static Map<String, Session> userSessionMap = new ConcurrentHashMap<String, Session>();

	/**
	 * 连接建立成功调用的方法
	 *
	 * @param session
	 */
	@OnOpen
	public void onOpen(Session session) {
		log.info("有新连接加入！");
	}

	/**
	 * 连接关闭调用的方法
	 */
	@OnClose
	public void onClose(Session session) {
		log.info("有一连接关闭！" + session.getId());
		userSessionMap.remove(session.getId());
	}

	/**
	 * 收到客户端消息后调用的方法
	 *
	 * @param message
	 * @param session
	 */
	@OnMessage
	public void onMessage(String message, Session session) throws IOException {
		log.info("来自客户端的消息:" + message);
		log.info("来自客户端的消息id:" + session.getId());
		userSessionMap.put(session.getId(), session);

		// 处理心跳包
		if (PING_MSG.equals(message)) {
			WebSocketUtil2.sendMessage(session.getId(), PONG_MSG);
		}
	}

	/**
	 * 发生错误时调用
	 *
	 * @param session
	 * @param error
	 */
	@OnError
	public void onError(Session session, Throwable error) throws IOException {
		log.error("onError error", error);
//		session.close();
	}

	/**
	 * 发送消息
	 *
	 * @param message
	 * @throws IOException
	 */
	public static void sendMessage(String user, String message) throws IOException {
		if (userSessionMap.containsKey(user)) {
			userSessionMap.get(user).getBasicRemote().sendText(message);
		}
	}

	/**
	 * 群发消息
	 *
	 * @param message
	 * @throws IOException
	 */
	public static void sendMessage(String message) throws IOException {
		for (String user : userSessionMap.keySet()) {
			try {
				userSessionMap.get(user).getBasicRemote().sendText(message);
			} catch (IOException e) {
				continue;
			}
		}
	}
}
