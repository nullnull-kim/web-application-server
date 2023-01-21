package model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class HttpRequest {
    private final static Logger logger = LoggerFactory.getLogger(HttpRequest.class);
    private String method;
    private String path;
    private Map<String, String> header = new HashMap<>();
    private Map<String, String> parameter = new HashMap<>();
    private Function<String, String[]> function1 = (queries) -> queries.split("&");
    private Function<String, String[]> function2 = (queries) -> queries.split("=");
    public HttpRequest(InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String firstLine = br.readLine();
            if (firstLine == null) return;

            processRequestLine(firstLine);

            String line = br.readLine();
            while (null != line && !line.equals("")) {
                logger.debug("header : {}", line);
                String[] tokens = line.split(":");
                header.put(tokens[0].trim(), tokens[1].trim());
                line = br.readLine();
            }

            if ("POST".equals(method)) {
                String body = IOUtils.readData(br, Integer.parseInt(header.get("Content-Length")));
                parameter = HttpRequestUtils.parseQueryString(body);
            }

//            String[] split = firstLine.split(" ");
//            if (split.length != 3) return;
//
//            this.method = split[0];
//
//            String line = bf.readLine();
//            while (!"".equals(line)) {
//                if (null == line || "".equals(line)) break;
//                String[] headers = line.split(":");
//                this.header.put(headers[0].trim(), headers[1].trim());
//                line = bf.readLine();
//            }
//
//            if ("GET".equals(method)) {
//                setPathAndParameter(split[1]);
//            } else if ("POST".equals(method)) {
//                setParameter(bf.readLine());
//                this.path = split[1];
//            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

    }

    private void processRequestLine(String firstLine) {
        logger.debug("request line : {}", firstLine);
        String[] tokens = firstLine.split(" ");
        method = tokens[0];

        if ("POST".equals(method)) {
            path = tokens[1];
            return;
        }

        int index = tokens[1].indexOf("?");
        if (index == -1) {
            path = tokens[1];
        } else {
            path = tokens[1].substring(0, index);
            parameter = HttpRequestUtils.parseQueryString(tokens[1].substring(index + 1));
        }
    }

    private void setPathAndParameter(String path) {
        if(path.contains("?")) {
            String[] pathAndQuery = path.split("[?]");
            this.path = pathAndQuery[0];
            Arrays.asList(function1.apply(pathAndQuery[1])).stream()
                    .map(s -> function2.apply(s))
                    .forEach(query -> this.parameter.put(query[0], query[1]));
        }
        else this.path = path;
    }

    private void setParameter(String body) {
        if(body == null) return;
        Arrays.asList(function1.apply(body)).stream()
                .map(s -> function2.apply(s))
                .forEach(query -> this.parameter.put(query[0], query[1]));
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
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
}
