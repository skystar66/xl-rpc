package com.xlRpc.grpc.server.service;

import com.xlRpc.grpc.common.rpc.CommonServiceGrpc;
import com.xlRpc.grpc.common.rpc.GrpcServerService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProcessService extends CommonServiceGrpc.CommonServiceImplBase {

    @Override
    public void handle(GrpcServerService.Request request, StreamObserver<GrpcServerService.Response> responseObserver) {
        long start = System.currentTimeMillis();
        try {

            responseObserver.onNext(GrpcServerService.Response.newBuilder().setRequestId(request.getRequestId())
                    .setResponse(request.getRequest())
                    .build());
            log.info("serve requestId={} cost={}ms", request.getRequestId(), System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("dispatch requestId={} error", request.getRequestId(), e);
            responseObserver.onNext(GrpcServerService.Response.newBuilder().setRequestId(request.getRequestId())
                    .setResponse(request.getRequest())
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }
}
