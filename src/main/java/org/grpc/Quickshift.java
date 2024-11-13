package org.grpc;

import io.grpc.ServerBuilder;
import org.grpc.service.Greeter;
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

    public static void main(String[] args) {
        SpringApplication.run(Quickshift.class, args);
    }

    @Bean
    public CommandLineRunner demo(ShiftRepository shiftRepository, EmployeeRepository employeeRepository){
        return (args) -> {
            io.grpc.Server server = ServerBuilder
                    .forPort(50051)
                    .addService(new Greeter())
                    .build();
            server.start();
            System.out.println("Server running on port 50051");
            server.awaitTermination();
        };
    }

}
