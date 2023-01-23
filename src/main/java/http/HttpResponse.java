package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HttpResponse {
    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);
    private Map<String, String> header = new HashMap<>();

    private DataOutputStream dos = null;

    public HttpResponse(OutputStream outputStream) {
        dos = new DataOutputStream(outputStream);
    }

    public void addHeader(String key, String value) {
        header.put(key, value);
    }

    public void forward(String file) {
        try {
            byte[] body = Files.readAllBytes(new File("./webapp" + file).toPath());
            if (file.endsWith(".css")) {
                header.put("Content-Type", "text/css");
            } else if (file.endsWith(".js")) {
                header.put("Content-Type", "application/javascript");
            } else {
                header.put("Content-Type", "text/html;charset=utf-8");
            }
            header.put("Content-Length", String.valueOf(body.length));
            response200Header(body.length);
            responseBody(body);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public void forwardBody(String body) {
        byte[] contents = body.getBytes();
        header.put("Content-Type", "text/html;charset=utf-8");
        header.put("Content-Length", String.valueOf(contents.length));
        response200Header(contents.length);
        responseBody(contents);
    }

    public void sendRedirect(String redirectUrl) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found" + System.lineSeparator());
            processHeaders();
            dos.writeBytes("Location: " + redirectUrl + System.lineSeparator());
            dos.writeBytes(System.lineSeparator());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void response200Header(int lengthOfBodyContent) {
        try {
            // \n - unix
            // \r - mac
            // \r\n - windows
            // 한가지를 사용하면 시스템에 따라서 줄바꿈이 되지 않을 수도 있습니다.
            // https://hianna.tistory.com/602
            dos.writeBytes("HTTP/1.1 200 OK" + System.lineSeparator());
            processHeaders();
            dos.writeBytes(System.lineSeparator());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void responseBody(byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.writeBytes(System.lineSeparator());
            dos.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void processHeaders() {
        try {
            Set<String> keys = header.keySet();
            for (String key : keys) {
                dos.writeBytes(key + ": " + header.get(key) + System.lineSeparator());
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
