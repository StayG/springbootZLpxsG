package com.web.service.impl;



import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.web.domain.Token;
import com.web.mapper.TokenMapper;
import com.web.utils.JwtUtil;
import org.springframework.stereotype.Service;

import com.web.service.TokenService;

import jakarta.annotation.Resource;
import java.util.Date;


/**
 * token服务实现（JWT + 黑名单）
 */
@Service("tokenService")
public class TokenServiceImpl extends ServiceImpl<TokenMapper, Token> implements TokenService {

	@Resource
	private TokenMapper tokenMapper;

	@Resource
	private JwtUtil jwtUtil;

	@Override
	public String generateToken(Integer userId, String userName, String tableName, String role) {
		// 使用 JWT 生成 token
		String jwtToken = jwtUtil.generateToken(userId, userName, tableName, role);
		Date expirationDate = jwtUtil.getExpirationDate(jwtToken);

		// 先删除该用户在该表的所有旧token（避免重复记录）
		tokenMapper.delete(
				Wrappers.lambdaQuery(Token.class)
						.eq(Token::getUserId, userId)
						.eq(Token::getTableName, tableName)
		);

		// 插入新的token记录
		baseMapper.insert(new Token(userId, userName, tableName, role, jwtToken, expirationDate));

		return jwtToken;
	}

	@Override
	public boolean validateToken(String token) {
		// 1. 验证 JWT 本身是否有效
		if (!jwtUtil.validateToken(token)) {
			return false;
		}

		// 2. 检查是否在黑名单中
		return !isInBlacklist(token);
	}

	@Override
	public void addToBlacklist(String token) {
		Token tokenEntity = tokenMapper.selectOne(
				Wrappers.lambdaQuery(Token.class).eq(Token::getToken, token)
		);

		if (tokenEntity != null) {
			tokenEntity.setIsBlacklisted(1);
			tokenMapper.updateById(tokenEntity);
		} else {
			// 如果数据库中没有记录，创建一个黑名单记录
			Integer userId = jwtUtil.getUserId(token);
			String userName = jwtUtil.getUserName(token);
			String tableName = jwtUtil.getTableName(token);
			String role = jwtUtil.getRole(token);
			Date expirationDate = jwtUtil.getExpirationDate(token);

			Token blacklistToken = new Token(userId, userName, tableName, role, token, expirationDate);
			blacklistToken.setIsBlacklisted(1);
			baseMapper.insert(blacklistToken);
		}
	}

	@Override
	public boolean isInBlacklist(String token) {
		Token tokenEntity = tokenMapper.selectOne(
				Wrappers.lambdaQuery(Token.class)
						.eq(Token::getToken, token)
						.eq(Token::getIsBlacklisted, 1)
		);
		return tokenEntity != null;
	}

	@Override
	public void blacklistUserTokens(Integer userId) {
		// 将该用户的所有token加入黑名单
		tokenMapper.update(null,
				Wrappers.lambdaUpdate(Token.class)
						.eq(Token::getUserId, userId)
						.set(Token::getIsBlacklisted, 1)
		);
	}
}
