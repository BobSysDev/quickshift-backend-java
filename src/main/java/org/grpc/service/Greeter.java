package org.grpc.service;

import io.grpc.stub.StreamObserver;
import quickshift.grpc.*;

public class Greeter extends GreeterGrpc.GreeterImplBase {
    public Greeter(){

    }


    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver){
        String name = request.getName();
        String greeting = "Greetings, " + name + "!";
        HelloReply reply = HelloReply.newBuilder().setMessage(greeting).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
