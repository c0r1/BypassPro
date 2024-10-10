package Burp.Application.Bypass;

import Burp.Bootstrap.Config;
import Burp.Bootstrap.DiffPage;
import Burp.UI.RootPanel;
import Burp.UI.ScanResultPanel;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Burp.Bootstrap.CustomUtils.*;

public class RunRequest implements Runnable {
    private final BaseRequest baseRequest;
    private final String old_path;
    private final String old_request;
    private final String old_method;
    private final HttpRequestResponse requestResponse;
    private final RootPanel rootPanel;
    private final MontoyaApi api;

    public RunRequest(MontoyaApi montoyaApi, RootPanel rootPanel, BaseRequest baseRequest, String old_path, String old_request, String old_method, HttpRequestResponse httpRequestResponse) {
        this.api = montoyaApi;
        this.rootPanel = rootPanel;
        this.baseRequest = baseRequest;
        this.old_method = old_method;
        this.old_path = old_path;
        this.old_request = old_request;
        this.requestResponse = httpRequestResponse;
    }

    private void addFinishRequestNum() {
        this.rootPanel.getControlPanel().addFinishRequestNum(1);
    }

    private void addErrorRequestNum() {
        this.rootPanel.getControlPanel().addErrorRequestNum(1);
    }

    @Override
    public void run() {
        StringBuilder new_request = new StringBuilder();
        String path = baseRequest.path;
        Map<String, String> headers = baseRequest.headers;

        switch (baseRequest.editType) {
            case Path:
                new_request.append(old_request.replaceFirst(old_path, path));
                break;
            case Protocol:
                String[] lines = old_request.split("\r\n|\r|\n", 3);
                new_request.append(lines[0].replaceFirst("HTTP/.+", path)).append("\r\n\r\n");
                break;
            case Method:
                HttpRequest httpRequest = this.requestResponse.request();
                if (path.equals("TOGGLE_METHOD")) {
                    new_request.append(toPostMethod(httpRequest).toString());
                    if (!Objects.equals(old_method, "GET")) {
                        new_request.setLength(0); // Clear the builder
                        new_request.append(toGetMethod(httpRequest).toString());
                    }
                } else {
                    new_request.append(toPostMethod(httpRequest).toString().replaceFirst("POST", path));
                }
                break;
            case Header:
                if (Config.FULL_URL_BYPASS.equals(path)) {
                    new_request.append(old_request.replaceFirst(old_path, requestResponse.request().url()));
                } else {
                    HttpRequest oldRequest = HttpRequest.httpRequest(requestResponse.httpService(), old_request.replaceFirst(old_path, path));
                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                        oldRequest = oldRequest.withHeader(entry.getKey(), entry.getValue());
                    }
                    new_request.append(oldRequest.toString());
                }
                break;
        }

        try {
            HttpRequestResponse newRequestResponse = api.http().sendRequest(HttpRequest.httpRequest(requestResponse.httpService(), new_request.toString()));

            String oldResponse = requestResponse.response().bodyToString();
            String newResponse = newRequestResponse.response().bodyToString();
            short oldStatusCode = requestResponse.response().statusCode();
            short newStatusCode = newRequestResponse.response().statusCode();

            if (Thread.currentThread().isInterrupted()) {
                return;
            } else if (!Objects.equals(newRequestResponse.response().toString(), "") && (oldStatusCode != newStatusCode || DiffPage.getRatio(oldResponse, newResponse) < 0.8)) {
                String title = getBodyTitle(newRequestResponse.response().bodyToString());
                addLog(newRequestResponse, 0, title);
            }
            addFinishRequestNum();
        } catch (Throwable ee) {
            out(api, ee.toString());
//            ee.printStackTrace();
            addErrorRequestNum();
        }
    }

    private void addLog(HttpRequestResponse messageInfo, int row, String title) {
        ScanResultPanel.Bypass bypass = new ScanResultPanel.Bypass(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()),
                messageInfo.request().method(),
                messageInfo.response().toString().length(),
                messageInfo,
                messageInfo.request().url(),
                messageInfo.response().statusCode(),
                messageInfo.response().mimeType().toString(),
                title
        );

        // 向 Burp 面板中添加请求记录
        this.rootPanel.getScanResultPanel().add(bypass);
    }

    /**
     * 获取页面标题
     *
     * @param s 页面内容
     * @return 页面标题
     */
    private String getBodyTitle(String s) {
        String regex;
        StringBuilder title = new StringBuilder();
        final List<String> list = new ArrayList<>();
        regex = "<title>.*?</title>";
        final Pattern pa = Pattern.compile(regex, Pattern.CANON_EQ);
        final Matcher ma = pa.matcher(s);
        while (ma.find()) {
            list.add(ma.group());
        }

        for (String string : list) {
            title.append(string);
        }

        return title.toString().replaceAll("<.*?>", "");
    }
}