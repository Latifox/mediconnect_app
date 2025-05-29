package com.mediconnect.backend.config;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

@Component
public class WebSocketConfig {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Value("${socket-server.host:0.0.0.0}")
    private String host;

    @Value("${socket-server.port:9092}")
    private Integer port;

    private SocketIOServer server;

    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        config.setHostname(host);
        config.setPort(port);
        config.setOrigin("*"); // Allow all origins for development
        config.setAllowCustomRequests(true);
        config.setPingTimeout(60000); // Set ping timeout to 60 seconds
        config.setPingInterval(25000); // Set ping interval to 25 seconds
        config.setMaxFramePayloadLength(1024 * 1024); // Set max frame size to 1MB

        server = new SocketIOServer(config);
        
        logger.info("Starting SocketIO server on {}:{}", host, port);
        server.start();
        return server;
    }

    @Bean
    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketServer) {
        return new SpringAnnotationScanner(socketServer);
    }

    @PreDestroy
    public void stopSocketIOServer() {
        if (server != null) {
            logger.info("Stopping SocketIO server");
            server.stop();
        }
    }
} 