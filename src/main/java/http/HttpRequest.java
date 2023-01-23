package http;

import enums.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private final static Logger logger = LoggerFactory.getLogger(HttpRequest.class);
    private String method;
    private String path;
    private RequestLine requestLine;
    private Map<String, String> header = new HashMap<>();
    private Map<String, String> parameter = new HashMap<>();
    public HttpRequest(InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String firstLine = br.readLine();
            if (firstLine == null) return;

            requestLine = new RequestLine(firstLine);

            String line = br.readLine();
            while (null != line && !line.equals("")) {
                logger.debug("header : {}", line);
                String[] tokens = line.split(":");
                header.put(tokens[0].trim(), tokens[1].trim());
                line = br.readLine();
            }

            if (getMethod().isPost()) {
                String body = IOUtils.readData(br, Integer.parseInt(header.get("Content-Length")));
                parameter = HttpRequestUtils.parseQueryString(body);
            } else {
                parameter = requestLine.getParams();
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

    }
    public HttpMethod getMethod() {
//        return method;
        return requestLine.getMethod();
    }

    public String getPath() {
//        return path;
        return requestLine.getPath();
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public String getHeader(String name) {
        return header.get(name);
    }

    public Map<String, String> getParameter() {
        return parameter;
    }

    public String getParameter(String name) {
        return parameter.get(name);
    }

    public HttpCookie getCookies() {
        return new HttpCookie(getHeader("Cookie"));
    }
}
