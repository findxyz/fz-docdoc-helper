package xyz.fz.docdoc.helper.form;

import xyz.fz.docdoc.helper.controller.DocController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {

    private DocController docController;

    public MainFrame(DocController docController) {
        this.docController = docController;
        this.setContentPane(docController.getMainForm().getMainPanel());
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setResizable(false);
        this.setLocation(700, 400);
        this.setTitle("docdochelper");
        this.pack();
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        // 初始化系统托盘
        initTray();
    }

    private void initTray() {
        MenuItem mainMenuItem = new MenuItem("Main");
        mainMenuItem.addActionListener(e -> setVisible(true));
        MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.addActionListener(e -> {
            docController.destroy();
            System.exit(0);
        });

        PopupMenu popup = new PopupMenu();
        popup.add(mainMenuItem);
        popup.addSeparator();
        popup.add(exitMenuItem);

        Image iconImage = Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/images/exchange.png"));
        TrayIcon trayIcon = new TrayIcon(iconImage, "docdochelper", popup);
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(e -> setVisible(true));

        if (SystemTray.isSupported()) {
            SystemTray systemTray = SystemTray.getSystemTray();
            try {
                systemTray.add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
    }
}
