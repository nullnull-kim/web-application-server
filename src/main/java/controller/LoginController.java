package controller;

import db.DataBase;
import http.HttpCookie;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginController extends AbstractController {
    private final static Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Override
    protected void doPost(HttpCookie.HttpRequest request, HttpCookie.HttpResponse response) {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpCookie.HttpRequest request, HttpCookie.HttpResponse response) {
        User user = DataBase.findUserById(request.getParameter("userId"));
        if (user == null) {
            response.sendRedirect("/user/login_failed.html");
        } else if (user.getPassword().equals(request.getParameter("password"))) {
            response.addHeader("Set-Cookie", "logined=true");
            response.sendRedirect("/index.html");
        } else {
            response.sendRedirect("/user/login_failed.html");
        }
    }
}
