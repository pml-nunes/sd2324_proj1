package tukano.clients.rest;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;

import java.net.URI;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.rest.RestBlobs;

public class RestBlobsClient extends RestClient implements Blobs {

    final Client client;
    final ClientConfig config;
    final WebTarget target;

    public RestBlobsClient(URI serverURI) {
        this.config = new ClientConfig();
        config.property( ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property( ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
        this.client = ClientBuilder.newClient(config);
        target = client.target(serverURI).path(RestBlobs.PATH);
    }

    @Override
    public Result<Void> delete(String blobId) {
    	return super.reTry( () -> clt_delete(blobId));
    }

    private Result<Void> clt_delete(String blobId) {
    	return super.toJavaResult(
                target.path(blobId)
                .request()
                .delete(), Void.class);
    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        throw new UnsupportedOperationException(
            "Method is out of scope for this project (not used by our servers neither the tester)");
    }

    @Override
    public Result<byte[]> download(String blobId) {
        throw new UnsupportedOperationException(
            "Method is out of scope for this project (not used by our servers neither the tester)");
    }
}