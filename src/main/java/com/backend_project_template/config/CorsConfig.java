package com.backend_project_template.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

  @Value("${cors.allowedOrigins}")
  private String allowedOrigins;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    System.out.println("CORS ALLOWED ORIGINS: " + allowedOrigins);
    String[] origins = allowedOrigins.split(",");
    registry.addMapping("/**").allowedOrigins(origins).allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS");
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/images/**")
        .addResourceLocations("file:uploads/images/");
  }
}
