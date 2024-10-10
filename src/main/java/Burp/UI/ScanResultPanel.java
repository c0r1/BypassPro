package Burp.UI;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;
import com.intellij.uiDesigner.core.GridConstraints;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static burp.api.montoya.ui.editor.EditorOptions.READ_ONLY;

public class ScanResultPanel extends AbstractTableModel {
    private final List<ScanResultPanel.Bypass> bypassArray = new ArrayList<>();
    private final HttpRequestEditor requestViewer;
    private final HttpResponseEditor responseViewer;
    private final ScanResultPanel.BypassTable bypassTable;
    private final JTabbedPane requestPane;
    private final JTabbedPane responsePane;
    private final JSplitPane ScanResultPane;
    private final JSplitPane RequestResponsePane;
    private final JScrollPane ScanQueuePane;

    public ScanResultPanel(MontoyaApi api, JPanel rootPanel) {
        requestViewer = api.userInterface().createHttpRequestEditor(READ_ONLY);
        responseViewer = api.userInterface().createHttpResponseEditor(READ_ONLY);

        // 扫描结果面板
        ScanResultPane = new JSplitPane();
        ScanResultPane.setOrientation(0);
        rootPanel.add(ScanResultPane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(200, 200), null, 0, false));
        ScanQueuePane = new JScrollPane();
        ScanQueuePane.setBackground(Color.decode("#FF0000"));
        ScanQueuePane.putClientProperty("html.disable", Boolean.TRUE);
        ScanResultPane.setLeftComponent(ScanQueuePane);

        // 扫描队列窗口
        bypassTable = new ScanResultPanel.BypassTable(ScanResultPanel.this);
        bypassTable.setFillsViewportHeight(false);
        bypassTable.setShowHorizontalLines(true);
        bypassTable.putClientProperty("html.disable", Boolean.TRUE);
        ScanQueuePane.setViewportView(bypassTable);

        // 请求显示窗口
        RequestResponsePane = new JSplitPane();
        RequestResponsePane.setContinuousLayout(false);
        RequestResponsePane.setDividerLocation(735);
        RequestResponsePane.setDividerSize(5);
        RequestResponsePane.setResizeWeight(0.5);
        RequestResponsePane.putClientProperty("html.disable", Boolean.TRUE);
        ScanResultPane.setRightComponent(RequestResponsePane);

        // Burp Request 请求页面
        requestPane = new JTabbedPane();
        requestPane.setTabLayoutPolicy(1);
        requestPane.setTabPlacement(1);
        requestPane.putClientProperty("html.disable", Boolean.TRUE);
        RequestResponsePane.setLeftComponent(requestPane);
        requestPane.addTab("Request", bypassTable.getRequestViewer().uiComponent());

        // Burp Response 响应页面
        responsePane = new JTabbedPane();
        responsePane.setTabLayoutPolicy(1);
        responsePane.setTabPlacement(1);
        responsePane.putClientProperty("html.disable", Boolean.TRUE);
        RequestResponsePane.setRightComponent(responsePane);
        responsePane.addTab("Response", bypassTable.getResponseViewer().uiComponent());
    }

    @Override
    public int getRowCount() {
        return bypassArray.size();
    }


    @Override
    public int getColumnCount() {
        return 7;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> "Time";
            case 1 -> "Title";
            case 2 -> "Method";
            case 3 -> "MIME Type";
            case 4 -> "Request URL";
            case 5 -> "Length";
            case 6 -> "HTTP Status";
            default -> "";
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0, 1, 2, 3, 4 -> String.class;
            case 5 -> Integer.class;
            case 6 -> Short.class;
            default -> null;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Bypass bypassEntry = bypassArray.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> bypassEntry.timestamp;
            case 1 -> bypassEntry.title;
            case 2 -> bypassEntry.method;
            case 3 -> bypassEntry.mimeType;
            case 4 -> bypassEntry.url;
            case 5 -> bypassEntry.length;
            case 6 -> bypassEntry.status;
            default -> "";
        };
    }

    public List<Bypass> getBypassArray() {
        return bypassArray;
    }

    public synchronized void add(Bypass bypassEntry) {
        int index = bypassArray.size();
        bypassArray.add(bypassEntry);
        fireTableRowsInserted(index, index);
    }

    public Bypass get(int rowIndex) {
        return bypassArray.get(rowIndex);
    }

    /**
     * 界面显示数据存储模块
     */
    public static class Bypass {
        final String timestamp;
        final int length;
        final HttpRequestResponse requestResponse;
        final String url;
        final short status;
        final String mimeType;
        final String method;
        final String title;

        public Bypass(String timestamp, String method, int length, HttpRequestResponse requestResponse, String url, short status, String mimeType, String title) {
            this.timestamp = timestamp;
            this.method = method;
            this.length = length;
            this.requestResponse = requestResponse;
            this.url = url;
            this.status = status;
            this.mimeType = mimeType;
            this.title = title;
        }
    }

    /**
     * 自定义 Table
     */
    private class BypassTable extends JTable {
        public BypassTable(TableModel tableModel) {
            super(tableModel);
            setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            setColumnLayoutParameters();
            setAutoCreateRowSorter(true);
        }

        private void setColumnLayoutParameters() {
            int[] columnWidths = {150, 100, 100, 100, 500, 100, 100};
            for (int i = 0; i < columnWidths.length; i++) {
                TableColumn column = getColumnModel().getColumn(i);
                column.setMinWidth(columnWidths[i]);
                column.setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value,
                                                                   boolean isSelected, boolean hasFocus,
                                                                   int row, int column) {
                        Component rendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        setHorizontalAlignment(SwingConstants.LEFT);
                        return rendererComponent;
                    }
                });
            }
        }

        @Override
        public void changeSelection(int row, int col, boolean toggle, boolean extend) {
            ScanResultPanel.Bypass bypassEntry = ScanResultPanel.this.get(convertRowIndexToModel(row));
            requestViewer.setRequest(bypassEntry.requestResponse.request());
            responseViewer.setResponse(bypassEntry.requestResponse.response());
            super.changeSelection(row, col, toggle, extend);
        }

        private HttpRequestEditor getRequestViewer() {
            return requestViewer;
        }

        private HttpResponseEditor getResponseViewer() {
            return responseViewer;
        }
    }
}

