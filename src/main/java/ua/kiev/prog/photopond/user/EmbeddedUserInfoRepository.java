package ua.kiev.prog.photopond.user;

import org.springframework.stereotype.Repository;
import ua.kiev.prog.photopond.exception.AddToRepositoryException;

import java.util.*;

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
    synchronized public Optional<UserInfo> findByLogin(String login) {
        return users.stream()
                .filter(Objects::nonNull)
                .filter(u -> Objects.nonNull(u.getLogin()))
                .filter(u -> u.getLogin().equals(login))
                .findFirst();
    }

    @Override
    synchronized public boolean existByLogin(String login) {
        return users.stream()
                .filter(Objects::nonNull)
                .filter(u -> Objects.nonNull(u.getLogin()))
                .anyMatch(u -> u.getLogin().equals(login));
    }

    @Override
    public boolean existByLogin(String login, long exceptId) {
        return users.stream()
                .filter(Objects::nonNull)
                .filter(u -> Objects.nonNull(u.getLogin()))
                .filter(user -> user.getId() != exceptId)
                .anyMatch(user -> user.getLogin().equals(login));
    }

    @Override
    synchronized public void addUser(UserInfo user) throws AddToRepositoryException {
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
    public void delete(long id) {
        users.stream()
                .filter(user -> user.getId() == id)
                .limit(1)
                .forEach(users::remove);

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
    public Optional<UserInfo> getUserById(long id) {
        return users.stream()
                .filter(Objects::nonNull)
                .filter(u -> id == u.getId())
                .findFirst();
    }

}
