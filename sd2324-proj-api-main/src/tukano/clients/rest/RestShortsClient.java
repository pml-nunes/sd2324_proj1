package tukano.clients.rest;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import tukano.api.Short;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.api.rest.RestShorts;

import java.net.URI;
import java.util.List;

public class RestShortsClient extends RestClient implements Shorts {

    final Client client;
    final ClientConfig config;
    final WebTarget target;

    public RestShortsClient( URI serverURI ) {
        this.config = new ClientConfig();
        config.property( ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property( ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
        this.client = ClientBuilder.newClient(config);
        target = client.target(serverURI).path(RestShorts.PATH);
    }

    public Result<Void> deleteUserShorts(String userId, String pwd) {
    	return super.reTry( () -> clt_deleteUserShorts(userId, pwd));
    }

    private Result<Void> clt_deleteUserShorts(String userId, String pwd) {
    	return super.toJavaResult(
             target.path(userId + RestShorts.SHORTS)
            .queryParam(RestShorts.PWD, pwd).request()
            .delete(), Void.class);
    }

    @Override
    public Result<Short> createShort(String userId, String password) {
        throw new UnsupportedOperationException(
            "Method is out of scope for this project (not used by our servers neither the tester)");
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        throw new UnsupportedOperationException(
            "Method is out of scope for this project (not used by our servers neither the tester)");
    }

    @Override
    public Result<Short> getShort(String shortId) {
        throw new UnsupportedOperationException(
            "Method is out of scope for this project (not used by our servers neither the tester)");
    }

    @Override
    public Result<List<String>> getShorts(String userId) {
        throw new UnsupportedOperationException(
            "Method is out of scope for this project (not used by our servers neither the tester)");
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        throw new UnsupportedOperationException(
            "Method is out of scope for this project (not used by our servers neither the tester)");
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        throw new UnsupportedOperationException(
            "Method is out of scope for this project (not used by our servers neither the tester)");
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        throw new UnsupportedOperationException(
            "Method is out of scope for this project (not used by our servers neither the tester)");
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        throw new UnsupportedOperationException(
            "Method is out of scope for this project (not used by our servers neither the tester)");
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        throw new UnsupportedOperationException(
            "Method is out of scope for this project (not used by our servers neither the tester)");
    }
}