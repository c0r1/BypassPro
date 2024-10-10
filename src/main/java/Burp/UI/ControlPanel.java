package Burp.UI;

import com.intellij.uiDesigner.core.GridConstraints;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;

import static Burp.Bootstrap.ThreadPool.clearThreadPool;

public class ControlPanel {
    private JButton clearButton;
    private JCheckBox autoClearCheckBox;
    private JLabel threadNumLabel;
    private JTextField threadNumText;
    private JTextField allRequestNumberText;
    private JTextField finishRequestNumberText;
    private JTextField errorRequestNumText;
    private JLabel allRequestNumberLabel;
    private JLabel finishNumberLabel;
    private JPanel controlPanel;
    private JLabel errorRequestNumLabel;

    public ControlPanel(JPanel rootPanel, ScanResultPanel scanResultPanel) {
        setupUI(rootPanel);

        clearButton.addActionListener(e -> {
            clearScanResultPanel(scanResultPanel);
        });
    }

    public void clearScanResultPanel(ScanResultPanel scanResultPanel) {
        if (getAllRequestNum() != (getFinishNum() + getErrorNum())) {
            // 清理线程池中未完成的任务
            clearThreadPool();
        }
        scanResultPanel.getBypassArray().clear();
        scanResultPanel.fireTableDataChanged();
        allRequestNumberText.setText("0");
        finishRequestNumberText.setText("0");
        errorRequestNumText.setText("0");
    }

    public boolean isAutoClear() {
        return autoClearCheckBox.isSelected();
    }

    public int getThreadNum() {
        return StringUtils.isBlank(threadNumText.getText()) ? 5 : Integer.parseInt(threadNumText.getText());
    }

    private int getAllRequestNum() {
        return Integer.parseInt(allRequestNumberText.getText());
    }

    private int getFinishNum() {
        return Integer.parseInt(finishRequestNumberText.getText());
    }

    private int getErrorNum() {
        return Integer.parseInt(errorRequestNumText.getText());
    }

    private void setAllRequestNumberText(int num) {
        allRequestNumberText.setText(String.valueOf(num));
    }

    public synchronized void addAllRequestNum(int num) {
        setAllRequestNumberText(getAllRequestNum() + num);
    }

    public synchronized void addFinishRequestNum(int num) {
        finishRequestNumberText.setText(String.valueOf(getFinishNum() + num));
    }

    public synchronized void addErrorRequestNum(int num) {
        errorRequestNumText.setText(String.valueOf(getErrorNum() + num));
    }

    private void setupUI(JPanel rootPanel) {
        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        controlPanel.putClientProperty("html.disable", Boolean.TRUE);
        rootPanel.add(controlPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        threadNumLabel = new JLabel("Thread Num: ", SwingConstants.RIGHT);
        threadNumLabel.putClientProperty("html.disable", Boolean.TRUE);
        controlPanel.add(threadNumLabel);
        threadNumText = new JTextField("1", 5);
        threadNumText.putClientProperty("html.disable", Boolean.TRUE);
        controlPanel.add(threadNumText);
        allRequestNumberLabel = new JLabel("All Request Num: ", SwingConstants.RIGHT);
        allRequestNumberLabel.putClientProperty("html.disable", Boolean.TRUE);
        controlPanel.add(allRequestNumberLabel);
        allRequestNumberText = new JTextField("0", 5);
        allRequestNumberText.setEditable(false);
        allRequestNumberText.putClientProperty("html.disable", Boolean.TRUE);
        controlPanel.add(allRequestNumberText);
        finishNumberLabel = new JLabel("Finish Num: ", SwingConstants.RIGHT);
        finishNumberLabel.putClientProperty("html.disable", Boolean.TRUE);
        controlPanel.add(finishNumberLabel);
        finishRequestNumberText = new JTextField("0", 5);
        finishRequestNumberText.setEditable(false);
        finishRequestNumberText.putClientProperty("html.disable", Boolean.TRUE);
        controlPanel.add(finishRequestNumberText);
        errorRequestNumLabel = new JLabel("Error Num: ", SwingConstants.RIGHT);
        controlPanel.add(errorRequestNumLabel);
        errorRequestNumText = new JTextField("0", 5);
        errorRequestNumText.setEditable(false);
        errorRequestNumText.putClientProperty("html.disable", Boolean.TRUE);
        controlPanel.add(errorRequestNumText);
        clearButton = new JButton("Clear");
        controlPanel.add(clearButton);
        autoClearCheckBox = new JCheckBox("AutoClear");
        autoClearCheckBox.setSelected(true);
        controlPanel.add(autoClearCheckBox);
        threadNumLabel.setLabelFor(threadNumText);
        allRequestNumberLabel.setLabelFor(allRequestNumberText);
        finishNumberLabel.setLabelFor(finishRequestNumberText);
        errorRequestNumLabel.setLabelFor(errorRequestNumText);
    }
}