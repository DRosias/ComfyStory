package net.swordie.webapi.routes;

import com.sun.net.httpserver.HttpExchange;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import net.swordie.ms.Server;
import net.swordie.ms.ServerConstants;
import net.swordie.ms.client.User;
import net.swordie.orm.dao.SworDaoFactory;
import net.swordie.orm.dao.UserDao;
import net.swordie.webapi.protocol.result.LoginResult;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Path("/launcher")
public class LauncherAuthRoute {
    private static final int MAX_USERNAME_LENGTH = 255;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 255;
    private static final Object REGISTRATION_LOCK = new Object();

    private static final UserDao userDao = (UserDao) SworDaoFactory.getByClass(User.class);

    @POST
    @Path("/account-status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public AccountStatusResult accountStatus(AccountStatusRequest request) {
        String username = normalizeAndValidateUsername(request == null ? null : request.username);
        return new AccountStatusResult(userDao.getByName(username) != null);
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public LoginResult register(@Context HttpExchange exchange, RegisterRequest request) {
        String username = normalizeAndValidateUsername(request == null ? null : request.username);
        String password = request == null ? null : request.password;
        validatePassword(password);

        synchronized (REGISTRATION_LOCK) {
            if (userDao.getByName(username) != null) {
                throw new BadRequestException("Username already taken.");
            }

            User user = new User(username);
            user.setPasswordAndHash(password);
            user.setCharacterSlots(ServerConstants.MAX_CHARACTERS);
            user.setRegisterIp(getClientIp(exchange));
            userDao.saveOrUpdate(user, null);

            byte[] token = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
            Server.getInstance().addAuthToken(token, user.getId(), user.isPlayer());
            return new LoginResult(token, user);
        }
    }

    static String normalizeAndValidateUsername(String username) {
        if (username == null) {
            throw new BadRequestException("Missing username.");
        }
        String normalized = username.trim();
        if (normalized.length() < 4 || normalized.length() > MAX_USERNAME_LENGTH) {
            throw new BadRequestException("Username must be between 4 and 255 characters.");
        }
        return normalized;
    }

    static void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            throw new BadRequestException("Password must be between 6 and 255 characters.");
        }
    }

    private static String getClientIp(HttpExchange exchange) {
        if (exchange == null) {
            return null;
        }
        String forwardedFor = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",", 2)[0].trim();
        }
        return exchange.getRemoteAddress() == null || exchange.getRemoteAddress().getAddress() == null
                ? null
                : exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    public static class AccountStatusRequest {
        public String username;
    }

    public static class RegisterRequest {
        public String username;
        public String password;
    }

    public static class AccountStatusResult {
        public boolean exists;

        public AccountStatusResult() {
        }

        public AccountStatusResult(boolean exists) {
            this.exists = exists;
        }
    }
}
