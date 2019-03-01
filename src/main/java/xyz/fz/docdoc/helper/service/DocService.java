package xyz.fz.docdoc.helper.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.fz.docdoc.helper.model.DocConfig;
import xyz.fz.docdoc.helper.model.DocResult;
import xyz.fz.docdoc.helper.util.BaseUtil;
import xyz.fz.docdoc.helper.util.HttpUtil;
import xyz.fz.docdoc.helper.util.ThreadUtil;

import java.util.concurrent.TimeUnit;

public class DocService {

    private static Logger LOGGER = LoggerFactory.getLogger(DocService.class);

    private NginxService nginxService;

    private DocConfig docConfig = DocConfig.ofDefault();

    private volatile boolean START = false;

    private volatile String DOC_TIME_LATEST = "";

    public DocService() {
        this.nginxService = new NginxService();
    }

    public void configRefresh(DocConfig docConfig) {
        this.docConfig = docConfig;
    }

    public synchronized void start() throws Exception {

        docConfigCheck();

        nginxStart();
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

    private void nginxStart() throws Exception {
        DocResult docResult = fetchDocResult();
        if (docResult == null) {
            throw new RuntimeException("接口异常无法获取最新列表");
        }
        if (docResult.isSuccess()) {
            nginxService.start(docConfig, docResult);
            START = true;
        }
    }

    private DocResult fetchDocResult() {
        try {
            String docHelperLocationsUrl = "/doc/helper/locations";
            String json = HttpUtil.httpGet("http://" + docConfig.getMockAddress() + docHelperLocationsUrl, null);
            DocResult docResult = BaseUtil.parseJson(json, DocResult.class);
            if (!StringUtils.equals(docResult.getData().getDocTimeLatest(), DOC_TIME_LATEST)) {
                DOC_TIME_LATEST = docResult.getData().getDocTimeLatest();
            } else {
                docResult.setSuccess(false);
            }
            return docResult;
        } catch (Exception e) {
            LOGGER.error("fetch doc result err: {}", BaseUtil.getExceptionStackTrace(e));
            return null;
        }
    }

    public synchronized void stop() {
        nginxStop();
    }

    private void nginxStop() {
        nginxService.stop();
        START = false;
    }

    public synchronized void docResultRefreshSchedule() {
        ThreadUtil.executorService().scheduleAtFixedRate(() -> {
            if (START) {
                try {
                    LOGGER.debug("auto check dev locations...");
                    nginxStart();
                } catch (Exception e) {
                    LOGGER.error("doc result refresh schedule err: {}", BaseUtil.getExceptionStackTrace(e));
                }
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
}
