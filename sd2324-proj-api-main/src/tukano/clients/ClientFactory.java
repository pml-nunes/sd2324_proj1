package tukano.clients;

import java.net.URI;

import tukano.api.java.Blobs;
import tukano.api.java.Discovery;
import tukano.api.java.Users;
import tukano.api.java.Shorts;
import tukano.clients.grpc.GrpcBlobsClient;
import tukano.clients.grpc.GrpcShortsClient;
import tukano.clients.grpc.GrpcUsersClient;
import tukano.clients.rest.RestBlobsClient;
import tukano.clients.rest.RestShortsClient;
import tukano.clients.rest.RestUsersClient;

public class ClientFactory {

    private static final String REST_SERVER = "rest";
    private static final String SHORTS_SERVICE = "shorts";
    private static final String USERS_SERVICE = "users";

    private static final Discovery discoveryService = Discovery.getInstance();

    public static Users getUsersClient() {
        URI serverURI = discoveryService.knownUrisOf(USERS_SERVICE, 1)[0]; 
        if( serverURI.toString().endsWith(REST_SERVER))
            return new RestUsersClient( serverURI );
        else
            return new GrpcUsersClient( serverURI );
    }

    public static Shorts getShortsClient() {
        URI serverURI = discoveryService.knownUrisOf(SHORTS_SERVICE, 1)[0]; 
        System.out.printf("shorts client uri is: %s\n", serverURI.toString());
        if( serverURI.toString().endsWith(REST_SERVER))
            return new RestShortsClient( serverURI );
        else
            return new GrpcShortsClient( serverURI );
    }

    public static Blobs getBlobsClient(URI serverURI) {
        if( serverURI.toString().endsWith(REST_SERVER))
            return new RestBlobsClient( serverURI );
        else    
            return new GrpcBlobsClient( serverURI );
    }
}
