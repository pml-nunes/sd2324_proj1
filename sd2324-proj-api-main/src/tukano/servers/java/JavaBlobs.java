package tukano.servers.java;

import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class JavaBlobs implements Blobs {
    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        if (blobId == null)
            return Result.error(ErrorCode.FORBIDDEN);

        try {
            if (Files.exists(Paths.get(blobId))) {
                if(Arrays.equals(Files.readAllBytes(Paths.get(blobId)),bytes))
                    return Result.error(ErrorCode.CONFLICT);
                return Result.ok();
            }
            FileOutputStream fileOut = new FileOutputStream(blobId);
            fileOut.write(bytes);
            fileOut.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.ok();
    }

    @Override
    public Result<byte[]> download(String blobId) {
        try {
            if (Files.exists(Paths.get(blobId)))
                return Result.ok(Files.readAllBytes(Paths.get(blobId)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.error(ErrorCode.NOT_FOUND);
    }

    @Override
    public Result<Void> delete(String blobId) {
        if (blobId == null)
            return Result.error(ErrorCode.FORBIDDEN);
        try {
            if (!Files.exists(Paths.get(blobId)))
                return Result.error(ErrorCode.NOT_FOUND);
            Files.delete(Paths.get(blobId));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.ok();
    }
}
