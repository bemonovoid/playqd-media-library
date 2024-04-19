package io.playqd.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.playqd.model.MediaItemType;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class PlayqdWebMvcConfigurer implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**")
        .allowedHeaders("*")
        .allowedMethods("*")
        .allowedOrigins("*");
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new StringToMediaItemTypeEnumConverter());
  }

  @Bean
  Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
    return customizer -> customizer.modules(new JavaTimeModule());
  }

  //TODO document on controller level
  private static class StringToMediaItemTypeEnumConverter implements Converter<String, MediaItemType> {

    @Override
    public MediaItemType convert(String source) {
      return MediaItemType.valueOf(source.toUpperCase());
    }

  }

  //    @Bean
//    public MethodValidationPostProcessor methodValidationPostProcessor() {
//        return new MethodValidationPostProcessor();
//    }

}
