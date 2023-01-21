package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import db.DataBase;
import model.HttpRequest;
import model.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);
            String path = getDefaultPath(request.getPath());

            DataOutputStream dos = new DataOutputStream(out);
            if ("/user/create".equals(path)) {
                User user = new User(
                        request.getParameter("userId"),
                        request.getParameter("password"),
                        request.getParameter("name"),
                        request.getParameter("email")
                );
                logger.debug("user : {}", user);
                DataBase.addUser(user);
                response.sendRedirect("/index.html");
            } else if ("/user/login".equals(path)) {
                User user = DataBase.findUserById(request.getParameter("userId"));
                if (user == null) {
                    response.sendRedirect("/user/login_failed.html");
                } else if (user.getPassword().equals(request.getParameter("password"))) {
                    response.addHeader("Set-Cookie", "logined=true");
                    response.sendRedirect("/index.html");
                } else {
                    response.sendRedirect("/user/login_failed.html");
                }
            } else if ("/user/list".equals(path)) {
                if (!isLogin(request.getHeader("Cookie"))) {
                    response.sendRedirect("/user/login.html");
                    return;
//                    responseResource(out, "/user/login.html");
                }
                Collection<User> users = DataBase.findAll();
                StringBuilder sb = new StringBuilder();
                sb.append("<table border='1'>");
                for (User user : users) {
                    sb.append("<tr>");
                    sb.append("<td>" + user.getUserId() + "</td>");
                    sb.append("<td>" + user.getName() + "</td>");
                    sb.append("<td>" + user.getEmail() + "</td>");
                    sb.append("</tr>");
                }
                response.forwardBody(sb.toString());
            } else {
                response.forward(path);
            }
//
//            BufferedReader br = new BufferedReader(new InputStreamReader(in));
//            String line = br.readLine();
//            if (line == null) return;
//
//            String url = HttpRequestUtils.getUrl(line);
//            byte[] body;
//
//            Map<String, String> headers = new HashMap<>();
//            while (!"".equals(line)) {
//                logger.debug("header : {}", line);
//                line = br.readLine();
//                String[] headerTokens = line.split(": ");
//                if(headerTokens.length == 2) headers.put(headerTokens[0], headerTokens[1]);
//            }
//
//            if (url.startsWith("/user/create")) {
//                String requestBody = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
//                logger.debug("requestBody : {}", requestBody);
//                Map<String, String> params = HttpRequestUtils.parseQueryString(requestBody);
//                User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
//                DataBase.addUser(user);
//                response302Header(dos);
//            } else if (url == null || url.equals("/") || url.equals("")){
//                body = "Hello World".getBytes();
//                response200Header(dos, body.length);
//                responseBody(dos, body);
//            } else if (url.equals("/user/login")) {
//                String requestBody = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
//                logger.debug("requestBody : {}", requestBody);
//                loginProcess(dos, requestBody);
//            } else if (url.endsWith(".css")){
//                body = Files.readAllBytes(new File("./webapp" + url).toPath());
//                response200CSSHeader(dos, body.length);
//                responseBody(dos, body);
//            } else if (url.startsWith("/user/list")) {
//                String cookieHeader = headers.get("Cookie");
//                Map<String, String> cookies = HttpRequestUtils.parseCookies(cookieHeader);
//                String loginedCookie = cookies.get("logined");
//                if (Boolean.parseBoolean(loginedCookie)) {
//                    StringBuilder sb = new StringBuilder();
//                    sb.append("<h1>user list</h1>");
//                    Iterator<User> iterator = DataBase.findAll().iterator();
//                    while (iterator.hasNext()) {
//                        User next = iterator.next();
//                        sb.append("<p>name : " + next.getName() + "</p>");
//                        sb.append("<p>email : " + next.getEmail() + "</p>");
//                    }
//                    response200Header(dos, sb.toString().getBytes().length);
//                    responseBody(dos, sb.toString().getBytes());
//                } else {
//                    logger.error("error! /user/list -> user not login");
//                    response302HeaderWithLocation(dos, null);
//                }
//                body = Files.readAllBytes(new File("./webapp" + url).toPath());
//                response200Header(dos, body.length);
//                responseBody(dos, body);
//            } else {
//                body = Files.readAllBytes(new File("./webapp" + url).toPath());
//                response200Header(dos, body.length);
//                responseBody(dos, body);
//            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void responseCssResource(OutputStream out, String path) {
        try {
            byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
            DataOutputStream dos = new DataOutputStream(out);
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css \r\n");
            dos.writeBytes("Content-Length: " + body.length + "\r\n");
            dos.writeBytes("\r\n");
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void responseResource(OutputStream out, String resource) {
        if(resource == null) resource = "/index.html";
        try {
            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = Files.readAllBytes(new File("./webapp" + resource).toPath());
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private boolean isLogin(String cookie) {
        Map<String, String> cookies = HttpRequestUtils.parseCookies(cookie);
        String value = cookies.get("logined");
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    private String getDefaultPath(String path) {
        if (path.equals("/")) {
            return "/index.html";
        }
        return path;
    }

    private void loginProcess(DataOutputStream dos, String requestBody) {
        Map<String, String> params = HttpRequestUtils.parseQueryString(requestBody);
        String userId = params.get("userId");
        String password = params.get("password");
        User user = DataBase.findUserById(userId);

        if (user == null) {
            logger.error("id not found");
            response404Header(dos);
        } else if (user.getPassword().equals(password)) {
            logger.debug("login success");
            response302HeaderWithCookie(dos, null, "logined=true");
        } else {
            logger.error("password not matched");
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
            logger.error(e.getMessage());
        }
    }

    private void response200CSSHeader(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css \r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            logger.error(e.getMessage());
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
            logger.error(e.getMessage());
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
            logger.error(e.getMessage());
        }
    }

    private void response302HeaderWithCookie(DataOutputStream dos, String location, String cookie) {
        try {
            this.response302HeaderWithLocation(dos, location);
            dos.writeBytes("Set-Cookie: " + cookie + " \r\n");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void response404Header(DataOutputStream dos) {
        try {
            // https://stackoverflow.com/questions/2769371/404-header-http-1-0-or-1-1
            dos.writeBytes("HTTP/1.1 404 Not Found \r\n");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void response401Header(DataOutputStream dos) {
        try {
            // https://developer.mozilla.org/ko/docs/Web/HTTP/Status/401
            dos.writeBytes("HTTP/1.1 401 Unauthorized \r\n");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
