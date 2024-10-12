package Burp.Bootstrap;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.requests.HttpTransformation;

public class CustomUtils {
    private static boolean gotBurp = false;

    public static void setBurpPresent() {
        gotBurp = true;
    }

    /**
     * 输出日志
     *
     * @param message 要输出的信息
     */
    public static void out(MontoyaApi api, String message) {
        if (gotBurp) {
            api.logging().logToOutput(message);
        } else {
            System.out.println(message);
        }
    }

    /**
     * 将请求转换为POST方法
     *
     * @param request 输入的请求
     * @return 转换后的请求
     */
    public static HttpRequest toPostMethod(HttpRequest request) {
        if (!"POST".equalsIgnoreCase(request.method())) {
            request = request.withTransformationApplied(HttpTransformation.TOGGLE_METHOD);
            // PUT OPTIONS 等会转换为 GET, 需要再次处理
            if ("GET".equalsIgnoreCase(request.method())) {
                request = request.withTransformationApplied(HttpTransformation.TOGGLE_METHOD);
            }
        }

        return request;
    }

    /**
     * 将请求转换为GET方法
     *
     * @param request 输入的请求
     * @return 转换后的请求
     */
    public static HttpRequest toGetMethod(HttpRequest request) {
        if (!"GET".equalsIgnoreCase(request.method())) {
            request = request.withTransformationApplied(HttpTransformation.TOGGLE_METHOD);
        }

        return request;
    }

    /**
     * 移除路径字符串末尾的斜杠（如果存在）
     *
     * @param path 路径字符串
     * @return 处理后的路径字符串
     */
    public static String removeTrailingSlash(String path) {
        return (path != null && path.endsWith("/")) ? path.substring(0, path.length() - 1) : path;
    }

    /**
     * 重复给定的字符串，并在每次重复之前添加一个斜杠
     *
     * @param symbol 要重复的字符串
     * @param count  重复次数
     * @return 重复后的字符串
     */
    public static String repeatSymbolWithSlash(String symbol, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Repeat count must be non-negative.");
        }
        StringBuilder repeatedSymbol = new StringBuilder();
        for (int i = 0; i < count; i++) {
            repeatedSymbol.append('/').append(symbol);
        }
        return repeatedSymbol.toString();
    }
}