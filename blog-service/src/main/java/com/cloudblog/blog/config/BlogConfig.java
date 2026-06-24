package com.cloudblog.blog.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Data
@RefreshScope
@Configuration
public class BlogConfig {

    @Value("${blog.default.page-size:10}")
    private Integer defaultPageSize;
}
