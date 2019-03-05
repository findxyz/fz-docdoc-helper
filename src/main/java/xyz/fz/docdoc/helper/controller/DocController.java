package xyz.fz.docdoc.helper.controller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.fz.docdoc.helper.event.EventBus;
import xyz.fz.docdoc.helper.event.EventListener;
import xyz.fz.docdoc.helper.event.NginxStartErrEvent;
import xyz.fz.docdoc.helper.form.MainForm;
import xyz.fz.docdoc.helper.model.DocConfig;
import xyz.fz.docdoc.helper.service.DocService;
import xyz.fz.docdoc.helper.util.BaseUtil;
import xyz.fz.docdoc.helper.util.ThreadUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

public class DocController implements EventListener<NginxStartErrEvent> {

    private static Logger LOGGER = LoggerFactory.getLogger(DocController.class);

    private DocService docService;

    private MainForm mainForm;

    public MainForm getMainForm() {
        return mainForm;
    }

    public DocController() {

        this.docService = new DocService();

        this.mainForm = new MainForm();

        mainForm.getTriggerBtn().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (mainForm.getTriggerBtn().isEnabled()) {
                    boolean start = mainForm.getTriggerBtn().getText().equals("启动");
                    if (start) {
                        start();
                    } else {
                        stop();
                    }
                }
            }
        });

        this.configLoad();
    }

    private void configLoad() {
        File config = new File("helper-doc.conf");
        DocConfig docConfig;
        if (config.exists()) {
            try {
                docConfig = BaseUtil.parseJson(FileUtils.readFileToString(config, Charset.forName("utf-8")), DocConfig.class);
            } catch (Exception e) {
                docConfig = DocConfig.ofDefault();
            }
        } else {
            docConfig = DocConfig.ofDefault();
        }
        mainForm.getMockUsername().setText(docConfig.getMockUsername());
        mainForm.getMockAddress().setText(docConfig.getMockAddress());
        mainForm.getProgramAddress().setText(docConfig.getProgramAddress());
        mainForm.getLocalPort().setText(docConfig.getLocalPort());
        docService.configRefresh(docConfig);
    }

    private void configStore() {
        File config = new File("helper-doc.conf");
        DocConfig docConfig = new DocConfig();
        docConfig.setMockUsername(StringUtils.defaultIfBlank(mainForm.getMockUsername().getText(), ""));
        docConfig.setMockAddress(StringUtils.defaultIfBlank(mainForm.getMockAddress().getText(), ""));
        docConfig.setProgramAddress(StringUtils.defaultIfBlank(mainForm.getProgramAddress().getText(), ""));
        docConfig.setLocalPort(StringUtils.defaultIfBlank(mainForm.getLocalPort().getText(), ""));
        try {
            FileUtils.writeStringToFile(config, BaseUtil.toJson(docConfig), Charset.forName("utf-8"));
        } catch (Exception ignored) {
        }
        docService.configRefresh(docConfig);
    }

    public void init() {
        initListeners();
        initScheduleCheck();
    }

    private void initListeners() {
        EventBus.addListener(this);
        EventBus.addListener(docService);
    }

    private void initScheduleCheck() {
        netStatusAllScheduleCheck();
        docService.docResultScheduleRefresh();
    }

    private void netStatusAllScheduleCheck() {
        ThreadUtil.executorService().scheduleAtFixedRate(() -> {
            try {
                netStatusMockUpdate(docService.netStatusMockCheck(mainForm.getMockAddress().getText()));
                netStatusProgramUpdate(docService.netStatusProgramCheck(mainForm.getProgramAddress().getText()));
            } catch (Exception e) {
                LOGGER.error("{}", BaseUtil.getExceptionStackTrace(e));
            }
        }, 2, 2, TimeUnit.SECONDS);
    }

    private void netStatusMockUpdate(boolean ok) {
        if (ok) {
            mainForm.getMockStatus().setText("网络通畅");
            mainForm.getMockStatus().setForeground(Color.BLUE);
        } else {
            mainForm.getMockStatus().setText("无法连通");
            mainForm.getMockStatus().setForeground(Color.RED);
        }
    }

    private void netStatusProgramUpdate(boolean ok) {
        if (ok) {
            mainForm.getProgramStatus().setText("网络通畅");
            mainForm.getProgramStatus().setForeground(Color.BLUE);
        } else {
            mainForm.getProgramStatus().setText("无法连通");
            mainForm.getProgramStatus().setForeground(Color.RED);
        }
    }

    private void start() {
        mainForm.getTriggerBtn().setEnabled(false);
        mainForm.getTriggerBtn().setText("启动中");
        formFieldTrigger(false);
        SwingUtilities.invokeLater(() -> {
            try {
                configStore();
                docService.start();
                mainForm.getTriggerBtn().setText("暂停");
            } catch (Exception e) {
                formFieldTrigger(true);
                mainForm.getTriggerBtn().setText("启动");
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, e.getMessage()));
            } finally {
                mainForm.getTriggerBtn().setEnabled(true);
            }
        });
    }

    private void stop() {
        mainForm.getTriggerBtn().setEnabled(false);
        mainForm.getTriggerBtn().setText("暂停中");
        SwingUtilities.invokeLater(() -> {
            try {
                docService.stop();
                formFieldTrigger(true);
                mainForm.getTriggerBtn().setText("启动");
            } catch (Exception e) {
                formFieldTrigger(false);
                mainForm.getTriggerBtn().setText("暂停");
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, e.getMessage()));
            } finally {
                mainForm.getTriggerBtn().setEnabled(true);
            }
        });
    }

    private void formFieldTrigger(boolean enabled) {
        mainForm.getMockUsername().setEnabled(enabled);
        mainForm.getMockAddress().setEnabled(enabled);
        mainForm.getProgramAddress().setEnabled(enabled);
        mainForm.getLocalPort().setEnabled(enabled);
    }

    public void destroy() {
        docService.stop();
    }

    @Override
    public void on(NginxStartErrEvent event) {
        formFieldTrigger(true);
        mainForm.getTriggerBtn().setText("启动");
        mainForm.getTriggerBtn().setEnabled(true);
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, event.getMsg()));
    }
}
