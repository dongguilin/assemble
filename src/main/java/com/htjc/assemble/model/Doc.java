package com.htjc.assemble.model;

import java.util.Map;

/**
 * Created by guilin on 2016/9/1.
 * 文档
 */
public class Doc {

    //ID标识
    private String id;

    //索引
    private String index;

    //类型
    private String type;

    private long timestamp;

    //内容
    private Map<String, Object> body;

    @Override
    public String toString() {
        return "Doc{" +
                "id='" + id + '\'' +
                ", index='" + index + '\'' +
                ", type='" + type + '\'' +
                ", timestamp=" + timestamp +
                ", body=" + body +
                '}';
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getBody() {
        return body;
    }

    public void setBody(Map<String, Object> body) {
        this.body = body;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
