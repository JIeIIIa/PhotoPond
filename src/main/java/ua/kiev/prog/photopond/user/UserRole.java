package ua.kiev.prog.photopond.user;

public enum UserRole {
    ADMIN,
    USER;


    @Override
    public String toString() {
        return "Role_" + name();
    }
}
