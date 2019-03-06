package xyz.fz.docdoc.helper.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.fz.docdoc.helper.event.EventListener;
import xyz.fz.docdoc.helper.event.NginxStartErrEvent;
import xyz.fz.docdoc.helper.model.DocConfig;
import xyz.fz.docdoc.helper.model.DocResult;
import xyz.fz.docdoc.helper.util.BaseUtil;
import xyz.fz.docdoc.helper.util.HttpUtil;
import xyz.fz.docdoc.helper.util.ProcessUtil;
import xyz.fz.docdoc.helper.util.ThreadUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DocService implements EventListener<NginxStartErrEvent> {

    private static Logger LOGGER = LoggerFactory.getLogger(DocService.class);

    private NginxService nginxService;

    private DocConfig docConfig = DocConfig.ofDefault();

    private volatile boolean START = false;

    private volatile String DOC_TIME_LATEST = System.currentTimeMillis() + "";

    public DocService() {
        this.nginxService = new NginxService();
    }

    public void configRefresh(DocConfig docConfig) {
        this.docConfig = docConfig;
    }

    public synchronized void start() {

        docConfigCheck();

        nginxRestart();
    }

    public synchronized void stop() {
        nginxStop();
    }

    private void docConfigCheck() {
        if (!netStatusMockCheck(docConfig.getMockAddress())) {
            throw new RuntimeException("连接模拟数据地址失败");
        }
        if (!netStatusProgramCheck(docConfig.getProgramAddress())) {
            throw new RuntimeException("连接程序数据地址失败");
        }
        try {
            int port = Integer.parseInt(docConfig.getLocalPort());
            if (port < 0 || port > 65535) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("本地端口格式错误");
        }
    }

    public boolean netStatusMockCheck(String mockAddress) {
        return HttpUtil.serverTest("http://" + mockAddress);
    }

    public boolean netStatusProgramCheck(String programAddress) {
        return HttpUtil.serverTest("http://" + programAddress);
    }

    private void nginxRestart() {
        DocResult docResult = fetchDocResult();
        if (!StringUtils.equals(docResult.getData().getDocTimeLatest(), DOC_TIME_LATEST)) {
            ProcessUtil.exists("nginx.exe", (exist) -> {
                if (exist) {
                    nginxStop();

                    Set<Integer> running = new HashSet<>();
                    running.add(1);
                    while (running.size() > 0) {
                        try {
                            Thread.sleep(50L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ProcessUtil.exists("nginx.exe", exist2 -> {
                            if (!exist2) {
                                running.remove(1);
                            }
                        });
                    }
                }
            });
            nginxStart(docResult);
        }
    }

    private DocResult fetchDocResult() {
        try {
            String docHelperLocationsUrl = "/doc/helper/locations";
            String json = HttpUtil.httpGet("http://" + docConfig.getMockAddress() + docHelperLocationsUrl, null);
            return BaseUtil.parseJson(json, DocResult.class);
        } catch (Exception e) {
            LOGGER.error("fetch doc result err: {}", BaseUtil.getExceptionStackTrace(e));
            throw new RuntimeException("接口异常无法获取最新列表");
        }
    }

    private void nginxStop() {
        nginxService.stop();
        START = false;
        DOC_TIME_LATEST = System.currentTimeMillis() + "";
    }

    private void nginxStart(DocResult docResult) {
        nginxService.start(docConfig, docResult);
        START = true;
        DOC_TIME_LATEST = docResult.getData().getDocTimeLatest();
    }

    public synchronized void docResultScheduleRefresh() {
        ThreadUtil.executorService().scheduleAtFixedRate(() -> {
            if (START) {
                try {
                    LOGGER.debug("auto check dev locations...");
                    nginxRestart();
                } catch (Exception e) {
                    LOGGER.error("doc result schedule refresh err: {}", BaseUtil.getExceptionStackTrace(e));
                }
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    @Override
    public void on(NginxStartErrEvent event) {
        DOC_TIME_LATEST = System.currentTimeMillis() + "";
    }
}
