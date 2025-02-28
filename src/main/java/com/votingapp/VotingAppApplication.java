package com.votingapp;

import com.votingapp.client.TcpClient;

import com.votingapp.tcp.TcpServer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class VotingAppApplication {

    public static void main(String[] args) {
        check_mode_and_start(args);
    }

    public static void check_mode_and_start(String[] args) {
        //контекст приложения
        var context = SpringApplication.run(VotingAppApplication.class, args);

        //default mode
        String mode = "server";

        if (args.length == 0) {
            System.out.println("Starting in default mode. You can usage: java -jar VotingApp-1.0-SNAPSHOT.jar --mode=server|client\n");
        } else {
            mode = args[0].split("=")[1];
        }

        switch (mode) {
            case "server":
                var server = context.getBean(TcpServer.class);
                startServer(server);
                break;
            case "client":
                var client = context.getBean(TcpClient.class);
                startClient(client);
                break;
            default:
                System.out.println("Invalid mode. Use 'server' or 'client'.");
                SpringApplication.exit(context, () -> 1);

        }
    }

    private static void startServer(TcpServer server) {
        log.info("Starting server...");

        // Логика запуска сервера
        server.run();

    }

    private static void startClient(TcpClient client) {
        System.out.println("Starting client...");

        // Логика запуска клиента
        client.run();
    }

}
