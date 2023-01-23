package util;

import enums.HttpMethod;
import http.HttpCookie;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class RequestLineTest {
    @Test
    public void create_method() {
        HttpCookie.RequestLine line = new HttpCookie.RequestLine("GET /index.html HTTP/1.1");
        Assert.assertEquals(HttpMethod.GET, line.getMethod());
        Assert.assertEquals("/index.html", line.getPath());

        line = new HttpCookie.RequestLine("POST /index.html HTTP/1.1");
        Assert.assertEquals("/index.html", line.getPath());
    }

    @Test
    public void create_path_and_params() {
        HttpCookie.RequestLine line = new HttpCookie.RequestLine("GET /user/create?userId=javajigi&password=pass HTTP/1.1");
        Assert.assertEquals(HttpMethod.GET, line.getMethod());
        Assert.assertEquals("/user/create", line.getPath());
        Map<String, String> params = line.getParams();
        Assert.assertEquals(2, params.size());
    }
}
