
package com.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.web.domain.Token;



/**
 * token服务（JWT + 黑名单）
 */
public interface TokenService extends IService<Token> {

	/**
	 * 生成 JWT Token
	 */
   	String generateToken(Integer userId, String userName, String tableName, String role);

	/**
	 * 验证 Token 是否有效（检查JWT有效性 + 黑名单）
	 */
   	boolean validateToken(String token);

	/**
	 * 将 Token 加入黑名单（用于登出、封禁）
	 */
	void addToBlacklist(String token);

	/**
	 * 检查 Token 是否在黑名单中
	 */
	boolean isInBlacklist(String token);

	/**
	 * 根据用户ID将所有token加入黑名单（用于封禁用户）
	 */
	void blacklistUserTokens(Integer userId);
}
