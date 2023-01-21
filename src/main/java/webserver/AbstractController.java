package webserver;

import model.HttpRequest;
import model.HttpResponse;

public abstract class AbstractController implements Controller{
    abstract void doPost(HttpRequest request, HttpResponse response);
    abstract void doGet(HttpRequest request, HttpResponse response);
}
