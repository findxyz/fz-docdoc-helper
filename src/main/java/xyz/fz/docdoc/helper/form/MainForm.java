package xyz.fz.docdoc.helper.form;

import javax.swing.*;

public class MainForm {
    private JButton triggerBtn;
    private JTextField localPort;
    private JTextField programAddress;
    private JTextField mockAddress;
    private JPanel mainPanel;
    private JTextField mockUsername;
    private JLabel mockStatus;
    private JLabel programStatus;

    public JButton getTriggerBtn() {
        return triggerBtn;
    }

    public JTextField getLocalPort() {
        return localPort;
    }

    public JTextField getProgramAddress() {
        return programAddress;
    }

    public JTextField getMockAddress() {
        return mockAddress;
    }

    JPanel getMainPanel() {
        return mainPanel;
    }

    public JTextField getMockUsername() {
        return mockUsername;
    }

    public JLabel getMockStatus() {
        return mockStatus;
    }

    public JLabel getProgramStatus() {
        return programStatus;
    }
}
