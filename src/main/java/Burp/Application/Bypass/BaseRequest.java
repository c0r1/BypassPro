package Burp.Application.Bypass;

import java.util.Map;

public class BaseRequest {
    public BaseRequest.EditType editType;
    public String path;
    public Map<String, String> headers;

    public BaseRequest(EditType editType, String path, Map<String, String> headers) {
        this.editType = editType;
        this.path = path;
        this.headers = headers;
    }

    public String toString() {
        return editType + " : " + path;
    }

    public enum EditType {
        Method, Path, Header, Protocol
    }
}