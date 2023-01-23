package controller;

import enums.HttpMethod;
import http.HttpCookie;

public abstract class AbstractController implements Controller{

    @Override
    public void service(HttpCookie.HttpRequest request, HttpCookie.HttpResponse response) {
        HttpMethod method = request.getMethod();
        if (method.isPost()) {
            doPost(request, response);
        } else {
            doGet(request, response);
        }
    }

    protected void doPost(HttpCookie.HttpRequest request, HttpCookie.HttpResponse response){}

    protected void doGet(HttpCookie.HttpRequest request, HttpCookie.HttpResponse response){}
}
