package ua.kiev.prog.photopond.user;

import org.springframework.stereotype.Repository;
import ua.kiev.prog.photopond.exception.AddToRepositoryException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class EmbeddedUserInfoRepository implements UserInfoSimpleRepository {
    private List<UserInfo> users = new ArrayList<>();
    private int idIndex;

    public EmbeddedUserInfoRepository() {
    }

    public EmbeddedUserInfoRepository(List<UserInfo> users) {
        this.users = new ArrayList<>(users);
    }

    private int getAndIncID() {
        return idIndex++;
    }
    @Override
    synchronized public UserInfo findByLogin(String login) {
        for (UserInfo user : users) {
            if (user.getLogin().equals(login)) {
                return user;
            }
        }
        return null;
    }

    @Override
    synchronized public boolean existByLogin(String login) {
        for (UserInfo user : users) {
            if (user.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean existByLogin(String login, long exceptId) {
        for (UserInfo user : users) {
            if (user.getId() != exceptId && user.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    @Override
    synchronized public void addUser(UserInfo user) throws AddToRepositoryException{
        if (user == null) {
            throw new AddToRepositoryException("Can't add NULL-value as user in Repository");
        }
        user.setId(getAndIncID());
        users.add(user);
    }

    @Override
    public List<UserInfo> getAllUsers() {
        return Collections.unmodifiableList(users);
    }

    @Override
    public UserInfo delete(long id) {
        for (UserInfo user : users) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }

    @Override
    public UserInfo update(UserInfo userInfo) {
        for (UserInfo user : users) {
            if (user.getId() == userInfo.getId()) {
                user.setLogin(userInfo.getLogin());
                user.setPassword(userInfo.getPassword());
                user.setRole(userInfo.getRole());
                return user;
            }
        }
        return null;
    }

    @Override
    public UserInfo getUserById(long id) {
        for (UserInfo user : users) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }

}
