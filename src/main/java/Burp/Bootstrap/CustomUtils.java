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
}