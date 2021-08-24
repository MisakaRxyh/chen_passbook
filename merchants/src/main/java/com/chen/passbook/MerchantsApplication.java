package com.chen.passbook;

import com.chen.passbook.merchants.security.AuthCheckInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Resource;

@SpringBootApplication
public class MerchantsApplication extends WebMvcConfigurerAdapter {

	/**
	 * 注入自定义的拦截器
	 */
	@Resource
	private AuthCheckInterceptor authCheckInterceptor;

	public static void main(String[] args) {
		SpringApplication.run(MerchantsApplication.class, args);
	}

	/**
	 * 实现自定义的拦截器
	 * @param registry
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		/**
		 * 将自定义的拦截器注册进spring的拦截器并增加拦截地址
		 */
		registry.addInterceptor(authCheckInterceptor).addPathPatterns("/merchants/**");
		super.addInterceptors(registry);
	}
}
