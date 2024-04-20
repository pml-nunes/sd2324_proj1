package tukano.servers.grpc;

import io.grpc.ServerBuilder;

import tukano.api.java.Discovery;

import java.net.InetAddress;
import java.util.logging.Logger;

public class GrpcUsersServer {
    private static Logger Log = Logger.getLogger(GrpcUsersServer.class.getName());

    public static final int PORT = 13456;
    public static final String SERVICE = "users";
    private static final String GRPC_CTX = "/gprc";
    private static final String SERVER_BASE_URI = "grpc://%s:%s%s";
    private static final Discovery discoveryService = Discovery.getInstance();

    public static void main(String[] args) {
        try {
            var stub = new GrpcUsersServerStub();
            var server = ServerBuilder.forPort(PORT).addService(stub).build();
            var serverURI = String.format(SERVER_BASE_URI, InetAddress.getLocalHost().getHostAddress(), PORT, GRPC_CTX);

            Log.info(String.format("%s Server ready @ %s\n", SERVICE, serverURI));

            discoveryService.announce(SERVICE, serverURI);
            server.start().awaitTermination();
        } catch (Exception e) {
            Log.severe(e.getMessage());
        }
    }
}