package controller;

import db.DataBase;
import http.HttpCookie;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateUserController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(CreateUserController.class);
    @Override
    protected void doPost(HttpCookie.HttpRequest request, HttpCookie.HttpResponse response) {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpCookie.HttpRequest request, HttpCookie.HttpResponse response) {
        User user = new User(request.getParameter("userId"),
                request.getParameter("password"),
                request.getParameter("name"),
                request.getParameter("email"));
        logger.debug("user : {}", user);
        DataBase.addUser(user);
        response.sendRedirect("/index.html");
    }
}
