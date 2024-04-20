package tukano.clients.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.java.Users;
import tukano.impl.grpc.generated_java.UsersGrpc;
import tukano.impl.grpc.generated_java.UsersProtoBuf.GetUserArgs;
import tukano.impl.grpc.generated_java.UsersProtoBuf.FindUserArgs;

import static tukano.api.java.Result.error;
import static tukano.api.java.Result.ok;
import static tukano.impl.grpc.common.DataModelAdaptor.GrpcUser_to_User;

import java.net.URI;
import java.util.List;
import java.util.function.Supplier;

public class GrpcUsersClient implements Users {
    final UsersGrpc.UsersBlockingStub stub;

    public GrpcUsersClient(URI serverURI) {
        var channel = ManagedChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort()).usePlaintext().build();
        stub = UsersGrpc.newBlockingStub( channel );
    }
    @Override
    public Result<String> createUser(User user) {
        throw new UnsupportedOperationException(
                "Method is out of scope for this project (not used by our servers neither the tester)");
    }

    @Override
    public Result<User> getUser(String userId, String pwd) {

        return toJavaResult(() -> {
            var res = stub.getUser(GetUserArgs.newBuilder().setUserId(userId).setPassword(pwd).build());
            return GrpcUser_to_User(res.getUser());
        });
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

    @Override
    public Result<Void> findUser(String userId) {
        return toJavaResult(() -> {
            var res = stub.findUser(FindUserArgs.newBuilder().setUserId(userId).build());
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
    static ErrorCode statusToErrorCode(Status status ) {
        return switch( status.getCode() ) {
            case OK -> ErrorCode.OK;
            case NOT_FOUND -> ErrorCode.NOT_FOUND;
            case ALREADY_EXISTS -> ErrorCode.CONFLICT;
            case PERMISSION_DENIED -> ErrorCode.FORBIDDEN;
            case INVALID_ARGUMENT -> ErrorCode.BAD_REQUEST;
            case UNIMPLEMENTED -> ErrorCode.NOT_IMPLEMENTED;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }
}
