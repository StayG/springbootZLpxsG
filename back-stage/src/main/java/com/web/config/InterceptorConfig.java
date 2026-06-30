package com.web.config;


import com.web.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.io.File;

@Configuration
public class InterceptorConfig extends WebMvcConfigurationSupport{
	
	/**
	 * 文件上传目录（从配置文件读取）
	 */
	@Value("${file.upload-dir:./upload}")
	private String uploadDir;
	
	@Bean
    public LoginInterceptor getLoginInterceptor() {
        return new LoginInterceptor();
    }
	
	@Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(getLoginInterceptor()).addPathPatterns("/**").excludePathPatterns("/static/**").excludePathPatterns("/upload/**");
        super.addInterceptors(registry);
	}
	
	/**
	 * springboot 2.0配置WebMvcConfigurationSupport之后，会导致默认配置被覆盖，要访问静态资源需要重写addResourceHandlers方法
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 静态资源映射
		registry.addResourceHandler("/**")
				.addResourceLocations("classpath:/resources/")
				.addResourceLocations("classpath:/static/")
				.addResourceLocations("classpath:/admin/")
				.addResourceLocations("classpath:/img/")
				.addResourceLocations("classpath:/client/")
				.addResourceLocations("classpath:/public/");

		// 上传文件映射（支持开发和生产环境）
		File uploadFolder = getUploadFolder();
		String uploadPath = "file:" + uploadFolder.getAbsolutePath() + File.separator;
		
		registry.addResourceHandler("/upload/**")
				.addResourceLocations(uploadPath);

		super.addResourceHandlers(registry);
	}
	
	/**
	 * 获取上传文件夹路径（与 FileController 保持一致）
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
				
				System.out.println("[InterceptorConfig] 开发环境 - 上传路径: " + uploadFolder.getAbsolutePath());
			} catch (Exception e) {
				// 如果获取失败，使用配置的路径
				uploadFolder = new File(uploadDir);
				System.out.println("[InterceptorConfig] 开发环境路径获取失败，使用配置路径: " + uploadFolder.getAbsolutePath());
				e.printStackTrace();
			}
		} else {
			// 生产环境：使用配置的外部路径
			uploadFolder = new File(uploadDir);
			System.out.println("[InterceptorConfig] 生产环境 - 使用配置路径: " + uploadFolder.getAbsolutePath());
		}
		
		// 确保目录存在
		if (!uploadFolder.exists()) {
			boolean created = uploadFolder.mkdirs();
			if (created) {
				System.out.println("[InterceptorConfig] 创建上传目录: " + uploadFolder.getAbsolutePath());
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
			System.out.println("[InterceptorConfig] 环境检测 - classPath: " + classPath);
			System.out.println("[InterceptorConfig] 环境检测 - isDevelopmentMode: " + isDev);
			return isDev;
		} catch (Exception e) {
			System.out.println("[InterceptorConfig] 环境检测失败: " + e.getMessage());
			return false;
		}
	}
}

