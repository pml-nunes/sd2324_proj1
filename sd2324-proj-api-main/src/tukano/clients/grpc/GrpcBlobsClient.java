package tukano.clients.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.impl.grpc.generated_java.BlobsGrpc;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DeleteArgs;

import static tukano.api.java.Result.error;
import static tukano.api.java.Result.ok;

import java.net.URI;
import java.util.function.Supplier;

public class GrpcBlobsClient implements Blobs {

    final BlobsGrpc.BlobsBlockingStub stub;

    public GrpcBlobsClient(URI serverURI) {
        var channel = ManagedChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort()).usePlaintext().build();
        stub = BlobsGrpc.newBlockingStub( channel );
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

    @Override
    public Result<Void> delete(String blobId) {
        return toJavaResult(() -> {
            var res = stub.delete(DeleteArgs.newBuilder().setBlobId(blobId).build());
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
