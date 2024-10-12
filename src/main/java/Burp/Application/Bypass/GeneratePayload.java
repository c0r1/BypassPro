package Burp.Application.Bypass;

import Burp.Bootstrap.Config;
import Burp.Bootstrap.CustomUtils;
import Burp.Bootstrap.YamlReader;
import burp.api.montoya.MontoyaApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static Burp.Application.Bypass.GeneratePayload.PathType.*;

public class GeneratePayload {
    private final BaseRequest.EditType editType = BaseRequest.EditType.Path;
    private final YamlReader yamlReader;

    public GeneratePayload(MontoyaApi api) {
        yamlReader = YamlReader.getInstance(api);
    }

    /**
     * 生成请求负载
     *
     * @param path 请求路径
     * @return 请求负载列表
     */
    public List<BaseRequest> GetPayload(String path) {
        List<BaseRequest> allRequests = new ArrayList<>();
        List<Function<String, List<BaseRequest>>> functions = new ArrayList<>();
        functions.add(this::make_prefix);
        functions.add(this::make_suffix);
        functions.add(this::make_hierarchy);
        functions.add(this::make_header);
        functions.add(this::change_method);
        functions.add(this::downgrade_protocol);
        functions.add(this::asp_cookieless);

        functions.forEach(function -> allRequests.addAll(function.apply(path)));

        return allRequests;
    }

    /**
     * 生成 ASP Cookieless 请求负载
     *
     * @param path 请求路径
     * @return 请求负载列表
     */
    private List<BaseRequest> asp_cookieless(String path) {
        List<BaseRequest> cookielessList = new ArrayList<>();

        boolean isEnd = path.endsWith("/");
        String[] paths = path.startsWith("/") ? path.substring(1).split("/") : path.split("/");
        int pathsLen = paths.length;

        List<String> charToFillList = yamlReader.getStringList(Config.COOKIELESS);

        for (int i = 0; i < pathsLen; i++) {
            for (String charToFill : charToFillList) {
                StringBuilder[] hierarchyPaths = new StringBuilder[3];
                hierarchyPaths[0] = new StringBuilder("/");
                hierarchyPaths[1] = new StringBuilder("/");
                hierarchyPaths[2] = new StringBuilder("/");

                // 三种 Payload 构造形式
                for (int j = 0; j < pathsLen; j++) {
                    hierarchyPaths[0].append(i == j ? paths[j] + charToFill + "/" : paths[j] + "/");
                    hierarchyPaths[1].append(i == j ? insertMiddleStr(paths[j], charToFill) + "/" : paths[j] + "/");
                    hierarchyPaths[2].append(i == j ? charToFill.substring(1) + "/" + insertMiddleStr(paths[j], charToFill) + "/" : paths[j] + "/");

                }

                if (!isEnd) {
                    for (StringBuilder hierarchyPath : hierarchyPaths) {
                        hierarchyPath.deleteCharAt(hierarchyPath.length() - 1);
                    }
                }

                hierarchyPaths[1].append(charToFill);

                for (StringBuilder hierarchyPath : hierarchyPaths) {
                    cookielessList.add(new BaseRequest(editType, hierarchyPath.toString(), null));
                }
            }
        }

        return cookielessList;
    }

    /**
     * 在字符串中间插入另一个字符串
     *
     * @param original 原始字符串
     * @param toInsert 要插入的字符串
     * @return 插入后的字符串
     */
    private String insertMiddleStr(String original, String toInsert) {
        int insertIndex = original.length() / 2;

        return new StringBuilder(original).insert(insertIndex, toInsert).toString();
    }

    /**
     * 获取层级路径 POC
     *
     * @param path     跷径
     * @param pathType 跷径类型
     * @return 层级路径列表
     */
    private List<String> generateHierarchyPath(String path, PathType pathType) {
        List<String> resultPath = new ArrayList<>();
        boolean isEnd = path.endsWith("/");
        String[] paths = path.startsWith("/") ? path.substring(1).split("/") : path.split("/");
        int pathsLen = paths.length;

        List<String> charToFillList = switch (pathType) {
            case AFTER_HIERARCHY_PATH -> yamlReader.getStringList(Config.AFTER_HIERARCHY_PATH);
            case BETWEEN_HIERARCHY_PATH -> yamlReader.getStringList(Config.BETWEEN_HIERARCHY_PATH);
            case URL_ENCODE_HIERARCHY_PATH, CONVERT_CASE_HIERARCHY_PATH -> List.of(path);
        };

        for (int i = 0; i < pathsLen; i++) {
            if (pathsLen >= 1) {
                for (String charToFill : charToFillList) {
                    StringBuilder hierarchyPath = new StringBuilder("/");
                    if ((pathType == AFTER_HIERARCHY_PATH || pathType == BETWEEN_HIERARCHY_PATH) && !isEnd && i == pathsLen - 1) {
                        continue;
                    }

                    for (int j = 0; j < pathsLen; j++) {
                        if (yamlReader.getStringList(Config.DIRECTORY_TRAVERSAL_SYMBOLS).contains(charToFill)) {
                            switch (pathType) {
                                case AFTER_HIERARCHY_PATH:
                                    hierarchyPath.append(i == j ? paths[j] + charToFill + "/" + paths[j] + "/" : paths[j] + "/");
                                    break;
                                case BETWEEN_HIERARCHY_PATH:
                                    hierarchyPath.append(i == j ? paths[j] + "/" + charToFill + "/" + paths[j] + "/" : paths[j] + "/");
                                    break;
                            }
                        } else {
                            switch (pathType) {
                                case AFTER_HIERARCHY_PATH:
                                    hierarchyPath.append(i == j ? paths[j] + charToFill + "/" : paths[j] + "/");
                                    break;
                                case BETWEEN_HIERARCHY_PATH:
                                    hierarchyPath.append(i == j ? paths[j] + "/" + charToFill + "/" : paths[j] + "/");
                                    break;
                                case URL_ENCODE_HIERARCHY_PATH:
                                    hierarchyPath.append(i == j ? URLParamEncode(paths[j]) + "/" : paths[j] + "/");
                                    break;
                                case CONVERT_CASE_HIERARCHY_PATH:
                                    hierarchyPath.append(i == j ? convertToUpperCaseIgnoringNumbers(paths[j]) + "/" : paths[j] + "/");
                                    break;
                            }
                        }
                    }

                    if (!isEnd) {
                        hierarchyPath.deleteCharAt(hierarchyPath.length() - 1);
                    }

                    resultPath.add(hierarchyPath.toString());
                }
            }
        }

        return resultPath;
    }

    /**
     * 生成HTTP Headers列表
     *
     * @param headers 头部列表
     * @param value   头部值
     * @param path    跷径
     * @return HTTP头部列表
     */
    private List<BaseRequest> generateHeaderList(List<String> headers, String value, String path) {
        List<BaseRequest> headerList = new ArrayList<>();
        Map<String, String> headerGroup = new HashMap<>();

        for (String header : headers) {
            headerGroup.put(header, value);
            if (headerGroup.size() == yamlReader.getInteger(Config.DEFAULT_ADD_HEADERS_NUM)) {
                headerList.add(new BaseRequest(BaseRequest.EditType.Header, path, new HashMap<>(headerGroup)));
                headerGroup.clear();
            }
        }

        if (!headerGroup.isEmpty()) {
            headerList.add(new BaseRequest(BaseRequest.EditType.Header, path, new HashMap<>(headerGroup)));
        }

        return headerList;
    }

    /**
     * 生成前缀列表
     *
     * @param path 跷径
     * @return 前缀列表
     */
    private List<BaseRequest> make_prefix(String path) {
        List<BaseRequest> prefixList = new ArrayList<>();

        yamlReader.getStringList(Config.PREFIX_PAYLOAD).forEach(payload -> {
            String fullPath = payload + path;
            prefixList.add(new BaseRequest(editType, fullPath, null));
        });

        List<String> allowedAccessPaths = yamlReader.getStringList(Config.ALLOWED_ACCESS_PATHS);
        if (allowedAccessPaths != null && !allowedAccessPaths.isEmpty()) {
            allowedAccessPaths.stream().map(CustomUtils::removeTrailingSlash) // 清理每个路径
                    .flatMap(cleanAccessPath -> buildPrefixBasedAccessPaths(cleanAccessPath, path).stream()) // 构建前缀路径并扁平化结果
                    .forEach(prefixBasedAccessPath -> prefixList.add(new BaseRequest(editType, prefixBasedAccessPath, null)) // 创建BaseRequest对象并加入列表
                    );
        }

        return prefixList;
    }

    /**
     * 生成后缀列表
     *
     * @param path 跷径
     * @return 后缀列表
     */
    private List<BaseRequest> make_suffix(String path) {
        List<BaseRequest> suffixList = new ArrayList<>();

        yamlReader.getStringList(Config.SUFFIX_PAYLOAD).forEach(payload -> {
            String fullPath = path + payload;
            suffixList.add(new BaseRequest(editType, fullPath, null));
            if (path.endsWith("/")) {
                String pathWithoutSlash = path.substring(0, path.length() - 1);
                String fullPathWithoutSlash = pathWithoutSlash + payload;
                suffixList.add(new BaseRequest(editType, fullPathWithoutSlash, null));
            }
        });

        return suffixList;
    }

    /**
     * 生成层级列表
     *
     * @param path 跷径
     * @return 层级列表
     */
    private List<BaseRequest> make_hierarchy(String path) {
        List<BaseRequest> prefixList = new ArrayList<>();
        List<String> resultPath = new ArrayList<>();

        PathType[] pathTypes = {AFTER_HIERARCHY_PATH, BETWEEN_HIERARCHY_PATH, URL_ENCODE_HIERARCHY_PATH, CONVERT_CASE_HIERARCHY_PATH};

        for (PathType pathType : pathTypes) {
            resultPath.addAll(generateHierarchyPath(path, pathType));
        }

        resultPath.stream()
                .map(s -> new BaseRequest(editType, s, null))
                .forEach(prefixList::add);

        return prefixList;
    }

    /**
     * 生成 HTTP Header 列表
     *
     * @param path 跷径
     * @return HTTP Header 列表
     */
    private List<BaseRequest> make_header(String path) {
        List<BaseRequest> headerList = new ArrayList<>();
        headerList.addAll(generateHeaderList(yamlReader.getStringList(Config.FAKE_IP_HEADERS), yamlReader.getString(Config.FAKE_IP), path));
        headerList.addAll(generateHeaderList(yamlReader.getStringList(Config.FAKE_PATH_HEADERS), path, "/"));

        headerList.add(new BaseRequest(BaseRequest.EditType.Header, Config.FULL_URL_BYPASS, null));

        return headerList;
    }

    /**
     * 生成 HTTP Method 列表
     *
     * @param path 跷径
     * @return HTTP Method 列表
     */
    private List<BaseRequest> change_method(String path) {
        List<BaseRequest> methodList = new ArrayList<>();

        for (String method : yamlReader.getStringList(Config.METHODS)) {
            methodList.add(new BaseRequest(BaseRequest.EditType.Method, method, null));
        }

        return methodList;
    }

    /**
     * 生成 HTTP 协议降级列表
     *
     * @param path 跷径
     * @return HTTP 协议降级列表
     */
    private List<BaseRequest> downgrade_protocol(String path) {
        List<BaseRequest> protocolList = new ArrayList<>();

        for (String protocol : yamlReader.getStringList(Config.PROTOCOLS)) {
            protocolList.add(new BaseRequest(BaseRequest.EditType.Protocol, protocol, null));
        }

        return protocolList;
    }

    /**
     * 对URL参数进行编码
     *
     * @param input 输入的URL参数
     * @return 编码后的URL参数
     */
    private String URLParamEncode(String input) {
        StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            resultStr.append('%');
            resultStr.append(toHex(ch / 16));
            resultStr.append(toHex(ch % 16));
        }
        return resultStr.toString();
    }

    private char toHex(int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    /**
     * 将字符串转换为大写，忽略数字
     *
     * @param input 输入的字符串
     * @return 转换后的字符串
     */
    private String convertToUpperCaseIgnoringNumbers(String input) {
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isLetter(c)) {
                result.append(Character.toUpperCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 生成基于指定前缀路径的路径列表
     *
     * @param authFreePath 免认证路径
     * @param targetPath   目标路径
     * @return 生成的路径列表
     */
    private List<String> buildPrefixBasedAccessPaths(String authFreePath, String targetPath) {
        List<String> traversalSymbols = yamlReader.getStringList(Config.DIRECTORY_TRAVERSAL_SYMBOLS);
        int prefixCount = authFreePath.split("/").length - 1;

        return traversalSymbols.stream().map(symbol -> CustomUtils.repeatSymbolWithSlash(symbol, prefixCount)).map(traversalPrefix -> authFreePath + traversalPrefix + targetPath).collect(Collectors.toList());
    }

    public enum PathType {
        AFTER_HIERARCHY_PATH,
        BETWEEN_HIERARCHY_PATH,
        URL_ENCODE_HIERARCHY_PATH,
        CONVERT_CASE_HIERARCHY_PATH
    }
}
