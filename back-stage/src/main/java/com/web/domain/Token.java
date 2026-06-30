package com.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * token表（JWT黑名单）
 * 用于存储已失效的token，实现主动登出和封禁功能
 */
@Data
@TableName("token")
public class Token implements Serializable {
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	private Integer id;

	/**
	 * 用户id
	 */
	private Integer userId;

	/**
	 * 用户名
	 */
	private String userName;

	/**
	 * 表名
	 */
	private String tableName;

	/**
	 * 角色
	 */
	private String role;

	/**
	 * token凭证（JWT）
	 */
	private String token;

	/**
	 * 过期时间
	 */
	@JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date expiratedTime;

	/**
	 * 新增时间
	 */
	@JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 是否在黑名单中（0-正常，1-已失效）
	 */
	private Integer isBlacklisted;

	public Token() {
	}

	public Token(Integer userId, String userName, String tableName, String role, String token, Date expiratedTime) {
		super();
		this.userId = userId;
		this.userName = userName;
		this.tableName = tableName;
		this.role = role;
		this.token = token;
		this.expiratedTime = expiratedTime;
		this.isBlacklisted = 0; // 默认不在黑名单
	}
}
