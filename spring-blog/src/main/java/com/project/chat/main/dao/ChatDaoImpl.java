package com.project.chat.main.dao;

import java.util.List;
import java.util.Map;

import com.project.chat.ChatMessageVO;
import com.project.chat.RoomVO;

public interface ChatDaoImpl {
	RoomVO selectRoom(RoomVO roomVO);
	int insertRoom(RoomVO roomVO);
	List<ChatMessageVO> chatList(RoomVO roomVO);
	int insertMessage(Map<String, Object> map);
	List<RoomVO> roomList(String user_id);
}
