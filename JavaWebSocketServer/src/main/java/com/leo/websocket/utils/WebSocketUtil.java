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
 *
 * @author leo
 * @date 2019/8/22
 */
@Slf4j
@ServerEndpoint(value = "/websocket")
@Component
public class WebSocketUtil {

	private static Map<String, Session> userSessionMap = new ConcurrentHashMap<String, Session>();
	private static Map<Session, String> sessionUserMap = new ConcurrentHashMap<Session, String>();

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
		log.info("有一连接关闭！");
		userSessionMap.remove(sessionUserMap.get(session));
		sessionUserMap.remove(session);
	}

	/**
	 * 收到客户端消息后调用的方法
	 *
	 * @param message
	 * @param session
	 */
	@OnMessage
	public void onMessage(String message, Session session) {
		log.info("来自客户端的消息:" + message);
		userSessionMap.put(message, session);
		sessionUserMap.put(session, message);
	}

	/**
	 * 发生错误时调用
	 *
	 * @param session
	 * @param error
	 */
	@OnError
	public void onError(Session session, Throwable error) {
		log.error("onError error", error);
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
