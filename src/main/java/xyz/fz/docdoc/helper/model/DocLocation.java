package xyz.fz.docdoc.helper.model;

import java.util.List;

public class DocLocation {

    private String docTimeLatest;

    private List<String> devLocations;

    public String getDocTimeLatest() {
        return docTimeLatest;
    }

    public void setDocTimeLatest(String docTimeLatest) {
        this.docTimeLatest = docTimeLatest;
    }

    public List<String> getDevLocations() {
        return devLocations;
    }

    public void setDevLocations(List<String> devLocations) {
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
