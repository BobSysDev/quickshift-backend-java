package org.grpc;

import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import org.grpc.service.EmployeeGrpcImpl;
import org.grpc.service.ShiftGrpcImpl;
import org.grpc.service.repositories.EmployeeRepository;
import org.grpc.service.repositories.ShiftRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Quickshift {
    private static final Logger log = LoggerFactory.getLogger(Quickshift.class);
    private static final int serverPort = 50051;

    public static void main(String[] args) {
        SpringApplication.run(Quickshift.class, args);
    }

    @Bean
    public CommandLineRunner demo(ShiftRepository shiftRepository, EmployeeRepository employeeRepository){
        return (args) -> {
            io.grpc.Server server = ServerBuilder
                    .forPort(serverPort)
                    .addService(new ShiftGrpcImpl(shiftRepository, employeeRepository))
                    .addService(new EmployeeGrpcImpl(shiftRepository, employeeRepository))
                    .addService(ProtoReflectionService.newInstance())
                    .build();
            server.start();
            log.info("Server running on port " + serverPort);
            log.info("Full server address: gprc://localhost:" + serverPort);
            server.awaitTermination();
        };
    }

}
