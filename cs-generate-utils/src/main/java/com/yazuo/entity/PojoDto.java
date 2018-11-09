package com.yazuo.entity;

import javax.persistence.Column;
import lombok.Data;

/**
 * @author lichunfeng
 * @date 2018/10/24 13:08
 */
@Data
public class PojoDto {

    private String name;
    private String comment;
    private String type;

    private String declareStr;

    private String comments;

    private String annotation;

    private String fieldType;


    public String getDeclareStr() {
        return "private " + getFieldType() + " " + name + ";";
    }

    public String getComments() {
        if ("id".equals(name)) {
            return "/** ID主键自增 */";
        }
        return "/** " + comment + " */";
    }

    public String getAnnotation() {
        if ("id".equals(name)) {
            return "@Id\n"
                + "    @GeneratedValue(generator = \"JDBC\")";
        }
        return "@Column";
    }

    public String getFieldType() {
        if ("int".equals(this.type) ||"tinyint".equals(this.type) || "smallint".equals(this.type)) {
            return "Integer";
        } else if ("bigint".equals(this.type)) {
            return "Long";
        } else if ("varchar".equals(this.type) || "text".equals(this.type)) {
            return "String";
        } else if ("date".equals(this.type)) {
            return "LocalDate";
        } else if ("datetime".equals(this.type)) {
            return "LocalDateTime";
        } else {
            return "undefine";
        }
    }
}
