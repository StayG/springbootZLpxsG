package com.web.domain.Resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class TokenResp implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer id;
	
	
	private Integer userId;
	
	
	private String userName;
	
	
	private String tableName;
	
	
	private String role;
	
	
	private String token;
	
	
	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	private Date expiratedTime;
	
	
	@JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	
}
