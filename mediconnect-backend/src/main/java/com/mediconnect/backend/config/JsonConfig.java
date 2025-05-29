package com.mediconnect.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class JsonConfig {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_FORMAT);

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Configure JavaTime module for both LocalDate and LocalDateTime
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // LocalDate configuration
        javaTimeModule.addDeserializer(LocalDate.class, 
            new LocalDateDeserializer(DATE_FORMATTER));
        javaTimeModule.addSerializer(LocalDate.class, 
            new LocalDateSerializer(DATE_FORMATTER));
            
        // LocalDateTime configuration
        javaTimeModule.addDeserializer(LocalDateTime.class,
            new LocalDateTimeDeserializer(DATETIME_FORMATTER));
        javaTimeModule.addSerializer(LocalDateTime.class,
            new LocalDateTimeSerializer(DATETIME_FORMATTER));
            
        mapper.registerModule(javaTimeModule);
        
        // Configure Hibernate5 module to handle lazy loading
        Hibernate5JakartaModule hibernate5Module = new Hibernate5JakartaModule();
        hibernate5Module.configure(Hibernate5JakartaModule.Feature.FORCE_LAZY_LOADING, false);
        hibernate5Module.configure(Hibernate5JakartaModule.Feature.USE_TRANSIENT_ANNOTATION, true);
        mapper.registerModule(hibernate5Module);
        
        // Disable failing on empty beans and writing dates as timestamps
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return mapper;
    }
} 