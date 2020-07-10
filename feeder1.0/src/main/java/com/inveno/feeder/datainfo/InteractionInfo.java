package com.inveno.feeder.datainfo;

import java.io.Serializable;

public class InteractionInfo implements Serializable {

    private String contentId;
    private String commentCount;
    private String thumbupCount;

    public String getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(String commentCount) {
        this.commentCount = commentCount;
    }

    public String getThumbupCount() {
        return thumbupCount;
    }

    public void setThumbupCount(String thumbupCount) {
        this.thumbupCount = thumbupCount;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    @Override
    public String toString() {
        return "InteractionInfo{" +
                "contentId='" + contentId + '\'' +
                ", commentCount='" + commentCount + '\'' +
                ", thumbupCount='" + thumbupCount + '\'' +
                '}';
    }
}
