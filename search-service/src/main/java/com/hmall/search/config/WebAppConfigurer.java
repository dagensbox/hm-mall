package com.hmall.search.config;

import com.hmall.search.interceptor.MyHeadInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class WebAppConfigurer implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("添加自定义拦截器~~~");
        registry.addInterceptor(new MyHeadInterceptor()).addPathPatterns("/**");
    }
}