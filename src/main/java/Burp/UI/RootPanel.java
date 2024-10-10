package Burp.UI;

import burp.api.montoya.MontoyaApi;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import java.awt.*;

public class RootPanel extends JPanel {
    private final JPanel rootPanel;
    private final ControlPanel controlPanel;
    private final ScanResultPanel scanResultPanel;
    private final MontoyaApi api;

    public RootPanel(MontoyaApi montoyaApi) {
        api = montoyaApi;

        // 主面板
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), 0, 0));
        rootPanel.putClientProperty("html.disable", Boolean.TRUE);

        // 扫描队列窗口
        scanResultPanel = new ScanResultPanel(api, rootPanel);

        // 设置面板
        controlPanel = new ControlPanel(rootPanel, scanResultPanel);
    }

    public JComponent getRootComponent() {
        return this.rootPanel;
    }

    /**
     * 基础设置面板
     *
     * @return
     */
    public ControlPanel getControlPanel() {
        return this.controlPanel;
    }

    /**
     * 扫描队列窗口
     * 可通过该类提供的方法,进行扫描任务的添加与修改
     *
     * @return
     */
    public ScanResultPanel getScanResultPanel() {
        return this.scanResultPanel;
    }
}
