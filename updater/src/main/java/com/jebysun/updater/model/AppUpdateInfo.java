package com.jebysun.updater.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/1/20.
 */

public class AppUpdateInfo implements Serializable {

    private int versionCode;
    private String versionName;
    private String fileSize;
    private boolean required;
    private String apkUrl;
    private Date publishDate;
    private List<String> updateMsgList = new ArrayList<>();


    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getApkUrl() {
        return apkUrl;
    }

    public void setApkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public List<String> getUpdateMsgList() {
        return updateMsgList;
    }

    public void setUpdateMsgList(List<String> updateMsgList) {
        this.updateMsgList = updateMsgList;
    }
}
