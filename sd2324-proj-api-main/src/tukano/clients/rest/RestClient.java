package tukano.clients.rest;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;

import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.utils.Sleep;

import java.util.function.Supplier;

public class RestClient {
    protected static final int MAX_RETRIES = 5;
    protected static final int RETRY_SLEEP = 5000;
	protected static final int READ_TIMEOUT = 1000;
    protected static final int CONNECT_TIMEOUT = 1000;

    protected <T> Result<T> reTry(Supplier<Result<T>> func) {
    	for (int i = 0; i < MAX_RETRIES; i++)
    		try {
    			return func.get();
    		} catch (ProcessingException x) {
    			Sleep.ms(RETRY_SLEEP);
    		} catch (Exception x) {
    			x.printStackTrace();
    			return Result.error(ErrorCode.INTERNAL_ERROR);
    		}
    	return Result.error(ErrorCode.TIMEOUT);
    }

    protected <T> Result<T> toJavaResult(Response r, Class<T> entityType) {
    	try {
			var status = r.getStatus();
			if (status != Response.Status.OK.getStatusCode())
				return Result.error(getErrorCodeFrom(status));
			else
				return Result.ok();
    	} finally {
    		r.close();
    	}
    }

    public static ErrorCode getErrorCodeFrom(int status) {
        return switch (status) {
            case 200, 209 -> ErrorCode.OK;
            case 409 -> ErrorCode.CONFLICT;
            case 403 -> ErrorCode.FORBIDDEN;
            case 404 -> ErrorCode.NOT_FOUND;
            case 400 -> ErrorCode.BAD_REQUEST;
            case 500 -> ErrorCode.INTERNAL_ERROR;
            case 501 -> ErrorCode.NOT_IMPLEMENTED;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }
}