package xyz.fz.docdoc.helper.model;

import java.util.List;
import java.util.Map;

public class DocLocation {

    private String docTimeLatest;

    private List<Map<String, Object>> devLocations;

    public String getDocTimeLatest() {
        return docTimeLatest;
    }

    public void setDocTimeLatest(String docTimeLatest) {
        this.docTimeLatest = docTimeLatest;
    }

    public List<Map<String, Object>> getDevLocations() {
        return devLocations;
    }

    public void setDevLocations(List<Map<String, Object>> devLocations) {
        this.devLocations = devLocations;
    }

    @Override
    public String toString() {
        return "DocLocation{" +
                "docTimeLatest='" + docTimeLatest + '\'' +
                ", devLocations=" + devLocations +
                '}';
    }
}
