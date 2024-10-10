package Burp;

import Burp.Application.Bypass.BaseRequest;
import Burp.Application.Bypass.GeneratePayload;
import Burp.Application.Bypass.RunRequest;
import Burp.Bootstrap.ThreadPool;
import Burp.UI.RootPanel;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static Burp.Bootstrap.CustomUtils.out;

/**
 * BypassProCMIP 类实现了 ContextMenuItemsProvider 接口，用于提供右键菜单项
 */
public class BypassProCMIP implements ContextMenuItemsProvider {
    private static ExecutorService executor;
    private final MontoyaApi api;
    private final RootPanel rootPanel;

    public BypassProCMIP(MontoyaApi montoyaApi, RootPanel rootPanel) {
        this.api = montoyaApi;
        this.rootPanel = rootPanel;
    }

    @Override
    public java.util.List<Component> provideMenuItems(ContextMenuEvent event) {
        if (!event.isFromTool(ToolType.PROXY, ToolType.REPEATER, ToolType.INTRUDER, ToolType.EXTENSIONS)) {
            return null;
        }

        java.util.List<Component> menuItemList = new ArrayList<>();
        JMenuItem retrieveSendItem = new JMenuItem("Send to BypassPro");

        retrieveSendItem.addActionListener(l -> {
            if (rootPanel.getControlPanel().isAutoClear()) {
                rootPanel.getControlPanel().clearScanResultPanel(rootPanel.getScanResultPanel());
            }

            int currentThreadNum = rootPanel.getControlPanel().getThreadNum();
            executor = ThreadPool.getThreadPool(currentThreadNum);

            java.util.List<HttpRequestResponse> requestResponses = event.messageEditorRequestResponse().isPresent() ? Collections.singletonList(event.messageEditorRequestResponse().get().requestResponse()) : event.selectedRequestResponses();
            for (HttpRequestResponse requestResponse : requestResponses) {
                HttpRequestResponse newRequestResponse = getRequestResponse(requestResponse);
                if (newRequestResponse != null) {
                    String old_path = newRequestResponse.request().pathWithoutQuery();
                    String old_request = newRequestResponse.request().toString();
                    String old_method = newRequestResponse.request().method();

                    List<BaseRequest> allRequests = new GeneratePayload(api).GetPayload(old_path);
                    out(api, "start thread, path: " + old_path);
                    this.rootPanel.getControlPanel().addAllRequestNum(allRequests.size());
                    for (BaseRequest baseRequest : allRequests) {
                        executor.submit(new RunRequest(this.api, this.rootPanel, baseRequest, old_path, old_request, old_method, newRequestResponse));
                    }
                }
            }
        });

        menuItemList.add(retrieveSendItem);
        return menuItemList;
    }

    /**
     * 处理未执行 send 的场景
     */
    public HttpRequestResponse getRequestResponse(HttpRequestResponse requestResponse) {
        final HttpRequestResponse[] newRequestResponse = {null};
        Thread thread = new Thread(() -> {
            try {
                newRequestResponse[0] = requestResponse.response() == null ? api.http().sendRequest(requestResponse.request()) : requestResponse;
            } catch (Exception e) {
                out(api, e.toString());
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            out(api, e.toString());
        }

        return newRequestResponse[0];
    }
}