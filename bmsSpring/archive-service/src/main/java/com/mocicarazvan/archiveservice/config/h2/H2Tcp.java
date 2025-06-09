package com.mocicarazvan.archiveservice.config.h2;


import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.InetAddress;

@Component
@RequiredArgsConstructor
@Slf4j
public class H2Tcp implements InitializingBean, DisposableBean {
    private final CustomH2Properties customH2Properties;

    private Server tcpServer;

    @Override
    public void afterPropertiesSet() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            String localIp = InetAddress.getLocalHost().getHostAddress();
            log.info("Detected hostname: {}", hostname);
            log.info("Detected local IP: {}", localIp);
            log.info("Configured H2 master host: {}", customH2Properties.getServerHost());


            boolean shouldStart = customH2Properties.getServerHost().equals(localIp) || customH2Properties.getServerHost().contains(hostname);


            if (shouldStart) {
                String rootDirPath = "archive/data".replace("/", File.separator);
                String dbDirPath = rootDirPath + File.separator + "database" + File.separator + "h2";
                File dbDir = new File(dbDirPath);

                tcpServer = Server.createTcpServer(
                        "-tcp", "-tcpAllowOthers", "-tcpPort", "9092", "-baseDir", dbDir.getAbsolutePath(), "-ifNotExists"
                ).start();
                log.info("Embedded H2 TCP server started on port 9092");
            } else {
                log.info("Skipping H2 TCP server start. Connecting as client.");
            }
        } catch (Exception e) {
            log.error("Failed to start embedded H2 TCP server", e);
        }

    }


    // sometimes the server hangs
    @PreDestroy
    public void preDestroy() {
        destroyTcpServer();
    }

    @Override
    public void destroy() {
        destroyTcpServer();
    }

    public void destroyTcpServer() {
        if (tcpServer != null && tcpServer.isRunning(true)) {
            log.info("Shutting down embedded H2 TCP server");
            tcpServer.stop();
        }
    }
}
