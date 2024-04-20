package tukano.servers.java;


import tukano.api.BlobInfo;
import tukano.api.Short;
import tukano.api.Follows;
import tukano.api.Likes;
import tukano.api.java.Blobs;
import tukano.api.java.Discovery;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.api.java.Users;
import tukano.clients.ClientFactory;
import tukano.persistence.Hibernate;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class JavaShorts implements Shorts {

    private static final String GET_BLOB_BY_INDEX = "SELECT * FROM BlobInfo b WHERE b.blobIndex = '%d'";
    private static final String GET_BLOB_BY_URI = "SELECT * FROM BlobInfo b WHERE b.blobURI = '%s'";
    private static final String FIND_ID = "SELECT * FROM Short s WHERE s.shortId = '%s'";
    private static final String SEARCH_USER_SHORTS = "SELECT shortId FROM Short s WHERE s.ownerId = '%s'";
    private static final String SEARCH_USER_SHORTS_DELETE = "SELECT * FROM Short s WHERE s.ownerId = '%s'";
    private static final String USER_FOLLOWER = "SELECT * FROM Follows f WHERE f.ownerId = '%s' AND f.userId = '%s'";
    private static final String USER_FOLLOWERS = "SELECT userId FROM Follows f WHERE f.ownerId = '%s'";
    private static final String USER_FOLLOWS_DELETE = "SELECT * FROM Follows f WHERE f.ownerId = '%s' OR f.userId = '%s'";
    private static final String FIND_LIKE = "SELECT * FROM Likes l WHERE l.shortId = '%s' AND l.userId LIKE '%s'";
    private static final String FIND_LIKES = "SELECT userId FROM Likes l WHERE l.shortId = '%s'";
    private static final String GET_LIKES = "SELECT * FROM Likes l WHERE l.shortId = '%s'";
    private static final String FIND_LIKES_DELETE = "SELECT * FROM Likes l WHERE l.shortId = '%s'";
    private static final String FIND_USER_LIKES_DELETE = "SELECT * FROM Likes l WHERE l.userId = '%s'";
    private static final String USER_FEED =
            "SELECT Sho.shortId FROM " +
                    "((SELECT Short.shortId, Short.timestamp FROM Short LEFT OUTER JOIN Follows " +
                    "ON Short.ownerId = Follows.ownerId WHERE Follows.userId = '%s') " +
                    "UNION " +
                    "(SELECT Short.shortId, Short.timestamp FROM Short WHERE Short.ownerId = '%s')) as Sho " +
                    "ORDER BY Sho.timestamp DESC";

    private static final String BLOB_SERVICE = "blobs";
    private static final String BLOB_URL_FORMAT = "%s/blobs/%s";
    private static final String BLOB_URL_DELIMITER = "/blobs/";
    
    private static Logger Log = Logger.getLogger(JavaShorts.class.getName());
    private static final Hibernate hib = Hibernate.getInstance();

    private static final Users clientUsers = ClientFactory.getUsersClient();
    private static final Map<String, Blobs> clientsBlobs = initBlobs();

    private static Map<String, Blobs> initBlobs() {
        Map<String, Blobs> clients =  new HashMap<String, Blobs>();
        URI[] blobsURI = Discovery.getInstance().knownUrisOf(BLOB_SERVICE, 1);
        for (int i = 0; i < blobsURI.length; i++) {
            String uriString = blobsURI[i].toString();
            hib.persist(new BlobInfo(i + 1, uriString, 0)); 
            clients.put(uriString, ClientFactory.getBlobsClient(blobsURI[i]));
        }
        return clients;
    }

    @Override
    public Result<Short> createShort(String userId, String password) {

        if(userId == null || password == null)
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        var result = clientUsers.getUser(userId, password);
        if(!result.isOK())
            return Result.error(result.error());

        String shortId = UUID.randomUUID().toString();
        BlobInfo chosenBlob = this.pickBlob();
        String blobURL = String.format(BLOB_URL_FORMAT,chosenBlob.getBlobURI().toString(), shortId);
        Short shortUser = new Short(shortId, userId, blobURL);
        hib.persist(shortUser);

        chosenBlob.setCurrentLoad(chosenBlob.getCurrentLoad() + 1);
        hib.update(chosenBlob);
        return Result.ok(shortUser);
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {

        Short foundShort;
        try {
            foundShort = hib.sql(String.format(FIND_ID, shortId), Short.class).get(0);
        } catch(Exception e) {
            Log.info("Short does not exist.");
            return Result.error( Result.ErrorCode.NOT_FOUND);
        }
        var result = clientUsers.getUser(foundShort.getOwnerId(), password);
        if(!result.isOK())
            return Result.error(Result.ErrorCode.FORBIDDEN);
        hib.delete(foundShort);

        String[] blobData = foundShort.getBlobUrl().split(BLOB_URL_DELIMITER);
        BlobInfo blob = hib.sql(String.format(GET_BLOB_BY_URI, blobData[0]),BlobInfo.class).get(0);
        blob.setCurrentLoad(blob.getCurrentLoad() - 1);
        hib.update(blob);

        List<Likes> likes = hib.sql(String.format(GET_LIKES, shortId), Likes.class);
        hib.delete(likes.toArray());

        new Thread(() -> {
            clientsBlobs.get(blob.getBlobURI()).delete(blobData[1]);
        }).start();

        return Result.ok();
    }

    @Override
    public Result<Short> getShort(String shortId) {

        Short foundShort;
        try {
            foundShort = hib.sql(String.format(FIND_ID, shortId), Short.class).get(0);
        } catch(Exception e) {
            Log.info("Short does not exist.");
            return Result.error( Result.ErrorCode.NOT_FOUND);
        }
        return Result.ok(foundShort);
    }

    @Override
    public Result<List<String>> getShorts(String userId) {
        var result = clientUsers.findUser(userId);
        if(!result.isOK()){
            return Result.error(result.error());
        }

        List<String> shorts = hib.sql(String.format(SEARCH_USER_SHORTS, userId), String.class);
        return Result.ok(shorts);
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        var result = clientUsers.getUser(userId1, password);
        var result2 = clientUsers.findUser(userId2);
        if(result.isOK()){
            if(result2.isOK()){

                List<Follows> follower = hib.sql(String.format(USER_FOLLOWER, userId2, userId1), Follows.class);

                if(!follower.isEmpty()){
                    if(isFollowing)
                        return Result.error( Result.ErrorCode.CONFLICT);
                    else
                        hib.delete(follower.get(0));
                }else{
                    if(isFollowing)
                        hib.persist(new Follows(UUID.randomUUID().toString(), userId2, userId1));
                }
                
            }else{
                return Result.error(result2.error());
            }
            
        } else{
            return Result.error(result.error());
        }

        return Result.ok();
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        var result = clientUsers.getUser(userId, password);
        if(result.isOK()){

            List<String> followers = hib.sql(String.format(USER_FOLLOWERS, userId), String.class);

            return Result.ok(followers);
        }

        return Result.error(result.error());
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {

        var result1 = clientUsers.getUser(userId, password);

        if(result1.isOK()) {
            var result2 = this.getShort(shortId);

            if(result2.isOK()){
                Short val = result2.value();

                List<Likes> foundLike = hib.sql(String.format(FIND_LIKE, shortId, userId), Likes.class);

                if(!foundLike.isEmpty()){
                    if(isLiked) {
                        return Result.error(Result.ErrorCode.CONFLICT);
                    }else {
                        hib.delete(foundLike.get(0));
                        val.setTotalLikes(val.getTotalLikes()-1);
                        hib.update(val);
                    }
                }else{
                    if(isLiked) {
                        hib.persist(new Likes(UUID.randomUUID().toString(), shortId, userId));
                        val.setTotalLikes(val.getTotalLikes()+1);
                        hib.update(val);
                    } else {
                        return Result.error(Result.ErrorCode.NOT_FOUND);
                    }
                }

            }else{
                return Result.error(result2.error());
            }

        }else{

            return Result.error(result1.error());
        }

        return Result.ok();

    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {

        var result1 = getShort(shortId);

        if(result1.isOK()){
            var result2 = clientUsers.getUser(result1.value().getOwnerId(), password);
            
            if(result2.isOK()){
                 List<String> likes = hib.sql(String.format(FIND_LIKES, shortId), String.class);

                return Result.ok(likes);
            }else{

                return Result.error(result2.error());
            }

        } else{

            return Result.error(result1.error());
        }
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {

        var result = clientUsers.getUser(userId, password);
        if(!result.isOK()) {
            return Result.error(result.error());
        }

        List<String> feed = hib.sql(String.format(USER_FEED, userId, userId), String.class);

        return Result.ok(feed); 

    }

    @Override
    public Result<Void> deleteUserShorts(String userId, String password) {

        List<Follows> userFollows = hib.sql(String.format(USER_FOLLOWS_DELETE, userId, userId), Follows.class);

        if(!userFollows.isEmpty()) {
            hib.delete(userFollows.toArray());
        }

        List<Likes> userLikes = hib.sql(String.format(FIND_USER_LIKES_DELETE, userId), Likes.class);

        if(!userLikes.isEmpty()){
            for(int j = 0; j < userLikes.size(); j++){

                Likes l = userLikes.get(j);

                List<Short> shortLike =  hib.sql(String.format(FIND_ID, l.getShortId()), Short.class);

                Short val = shortLike.get(0);

                val.setTotalLikes(val.getTotalLikes()-1);
                hib.update(val);

                hib.delete(l);
            }
        }

        List<Short> userShorts = hib.sql(String.format(SEARCH_USER_SHORTS_DELETE, userId), Short.class);

        if(!userShorts.isEmpty()) {
            for (int i = 0; i < userShorts.size(); i++) {

                Short s = userShorts.get(i);

                List<Likes> shortLikes = hib.sql(String.format(FIND_LIKES_DELETE, s.getShortId()), Likes.class);
                if(!shortLikes.isEmpty()){
                    hib.delete(shortLikes.toArray());
                }
                String[] blobData = s.getBlobUrl().split(BLOB_URL_DELIMITER);
                BlobInfo blob = hib.sql(String.format(GET_BLOB_BY_URI, blobData[0]),BlobInfo.class).get(0);
                blob.setCurrentLoad(blob.getCurrentLoad() - 1);
                hib.update(blob);

                new Thread(() -> {
                    clientsBlobs.get(blob.getBlobURI()).delete(blobData[1]);
                }).start();
                hib.delete(s);
            }
        }

        return Result.ok();
    }

    private BlobInfo pickBlob() {
        BlobInfo selectedBlob = null;
        BlobInfo currentBlob;
        int minSize = Integer.MAX_VALUE;
        for(int i = 0; i < JavaShorts.clientsBlobs.size(); i++) {
            currentBlob = hib.sql(String.format(GET_BLOB_BY_INDEX, i + 1), BlobInfo.class).get(0);

            if(currentBlob.getCurrentLoad() < minSize) {
                selectedBlob = currentBlob;
                minSize = currentBlob.getCurrentLoad();
            }
        }
        return selectedBlob;
    }
}
