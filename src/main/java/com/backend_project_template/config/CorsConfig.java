package com.backend_project_template.config;

import org.springframework.beans.factory.annotation.Value;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

  private static final int IMAGE_CACHE_DAYS = 30;

  @Value("${cors.allowedOrigins}")
  private String allowedOrigins;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    String[] origins = allowedOrigins.split(",");
    registry.addMapping("/**").allowedOrigins(origins).allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD",
        "OPTIONS");
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/uploads/**")
        .addResourceLocations("file:uploads/")
        .setCacheControl(CacheControl.maxAge(Duration.ofDays(IMAGE_CACHE_DAYS)).cachePublic());
    registry.addResourceHandler("/images/**")
        .addResourceLocations("classpath:/static/images/")
        .setCacheControl(CacheControl.maxAge(Duration.ofDays(IMAGE_CACHE_DAYS)).cachePublic());
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    String[] origins = allowedOrigins.split(",");
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(origins));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
