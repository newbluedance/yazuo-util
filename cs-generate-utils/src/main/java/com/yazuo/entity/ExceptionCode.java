package com.yazuo.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lichunfeng
 * @create 2018-08-11 16:20:39
 **/
@Data
@NoArgsConstructor
@Table(name = "exception_code")
public class ExceptionCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "code")
    private String code;

    @Column(name = "message")
    private String message;

    @Column(name = "description")
    private String description;

    @Column(name = "create_user")
    private String createUser;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

    public ExceptionCode(String code,String message){
        super();
        this.code=code;
        this.message=message;
    }

    public ExceptionCode(String code, String message, String description, String createUser, Date createTime, Date updateTime) {
        super();
        this.code = code;
        this.message = message;
        this.description = description;
        this.createUser = createUser;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

}