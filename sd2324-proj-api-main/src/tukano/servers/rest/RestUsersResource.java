package tukano.servers.rest;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Users;
import tukano.api.rest.RestUsers;
import tukano.servers.java.JavaUsers;

import java.util.List;

@Singleton
public class RestUsersResource implements RestUsers {
    final Users impl;
    public RestUsersResource() {
        this.impl = new JavaUsers();
    }

    @Override
    public String createUser(User user) {
        return resultOrThrow(impl.createUser(user));
    }

    @Override
    public User getUser(String userId, String pwd) {
        return resultOrThrow(impl.getUser(userId, pwd));
    }


    @Override
    public void findUser(String userId) {
        resultOrThrow(impl.findUser(userId));
        throw new WebApplicationException(Response.Status.OK);
    }


    @Override
    public User updateUser(String name, String pwd, User user) {
        return resultOrThrow(impl.updateUser(name, pwd, user));
    }

    @Override
    public User deleteUser(String name, String pwd) {
        return resultOrThrow(impl.deleteUser(name, pwd));
    }

    @Override
    public List<User> searchUsers(String pattern) {
        return resultOrThrow(impl.searchUsers(pattern));
    }

    /**
     * Given a Result<T>, either returns the value, or throws the JAX-WS Exception
     * matching the error code...
     */
    protected <T> T resultOrThrow(Result<T> result) {
        if (result.isOK())
            return result.value();
        else
            throw new WebApplicationException(statusCodeFrom(result));
    }

    /**
     * Translates a Result<T> to a HTTP Status code
     */
    private static Response.Status statusCodeFrom(Result<?> result) {
        return switch (result.error()) {
            case CONFLICT -> Response.Status.CONFLICT;
            case NOT_FOUND -> Response.Status.NOT_FOUND;
            case FORBIDDEN -> Response.Status.FORBIDDEN;
            case BAD_REQUEST -> Response.Status.BAD_REQUEST;
            case INTERNAL_ERROR -> Response.Status.INTERNAL_SERVER_ERROR;
            case NOT_IMPLEMENTED -> Response.Status.NOT_IMPLEMENTED;
            case OK -> result.value() == null ? Response.Status.NO_CONTENT : Response.Status.OK;
            default -> Response.Status.INTERNAL_SERVER_ERROR;
        };
    }
}
