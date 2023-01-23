package controller;

import http.HttpCookie;

public interface Controller {
    void service(HttpCookie.HttpRequest request, HttpCookie.HttpResponse response);
}
