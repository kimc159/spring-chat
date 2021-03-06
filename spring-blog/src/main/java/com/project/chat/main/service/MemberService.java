package com.project.chat.main.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.chat.config.SessionConfig;
import com.project.chat.login.LoginVO;
import com.project.chat.main.dao.MemberDao;
import com.project.chat.member.MemberVO;
import com.project.chat.util.SHA256Util;

@Service
public class MemberService implements MemberServiceImpl{

	@Autowired
	private MemberDao memberDao;
	
	@Override
	public int join(MemberVO member) {
		return memberDao.join(member);
	}
	@Override
	public int updateAuthKey(Map<String, String> map) {
		return memberDao.updateAuthKey(map);
	}
	@Override
	public int login(LoginVO loginVO, HttpServletResponse response, HttpSession session) {
		
		LoginVO mem = memberDao.login(loginVO.getMemId());
		
		if(mem != null) {
			// SHA256 암호화
			String salt = getSaltById(loginVO.getMemId());
			loginVO.setMemPassword(SHA256Util.getEncrypt(loginVO.getMemPassword(), salt));
			
			// 로그인 성공, 비밀번호 일치 여부
			if(loginVO.getMemPassword().equals(mem.getMemPassword())) {
				
				int authStatus = memberAuthStatus(loginVO); 
				
				if(authStatus == 1) {// 이메일 인증 받았을 경우

					// 아이디 중복 체크
					SessionConfig.getSessionidCheck("user_id", loginVO.getMemId().toString());

					Cookie cookie = new Cookie("rememberID", loginVO.getMemId());
					if(loginVO.getRememberId()) {
						cookie.setMaxAge(60*60*24*30);
					} else {
						cookie.setMaxAge(0);
					} 
					cookie.setPath("/");
					response.addCookie(cookie); 
					
					// 사용자 세션 설정				
					session.setAttribute("user_id", loginVO.getMemId());
					session.setAttribute("login", loginVO);
					session.setMaxInactiveInterval(30*60); 
					
					return 1;
				} else { // 이메일 인증 안받았을 경우
					return -2;
				}
			} else { 
				// 비밀번호 일치하지 않을 경우
				return 0;
			}
		} else {
			// 아이디 없을 경우
			return -1;
		}
		
		
	}
	@Override
	public String getSaltById(String memId) {
		return memberDao.getSaltById(memId);
	}
	@Override
	public String findId(String email) {
		return memberDao.findId(email);
	}
	@Override
	public String findPassword(String memId, String memEmail) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("memId", memId);
		map.put("memEmail", memEmail);
		
		int result = memberDao.findPassword(map);
		
		if(result == 1) {
			StringBuffer ranNum = new StringBuffer();
			
			Random ran = new Random();
			for(int i=0; i<6; i++) {
				ranNum.append(ran.nextInt(10));
			}
			
			String salt = SHA256Util.generateSalt();
			MemberVO member = new MemberVO();
			member.setSalt(salt); 
			
			String password = ranNum.toString();
			password = SHA256Util.getEncrypt(password, salt);
			
			map.put("memPassword", password);
			map.put("salt", salt);
			
			memberDao.updatePassword(map);
			
			return ranNum.toString();
		} else {
			return "-1";
		}
		
	}
	@Override
	public int idCheck(String id) {
		return memberDao.idCheck(id);
	}
	@Override
	public int modify(MemberVO member) {
		return memberDao.modify(member);
	}
	@Override
	public MemberVO selectMember(String memberId) {
		return memberDao.selectMember(memberId);
	}
	@Override
	public int joinConfirm(Map<String, Object> map) {
		return memberDao.joinConfirm(map);
	}
	@Override
	public int memberAuthStatus(LoginVO loginVO) {
		return memberDao.memberAuthStatus(loginVO);
	}
}
