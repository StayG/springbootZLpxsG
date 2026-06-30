package com.web.controller;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.web.annotation.IgnoreAuth;
import com.web.domain.Config;
import com.web.exception.BusinessException;
import com.web.service.ConfigService;
import com.web.utils.Result;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;

/**
 * 上传文件映射表
 */
@RestController
@RequestMapping("file")
@SuppressWarnings({ "unchecked", "rawtypes" })
public class FileController {
	
	private static final Logger log = LoggerFactory.getLogger(FileController.class);
	
	@Resource
	private ConfigService configService;
	
	/**
	 * 文件上传目录（从配置文件读取）
	 */
	@Value("${file.upload-dir:./upload}")
	private String uploadDir;

	/**
	 * 上传文件
	 * 
	 * @throws Exception
	 */
	@IgnoreAuth
	@RequestMapping("/upload")
	public Result upload(@RequestParam("file") MultipartFile file, String type) throws Exception {
		if (file.isEmpty()) {
			throw new BusinessException("上传文件不能为空");
		}
		
		// ========== 文件安全校验 ==========
		String validationError = validateUploadFile(file, "image");
		if (validationError != null) {
			throw new BusinessException(validationError);
		}
		
		// 获取文件扩展名
		String originalFilename = file.getOriginalFilename();
		String fileExt = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
		
		// 使用配置的上传目录
		File uploadFolder = getUploadFolder();
		
		// 生成唯一文件名
		String fileName = new Date().getTime() + "." + fileExt;
		File dest = new File(uploadFolder, fileName);
		
		// 保存文件
		file.transferTo(dest);
		log.info("文件上传成功: {}", dest.getAbsolutePath());
		
		// 如果是头像文件，保存到配置表
		if (StrUtil.isNotBlank(type) && type.equals("1")) {
			Config config = configService.getOne(Wrappers.lambdaQuery(Config.class).eq(Config::getName, "faceFile"));
			if (config == null) {
				config = new Config();
				config.setName("faceFile");
				config.setValue(fileName);
			} else {
				config.setValue(fileName);
			}
			configService.saveOrUpdate(config);
		}
		
		return Result.success(fileName);
	}
	
	/**
	 * 获取上传文件夹（自动创建）
	 */
	private File getUploadFolder() {
		File uploadFolder;
		
		// 判断是否为开发环境（IDE 运行）
		if (isDevelopmentMode()) {
			// 开发环境：使用项目内路径
			try {
				// 获取 classpath 路径 (例如: /back-stage/target/classes/)
				String classPath = this.getClass().getResource("/").getPath();
				File classesDir = new File(classPath);
				// 向上两级到达 back-stage 目录
				File backStageRoot = classesDir.getParentFile().getParentFile();
				// 构建到 src/main/resources/static/upload 的路径
				uploadFolder = new File(backStageRoot, "src/main/resources/static/upload/");
				
				log.info("开发环境 - 使用项目内路径: {}", uploadFolder.getAbsolutePath());
			} catch (Exception e) {
				// 如果获取失败，使用配置的路径
				uploadFolder = new File(uploadDir);
				log.warn("开发环境路径获取失败，使用配置路径: {}", uploadFolder.getAbsolutePath(), e);
			}
		} else {
			// 生产环境：使用配置的外部路径
			uploadFolder = new File(uploadDir);
			log.info("生产环境 - 使用配置路径: {}", uploadFolder.getAbsolutePath());
		}
		
		// 确保目录存在
		if (!uploadFolder.exists()) {
			boolean created = uploadFolder.mkdirs();
			if (created) {
				log.info("创建上传目录: {}", uploadFolder.getAbsolutePath());
			} else {
				log.error("创建上传目录失败: {}", uploadFolder.getAbsolutePath());
			}
		}
		
		return uploadFolder;
	}
	
	/**
	 * 判断是否为开发模式（IDE 运行）
	 */
	private boolean isDevelopmentMode() {
		try {
			// 检查是否存在 target/classes 目录（Maven 编译输出）
			String classPath = this.getClass().getResource("/").getPath();
			boolean isDev = classPath.contains("/target/classes") || classPath.contains("\\target\\classes");
			log.info("环境检测 - classPath: {}", classPath);
			log.info("环境检测 - isDevelopmentMode: {}", isDev);
			return isDev;
		} catch (Exception e) {
			log.error("环境检测失败", e);
			return false;
		}
	}

	/**
	 * 下载文件
	 */
	@IgnoreAuth
	@RequestMapping("/download")
	public ResponseEntity<byte[]> download(@RequestParam String fileName) {
		try {
			// 文件名安全校验（防止路径穿越攻击）
			if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
				log.warn("检测到非法文件名: {}", fileName);
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			
			// 使用配置的上传目录
			File uploadFolder = getUploadFolder();
			File file = new File(uploadFolder, fileName);
			
			if (file.exists() && file.isFile()) {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
				headers.setContentDispositionFormData("attachment", fileName);
				log.info("文件下载: {}", file.getAbsolutePath());
				return new ResponseEntity<>(FileUtils.readFileToByteArray(file), headers, HttpStatus.OK);
			} else {
				log.warn("文件不存在: {}", file.getAbsolutePath());
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		} catch (IOException e) {
			log.error("文件下载失败: {}", fileName, e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 文件上传安全校验
	 * 
	 * @param file 上传的文件
	 * @param fileType 文件类型（image/excel）
	 * @return 校验失败返回错误信息，校验通过返回 null
	 */
	private String validateUploadFile(MultipartFile file, String fileType) {
		if (file == null || file.isEmpty()) {
			return "上传文件不能为空";
		}

		// 1. 文件名校验
		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null || originalFilename.trim().isEmpty()) {
			return "文件名不能为空";
		}

		// 2. 文件名安全校验（防止路径穿越攻击）
		if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
			return "文件名包含非法字符";
		}

		// 3. 文件扩展名校验
		if (!originalFilename.contains(".")) {
			return "文件必须包含扩展名";
		}

		String fileExt = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
		
		// 根据文件类型校验扩展名
		if ("image".equals(fileType)) {
			// 图片文件白名单
			String[] allowedImageExts = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};
			boolean isValidExt = false;
			for (String ext : allowedImageExts) {
				if (ext.equals(fileExt)) {
					isValidExt = true;
					break;
				}
			}
			if (!isValidExt) {
				return "只允许上传图片文件（jpg、jpeg、png、gif、bmp、webp）";
			}
		} else if ("excel".equals(fileType)) {
			// Excel 文件白名单
			String[] allowedExcelExts = {"xls", "xlsx"};
			boolean isValidExt = false;
			for (String ext : allowedExcelExts) {
				if (ext.equals(fileExt)) {
					isValidExt = true;
					break;
				}
			}
			if (!isValidExt) {
				return "只允许上传 Excel 文件（xls、xlsx）";
			}
		}

		// 4. 文件大小校验
		long fileSize = file.getSize();
		long maxSize;
		
		if ("image".equals(fileType)) {
			maxSize = 5 * 1024 * 1024; // 图片最大 5MB
			if (fileSize > maxSize) {
				return "图片文件大小不能超过 5MB";
			}
		} else if ("excel".equals(fileType)) {
			maxSize = 10 * 1024 * 1024; // Excel 最大 10MB
			if (fileSize > maxSize) {
				return "Excel 文件大小不能超过 10MB";
			}
		}

		if (fileSize == 0) {
			return "文件大小不能为 0";
		}

		// 5. 文件内容类型校验（MIME 类型）
		String contentType = file.getContentType();
		if (contentType == null || contentType.trim().isEmpty()) {
			return "无法识别文件类型";
		}

		if ("image".equals(fileType)) {
			// 图片 MIME 类型白名单
			if (!contentType.startsWith("image/")) {
				return "文件类型不是有效的图片格式";
			}
		} else if ("excel".equals(fileType)) {
			// Excel MIME 类型白名单
			boolean isValidMime = contentType.equals("application/vnd.ms-excel") ||
					contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
					contentType.equals("application/octet-stream");
			if (!isValidMime) {
				return "文件类型不是有效的 Excel 格式";
			}
		}

		// 校验通过
		return null;
	}

}
