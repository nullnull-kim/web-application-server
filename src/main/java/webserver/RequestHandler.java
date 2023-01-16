package webserver;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = br.readLine();
            if (line == null) return;

            String url = HttpRequestUtils.getUrl(line);
            byte[] body;

            Map<String, String> headers = new HashMap<>();
            while (!"".equals(line)) {
                log.debug("header : {}", line);
                line = br.readLine();
                String[] headerTokens = line.split(": ");
                if(headerTokens.length == 2) headers.put(headerTokens[0], headerTokens[1]);
            }
            DataOutputStream dos = new DataOutputStream(out);
            if (url.startsWith("/user/create")) {
                String requestBody = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
                log.debug("requestBody : {}", requestBody);
                Map<String, String> params = HttpRequestUtils.parseQueryString(requestBody);
                User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
                DataBase.addUser(user);
                response302Header(dos);
            } else if (url == null || url.equals("/") || url.equals("")){
                body = "Hello World".getBytes();
                response200Header(dos, body.length);
                responseBody(dos, body);
            } else if (url.equals("/user/login")) {
                String requestBody = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
                log.debug("requestBody : {}", requestBody);
                loginProcess(dos, requestBody);
            } else if (url.endsWith(".css")){
                body = Files.readAllBytes(new File("./webapp" + url).toPath());
                response200CSSHeader(dos, body.length);
                responseBody(dos, body);
            } else if (url.startsWith("/user/list")) {
                String cookieHeader = headers.get("Cookie");
                Map<String, String> cookies = HttpRequestUtils.parseCookies(cookieHeader);
                String loginedCookie = cookies.get("logined");
                if (Boolean.parseBoolean(loginedCookie)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<h1>user list</h1>");
                    Iterator<User> iterator = DataBase.findAll().iterator();
                    while (iterator.hasNext()) {
                        User next = iterator.next();
                        sb.append("<p>name : " + next.getName() + "</p>");
                        sb.append("<p>email : " + next.getEmail() + "</p>");
                    }
                    response200Header(dos, sb.toString().getBytes().length);
                    responseBody(dos, sb.toString().getBytes());
                } else {
                    log.error("error! /user/list -> user not login");
                    response302HeaderWithLocation(dos, null);
                }
                body = Files.readAllBytes(new File("./webapp" + url).toPath());
                response200Header(dos, body.length);
                responseBody(dos, body);
            } else {
                body = Files.readAllBytes(new File("./webapp" + url).toPath());
                response200Header(dos, body.length);
                responseBody(dos, body);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void loginProcess(DataOutputStream dos, String requestBody) {
        Map<String, String> params = HttpRequestUtils.parseQueryString(requestBody);
        String userId = params.get("userId");
        String password = params.get("password");
        User user = DataBase.findUserById(userId);

        if (user == null) {
            log.error("id not found");
            response404Header(dos);
        } else if (user.getPassword().equals(password)) {
            log.debug("login success");
            response302HeaderWithCookie(dos, null, "logined=true");
        } else {
            log.error("password not matched");
            response401Header(dos);
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200CSSHeader(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css \r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos) {
        try {
            // https://en.wikipedia.org/wiki/HTTP_302
            // HTTP/1.1 302 Found
            // Location: http://www.iana.org/domains/example/
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: /index.html \r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302HeaderWithLocation(DataOutputStream dos, String location) {
        if(location == null) location = "/index.html";
        try {
            // https://en.wikipedia.org/wiki/HTTP_302
            // HTTP/1.1 302 Found
            // Location: http://www.iana.org/domains/example/
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: "+ location +" \r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302HeaderWithCookie(DataOutputStream dos, String location, String cookie) {
        try {
            this.response302HeaderWithLocation(dos, location);
            dos.writeBytes("Set-Cookie: " + cookie + " \r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response404Header(DataOutputStream dos) {
        try {
            // https://stackoverflow.com/questions/2769371/404-header-http-1-0-or-1-1
            dos.writeBytes("HTTP/1.1 404 Not Found \r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response401Header(DataOutputStream dos) {
        try {
            // https://developer.mozilla.org/ko/docs/Web/HTTP/Status/401
            dos.writeBytes("HTTP/1.1 401 Unauthorized \r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
