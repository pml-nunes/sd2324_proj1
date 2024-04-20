package tukano.clients.rest;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Users;
import tukano.api.rest.RestUsers;

import java.net.URI;
import java.util.List;

public class RestUsersClient extends RestClient implements Users {

    final Client client;
    final ClientConfig config;
    final WebTarget target;

    public RestUsersClient(URI serverURI) {
        this.config = new ClientConfig();
        config.property( ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property( ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
        this.client = ClientBuilder.newClient(config);
        target = client.target(serverURI).path(RestUsers.PATH);
    }

    public Result<User> getUser(String userId, String pwd) {
    	return super.reTry( () -> clt_getUser(userId, pwd));
    }

    private Result<User> clt_getUser(String userId, String pwd) {
    	return super.toJavaResult(
    			target.path( userId )
    			.queryParam(RestUsers.PWD, pwd).request()
    			.accept(MediaType.APPLICATION_JSON)
    			.get(), User.class);
    }

    public Result<Void> findUser(String name) {
    	return super.reTry( () -> clt_findUser(name));
    }

    private Result<Void> clt_findUser(String name) {
    	return super.toJavaResult(
            target.path(name)
                .request()
                .get(), Void.class);
    }

    @Override
    public Result<String> createUser(User user) {
        throw new UnsupportedOperationException(
            "Method is out of scope for this project (not used by our servers neither the tester)");
    }

    @Override
    public Result<User> updateUser(String userId, String pwd, User user) {
        throw new UnsupportedOperationException(
            "Method is out of scope for this project (not used by our servers neither the tester)");
    }

    @Override
    public Result<User> deleteUser(String userId, String pwd) {
        throw new UnsupportedOperationException(
            "Method is out of scope for this project (not used by our servers neither the tester)");
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        throw new UnsupportedOperationException(
            "Method is out of scope for this project (not used by our servers neither the tester)");
    }
}