package com.hamsterwhat.wechat.entity.po;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class AppVersion implements Serializable {

    @Serial
    private static final long serialVersionUID = 3662285696626906902L;

    private Long id;

    private String version;

    private String description;

    private Short status;

    private String[] grayscaleUIDs;

    private Short fileType;

    private String backlink;

    private Date createdAt;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
