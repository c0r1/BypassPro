package Burp;

import Burp.UI.RootPanel;
import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.extension.ExtensionUnloadingHandler;

import static Burp.Bootstrap.CustomUtils.setBurpPresent;
import static Burp.Bootstrap.ThreadPool.clearThreadPool;

public class BypassPro implements BurpExtension, ExtensionUnloadingHandler {
    private final String name = "BypassPro";
    private final String version = "1.5.0";
    private MontoyaApi api;
    private RootPanel rootPanel;

    @Override
    public void initialize(MontoyaApi montoyaApi) {
        api = montoyaApi;
        api.extension().setName(name);
        api.extension().registerUnloadingHandler(this);
        api.logging().logToOutput("=======================================");
        api.logging().logToOutput("[+]          load successful!          ");
        api.logging().logToOutput("[+]           BypassPro v" + version);
        api.logging().logToOutput("[+]            code by c0r1            ");
        api.logging().logToOutput("=======================================");

        setBurpPresent();
        rootPanel = new RootPanel(api);
        api.userInterface().registerSuiteTab(name, rootPanel.getRootComponent());
        api.userInterface().registerContextMenuItemsProvider(new BypassProCMIP(api, rootPanel));
    }

    @Override
    public void extensionUnloaded() {
        clearThreadPool();
        rootPanel.getScanResultPanel().getBypassArray().clear();
        rootPanel.removeAll();
        api.logging().logToOutput("[*] Extension has been unloaded.");
    }
}