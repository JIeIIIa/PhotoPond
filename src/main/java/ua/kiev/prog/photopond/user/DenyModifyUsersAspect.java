package ua.kiev.prog.photopond.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

@Aspect
@Component
public class DenyModifyUsersAspect {
    private static final Logger LOG = LogManager.getLogger(DenyModifyUsersAspect.class);

    private static final Set<String> predefinedUserLogin;
    private static final long MAX_UNMODIFIABLE_ID = 3;
    static {
        TreeSet<String> userLogin = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        userLogin.add("Admin");
        userLogin.add("User");
        predefinedUserLogin = Collections.unmodifiableSet(userLogin);
    }


    public DenyModifyUsersAspect() {
        LOG.debug("Create instance of " + this.getClass().getName());
    }

    @Around("execution(boolean ua.kiev.prog.photopond.user.UserInfoService.setNewPassword(*)) && args(userInfoDTO) ")
    public Object changePassword(ProceedingJoinPoint joinPoint, UserInfoDTO userInfoDTO) throws Throwable {
        LOG.debug("ASPECT change password: {}", userInfoDTO);
        if (predefinedUserLogin.contains(userInfoDTO.getLogin())) {
            LOG.warn("Cannot modify user password: {}", userInfoDTO.getLogin());
            return false;
        } else {
            LOG.debug("Call method to modify user password: {}", userInfoDTO.getLogin());
            return joinPoint.proceed(joinPoint.getArgs());
        }
    }

    @Around("execution(java.util.Optional<UserInfoDTO> ua.kiev.prog.photopond.user.UserInfoService.updateBaseInformation(*)) && args(userInfoDTO) ")
    public Object updateBaseInformation(ProceedingJoinPoint joinPoint, UserInfoDTO userInfoDTO) throws Throwable {
        LOG.debug("ASPECT update: {}", userInfoDTO);
        if (predefinedUserLogin.contains(userInfoDTO.getLogin())) {
            LOG.warn("Cannot modify user information: {}", userInfoDTO.getLogin());
            return Optional.empty();
        } else {
            LOG.debug("Call method to modify user password: {}", userInfoDTO.getLogin());
            return joinPoint.proceed(joinPoint.getArgs());
        }
    }

    @Around("execution(java.util.Optional<UserInfoDTO> ua.kiev.prog.photopond.user.UserInfoService.delete(*)) && args(id) ")
    public Object delete(ProceedingJoinPoint joinPoint, long id) throws Throwable {
        LOG.debug("ASPECT delete: {}", id);
        if (id <= MAX_UNMODIFIABLE_ID) {
            LOG.warn("Cannot delete user with id = {}", id);
            return Optional.empty();
        } else {
            LOG.debug("Call method to delete user with id = {}", id);
            return joinPoint.proceed(joinPoint.getArgs());
        }
    }
}
