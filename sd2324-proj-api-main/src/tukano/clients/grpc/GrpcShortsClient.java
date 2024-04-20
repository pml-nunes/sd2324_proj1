package tukano.clients.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import tukano.api.Short;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.impl.grpc.generated_java.ShortsGrpc;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.DeleteUserShortsArgs;

import java.net.URI;
import java.util.List;
import java.util.function.Supplier;

import static tukano.api.java.Result.error;
import static tukano.api.java.Result.ok;

public class GrpcShortsClient implements Shorts {
    final ShortsGrpc.ShortsBlockingStub stub;

    public GrpcShortsClient(URI serverURI){
        var channel = ManagedChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort()).usePlaintext().build();
        stub = ShortsGrpc.newBlockingStub( channel );

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

    @Override
    public Result<Void> deleteUserShorts(String userId, String password) {
        return toJavaResult(() -> {
            var res = stub.deleteUserShorts(DeleteUserShortsArgs.newBuilder()
                    .setUserId(userId).setPassword(password).build());
            return null;
        });

    }

    static <T> Result<T> toJavaResult(Supplier<T> func) {
        try {
            return ok(func.get());
        } catch(StatusRuntimeException sre) {
            var code = sre.getStatus().getCode();
            if( code == Status.Code.UNAVAILABLE || code == Status.Code.DEADLINE_EXCEEDED )
                throw sre;
            return error( statusToErrorCode( sre.getStatus() ) );
        }
    }
    static Result.ErrorCode statusToErrorCode(Status status ) {
        return switch( status.getCode() ) {
            case OK -> Result.ErrorCode.OK;
            case NOT_FOUND -> Result.ErrorCode.NOT_FOUND;
            case ALREADY_EXISTS -> Result.ErrorCode.CONFLICT;
            case PERMISSION_DENIED -> Result.ErrorCode.FORBIDDEN;
            case INVALID_ARGUMENT -> Result.ErrorCode.BAD_REQUEST;
            case UNIMPLEMENTED -> Result.ErrorCode.NOT_IMPLEMENTED;
            default -> Result.ErrorCode.INTERNAL_ERROR;
        };
    }
}
