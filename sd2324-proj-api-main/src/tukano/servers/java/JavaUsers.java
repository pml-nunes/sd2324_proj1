package tukano.servers.java;


import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.java.Shorts;
import tukano.api.java.Users;
import tukano.clients.ClientFactory;
import tukano.persistence.Hibernate;

import java.util.List;
import java.util.logging.Logger;

public class JavaUsers implements Users {
    private static final String FIND_ID = "SELECT * FROM User u WHERE u.userId = '%s'";
    private static final String SEARCH_ALL = "SELECT * FROM User u WHERE LOWER(u.userId) LIKE LOWER('%%%s%%')";
    private static Logger Log = Logger.getLogger(JavaUsers.class.getName());
    private final Hibernate hib = Hibernate.getInstance();

    @Override
    public Result<String> createUser(User user) {
        Log.info("createUser : " + user);

        if(user.userId() == null || user.pwd() == null || user.displayName() == null || user.email() == null) {
            Log.info("User object invalid.");
            return Result.error( ErrorCode.BAD_REQUEST);
        }

        if(hib.sql(String.format(FIND_ID, user.userId()), User.class).size() != 0) {
            Log.info("User already exists.");
            return Result.error( ErrorCode.CONFLICT);
        }
        this.hib.persist(user);
        return Result.ok( user.userId() );
    }

    @Override
    public Result<User> getUser(String userId, String pwd) {
        Log.info("getUser : user = " + userId + "; pwd = " + pwd);

        if(userId == null || pwd == null) {
            Log.info("Name or Password null.");
            return Result.error( ErrorCode.BAD_REQUEST);
        }

        User user;
        try {
            user = hib.sql(String.format(FIND_ID, userId), User.class).get(0);
        } catch(Exception e) {
            Log.info("User does not exist.");
            return Result.error( ErrorCode.NOT_FOUND);
        }

        if( !user.pwd().equals( pwd)) {
            Log.info("Password is incorrect.");
            return Result.error( ErrorCode.FORBIDDEN);
        }

        return Result.ok(user);
    }

    @Override
    public Result<User> updateUser(String userId, String pwd, User newUser) {
        Log.info("updateUser : user = " + userId + "; pwd = " + pwd + " ; newUser = " + newUser);

        if(userId == null || pwd == null || (newUser.userId() != null && !userId.equals(newUser.userId()))) {
            Log.info("Name or Password null.");
            return Result.error( ErrorCode.BAD_REQUEST);
        }

        User oldUser;
        try {
            oldUser = hib.sql(String.format(FIND_ID, userId), User.class).get(0);
        } catch(Exception e) {
            Log.info("User does not exist.");
            return Result.error( ErrorCode.NOT_FOUND);
        }

        if( !oldUser.pwd().equals(pwd)) {
            Log.info("Password is incorrect.");
            return Result.error( ErrorCode.FORBIDDEN);
        }

        newUser.setUserId(userId);
        if(newUser.getPwd() == null)
            newUser.setPwd(pwd);
        if(newUser.getDisplayName() == null)
            newUser.setDisplayName(oldUser.getDisplayName());
        if(newUser.getEmail() == null)
            newUser.setEmail(oldUser.getEmail());
        
        this.hib.update(newUser);
        return Result.ok(newUser);
    }

    @Override
    public Result<User> deleteUser(String userId, String pwd) {
        Log.info("deleteUser : user = " + userId + "; pwd = " + pwd);

        if(userId == null || pwd == null) {
            Log.info("Name or Password null.");
            return Result.error( ErrorCode.BAD_REQUEST);
        }

        User user;
        try {
            user = hib.sql(String.format(FIND_ID, userId), User.class).get(0);
        } catch(Exception e) {
            Log.info("User does not exist.");
            return Result.error( ErrorCode.NOT_FOUND);
        }

        if( !user.pwd().equals(pwd)) {
            Log.info("Password is incorrect.");
            return Result.error( ErrorCode.FORBIDDEN);
        }

        new Thread(() -> {
            ClientFactory.getShortsClient().deleteUserShorts(userId, pwd);
        }).start();

        this.hib.delete(user);
        return Result.ok(user);
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        if(pattern == null) {
            Log.info("pattern null");
            return Result.error(ErrorCode.BAD_REQUEST);
        }
        List<User> users = hib.sql(String.format(SEARCH_ALL, pattern), User.class);
        for(User u : users)
            u.setPwd("");
        return Result.ok(users);
    }

    @Override
    public Result<Void> findUser(String userId) {
        System.out.println("entered userServer findUser");
        try {
            hib.sql(String.format(FIND_ID, userId), User.class).get(0);
        } catch(Exception e) {
            Log.info("User does not exist.");
            return Result.error( ErrorCode.NOT_FOUND);
        }

        return Result.ok();
    }
}
