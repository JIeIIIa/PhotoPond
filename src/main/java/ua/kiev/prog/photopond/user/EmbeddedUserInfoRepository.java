package ua.kiev.prog.photopond.user;

import org.springframework.stereotype.Repository;
import ua.kiev.prog.photopond.exception.AddToRepositoryException;

import java.util.ArrayList;
import java.util.List;

@Repository
public class EmbeddedUserInfoRepository implements UserInfoSimpleRepository {
    private List<UserInfo> users = new ArrayList<>();

    public EmbeddedUserInfoRepository() {
        users.add(new UserInfo("user", "user"));
        users.add(new UserInfo("admin", "admin", UserRole.ADMIN));
    }

    public EmbeddedUserInfoRepository(List<UserInfo> users) {
        this.users = new ArrayList<>(users);
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

    synchronized public void addUser(UserInfo user) throws AddToRepositoryException{
        if (user == null) {
            throw new AddToRepositoryException("Can't add NULL-value as user in Repository");
        }
        users.add(user);
    }


}
