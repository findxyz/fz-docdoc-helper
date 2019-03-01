package xyz.fz.docdoc.helper.model;

public class DocConfig {

    private String mockUsername;

    private String mockAddress;

    private String programAddress;

    private String localPort;

    public static DocConfig ofDefault() {
        DocConfig docConfig = new DocConfig();
        docConfig.mockUsername = "fz";
        docConfig.mockAddress = "192.168.1.21:9981";
        docConfig.programAddress = "192.168.1.21:9000";
        docConfig.localPort = "80";
        return docConfig;
    }

    public String getMockUsername() {
        return mockUsername;
    }

    public void setMockUsername(String mockUsername) {
        this.mockUsername = mockUsername;
    }

    public String getMockAddress() {
        return mockAddress;
    }

    public void setMockAddress(String mockAddress) {
        this.mockAddress = mockAddress;
    }

    public String getProgramAddress() {
        return programAddress;
    }

    public void setProgramAddress(String programAddress) {
        this.programAddress = programAddress;
    }

    public String getLocalPort() {
        return localPort;
    }

    public void setLocalPort(String localPort) {
        this.localPort = localPort;
    }
}
