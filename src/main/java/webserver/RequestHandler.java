package webserver;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
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

            String url = HttpRequestUtils.getUrl(line);
            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
            DataOutputStream dos = new DataOutputStream(out);
            response200Header(dos, body.length);
            responseBody(dos, body);

            /*while (!"".equals(line)) {
                log.debug("header : {}", line);
                line = br.readLine();
                if (line == null) return;
            }*/

//            for (; ; ) {
//                String bufferedReaderLine = br.readLine();
//                if(br == null || bufferedReaderLine.equals("")) return;
//                System.out.println("bufferedReaderLine = " + bufferedReaderLine);
//                if (bufferedReaderLine.startsWith("POST") || bufferedReaderLine.startsWith("GET")) {
//                    String[] tokens = bufferedReaderLine.split(" ");
//                    String method = tokens[0];
//                    String urlNQuery = tokens[1];
//                    String url = urlNQuery.split("[?]")[0];
//                    if (url.equals("/user/create") && method.equals("GET")) {
//                        if (urlNQuery.contains("?")) {
//                            String query = urlNQuery.split("[?]")[1];
//                            Map<String, String> keyAndValue = HttpRequestUtils.parseQueryString(query);
//                            String userId = keyAndValue.get("userId");
//                            String password = keyAndValue.get("password");
//                            String name = keyAndValue.get("name");
//                            String email = keyAndValue.get("email");
//                            User user = new User(userId, password, name, email);
//                            DataBase.addUser(user);
//                        }
//                        DataOutputStream dos = new DataOutputStream(out);
//                        byte[] body = "Hello World".getBytes();
//                        response200Header(dos, body.length);
//                        responseBody(dos, body);
//                    } else {
//                        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
//                        DataOutputStream dos = new DataOutputStream(out);
//                        response200Header(dos, body.length);
//                        responseBody(dos, body);
//                    }
//                }else {
////                    DataOutputStream dos = new DataOutputStream(out);
////                    byte[] body = "Hello World".getBytes();
////                    response200Header(dos, body.length);
////                    responseBody(dos, body);
//                }
//            }
        } catch (IOException e) {
            log.error(e.getMessage());
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

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
