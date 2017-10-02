package ua.kiev.prog.photopond.user;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UserInfoEmbedRepository implements UserInfoSimpleRepository {
    private List<UserInfo> users = new ArrayList<>();

    public UserInfoEmbedRepository() {
        users.add(new UserInfo("user", "user"));
        users.add(new UserInfo("admin", "admin", UserRole.ADMIN));
    }

    synchronized public UserInfo findByLogin(String login) {
        for (UserInfo user : users) {
            if (user.getLogin().equals(login)) {
                return user;
            }
        }
        return null;
    }

    synchronized public boolean existByLogin(String login) {
        for (UserInfo user : users) {
            if (user.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    synchronized public void addUser(UserInfo user) {
        if (user != null) {
            users.add(user);
        }
    }


}
