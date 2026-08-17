package net.swordie.webapi.routes;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import net.swordie.ms.client.User;
import net.swordie.orm.dao.SworDaoFactory;
import net.swordie.orm.dao.UserDao;
import net.swordie.webapi.modules.UserModule;
import net.swordie.webapi.protocol.request.ResetRequest;

@Path("/reset-password-request")
public class PasswordResetRequestRoute {

    private static final UserDao userDao = (UserDao) SworDaoFactory.getByClass(User.class);

    @POST
    @Path("")
    @Consumes({MediaType.APPLICATION_JSON})
    public void resetPassword(ResetRequest request) {
        request.validate();

        var user = userDao.getByName(request.username);
        if (user == null) {
            return; // So people can't find account names using this method
        }

        UserModule.sendPasswordResetEmail(user);
    }

}
