package com.hamsterwhat.wechat.entity.constants;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("app")
@Setter
@Getter
public class AppProperties {

    private WebSocket webSocket;

    private Project project;

    @Setter
    @Getter
    public static class WebSocket {

        private Integer port;

        private String wsPath;

        private Integer readerIdleTimeSeconds;
    }

    @Setter
    @Getter
    public static class Project {

        private String folder;
    }
}
