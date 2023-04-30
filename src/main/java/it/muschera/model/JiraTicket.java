package it.muschera.model;

import java.util.List;

public class JiraTicket {

    private String key;
    private Release injectedVersion;
    private Release openingVersion;
    private Release fixVersion;
    private List<Release> affectedVersions;

    public JiraTicket(String key, Release ov, Release fv, List<Release> av) {
        this.key = key;
        this.injectedVersion = null;
        this.openingVersion = ov;
        this.fixVersion = fv;
        this.affectedVersions = av;
    }



    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Release getInjectedVersion() {
        return injectedVersion;
    }

    public void setInjectedVersion(Release injectedVersion) {
        this.injectedVersion = injectedVersion;
    }

    public Release getOpeningVersion() {
        return openingVersion;
    }

    public void setOpeningVersion(Release openingVersion) {
        this.openingVersion = openingVersion;
    }

    public Release getFixVersion() {
        return fixVersion;
    }

    public void setFixVersion(Release fixVersion) {
        this.fixVersion = fixVersion;
    }

    public List<Release> getAffectedVersions() {
        return affectedVersions;
    }

    public void setAffectedVersions(List<Release> affectedVersions) {
        this.affectedVersions = affectedVersions;
    }





}
