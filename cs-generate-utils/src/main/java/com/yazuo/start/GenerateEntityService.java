package com.yazuo.start;

import com.yazuo.entity.PojoDto;
import com.yazuo.utils.JDBCutil;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * @author lichunfeng
 * @date 2018/10/24 13:04
 */
public class GenerateEntityService {

    private static final String TAB = "    ";
    private static final String ENTER = "\n";
    private static final String SQL = "SELECT COLUMN_COMMENT comment, column_name name, DATA_TYPE type FROM information_schema.COLUMNS co WHERE table_name = ? and TABLE_SCHEMA='cs_expert_dev'";
    private static Pattern LINE_PATTERN = Pattern.compile("_(\\w)");

    public static Properties getProperties() {
        return properties;
    }

    private static Properties properties;
    private static String entityClassName;

    private static String classComments;
    private static String classAnnotation;
    private static String dtoAnnotation;
    private static String importStr;
    private static String dtoImportStr;
    private static String mapperImportStr;
    private static StringBuffer entityBody = new StringBuffer();
    private static StringBuffer dtoBody = new StringBuffer();


    static {
        properties = new Properties();
        String path = Start.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        path = path.substring(0, path.lastIndexOf("/") + 1);
        try {
            properties
                .load(new FileInputStream(path.concat("/config.properties")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (properties.getProperty("className") != null) {
            entityClassName = properties.getProperty("className");
        } else {
            entityClassName = StringUtils.capitalize(lineToHump(properties.getProperty("tableName")));
        }

        int importDateFlag = 0;
        int importDateTimeFlag = 0;
        List<PojoDto> pojoDtos = JDBCutil.executeQuery(SQL, new PojoDto(), properties.getProperty("tableName"));

        for (PojoDto dto : pojoDtos) {
            if ("date".equals(dto.getType())) {
                importDateFlag = 1;
            }
            if ("datetime".equals(dto.getType())) {
                importDateTimeFlag = 1;
            }
            entityBody.append(TAB).append(dto.getComments()).append(ENTER).append(TAB).append(dto.getAnnotation())
                .append(ENTER).append(TAB).append(lineToHump(dto.getDeclareStr())).append(ENTER).append(ENTER);
            dtoBody.append(TAB).append(dto.getComments()).append(ENTER).append(TAB)
                .append(lineToHump(dto.getDeclareStr())).append(ENTER).append(ENTER);
        }

        String common = "import lombok.Data;\n"
            + "\n"
            + "import java.io.Serializable;\n";
        String persistence = "import javax.persistence.Column;\n"
            + "import javax.persistence.GeneratedValue;\n"
            + "import javax.persistence.Id;\n"
            + "import javax.persistence.Table;\n";
        String lombok = "import lombok.EqualsAndHashCode;\n"
            + "import lombok.ToString;\n";
        String add = "";
        if (importDateFlag == 1) {
            add += "import java.time.LocalDate;\n";
        }
        if (importDateTimeFlag == 1) {
            add += "import java.time.LocalDateTime;\n";
        }
        dtoImportStr = common + lombok + add;
        importStr = common + persistence + add;
        mapperImportStr = "import com.yazuo.framework.db.util.YazuoBaseMapper;\n"
            + "import org.apache.ibatis.annotations.Mapper;\n"
            + "import " + StringUtils.replace(properties.getProperty("package"), "\\", ".").concat("entity.")
            .concat(entityClassName).concat(";\n");

        classComments = "/**\n"
            + " * \n"
            + " *\n"
            + " * @author " + properties.getProperty("author") + "\n"
            + " * @date " + DateFormatUtils.format(new Date(), "yyyy-MM-dd hh:mm:ss") + "\n"
            + " */\n";
        classAnnotation = "@Data\n"
            + "@Table(name = \"" + properties.getProperty("tableName") + "\")\n";
        dtoAnnotation = "@Data\n";
    }

    /**
     * 生成实体类
     */
    public String GenerateEntity() {

        String javaEntityPath = properties.getProperty("projectPath").concat(properties.getProperty("javaPath"))
            .concat(properties.getProperty("package")).concat("entity").concat("\\").concat(entityClassName)
            .concat(".java");
        String packageStr = StringUtils.replace(properties.getProperty("package"), "\\", ".").concat("entity");

        StringBuffer sb = new StringBuffer();
        sb.append("package ").append(packageStr).append(";").append(ENTER).append(ENTER);
        sb.append(importStr);
        sb.append(classComments);
        sb.append(classAnnotation);
        sb.append("public class ").append(entityClassName).append("   implements Serializable {\n\n");
        sb.append(TAB).append("private static final long serialVersionUID = ").append(getSerialVersionUID()).append(";\n\n");
        sb.append(entityBody);
        sb.append("}");
        outFile(sb.toString(), javaEntityPath);
        return sb.toString();
    }

    /**
     * dto
     */
    public String GenerateDto() {
        String className = entityClassName.concat("Dto");
        String javaEntityPath = properties.getProperty("projectPath").concat(properties.getProperty("javaPath"))
            .concat(properties.getProperty("package")).concat("dto").concat("\\").concat(className).concat(".java");
        String packageStr = StringUtils.replace(properties.getProperty("package"), "\\", ".").concat("dto");

        StringBuffer sb = new StringBuffer();
        sb.append("package ").append(packageStr).append(";").append(ENTER).append(ENTER);
        sb.append(dtoImportStr);
        sb.append(classComments);
        sb.append(dtoAnnotation);
        sb.append("public class ").append(className).append("   implements Serializable {\n\n");
        sb.append(TAB).append("private static final long serialVersionUID = ").append(getSerialVersionUID()).append(";\n\n");
        sb.append(dtoBody);
        sb.append("}");
        outFile(sb.toString(), javaEntityPath);
        return sb.toString();
    }

    public String GenerateMapper() {
        String className = entityClassName.concat("Mapper");
        String javaEntityPath = properties.getProperty("projectPath").concat(properties.getProperty("javaPath"))
            .concat(properties.getProperty("package")).concat("mapper").concat("\\").concat(className).concat(".java");
        String packageStr = StringUtils.replace(properties.getProperty("package"), "\\", ".").concat("mapper");
        StringBuffer sb = new StringBuffer();
        sb.append("package ").append(packageStr).append(";").append(ENTER).append(ENTER);
        sb.append(mapperImportStr);
        sb.append(classComments);
        sb.append("@Mapper\n");
        sb.append("public interface ").append(className).append(" extends YazuoBaseMapper<" + entityClassName + ">")
            .append(" {\n\n");
        sb.append("}");
        outFile(sb.toString(), javaEntityPath);
        return sb.toString();
    }

    public String GenerateMapperXml() {
        String className = entityClassName.concat("Mapper");
        String packageStr = StringUtils.replace(properties.getProperty("package"), "\\", ".").concat("mapper.")
            .concat(className);
        String xmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n"
            + "<mapper namespace=\"" + packageStr + "\">\n"
            + "</mapper>";
        String mapperPath = properties.getProperty("projectPath").concat("src/main/resources/mappers/")
            .concat(className).concat(".xml");
        outFile(xmlStr, mapperPath);
        return xmlStr;
    }


    /**
     * 字符串蛇形转驼峰
     */
    public static String lineToHump(String str) {
        Matcher matcher = LINE_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     *
     * @param s
     * @param path
     */
    public static void outFile(String s, String path) {
        File file = new File(path);
        try {
                createNewFile(file);
            FileOutputStream fop = new FileOutputStream(file);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fop, "utf-8");

            BufferedWriter writer = new BufferedWriter(outputWriter);
            writer.write(s);
            writer.flush();
            System.out.println("已经生成文件到:" + path);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    static void createNewFile(File file){
        if(!file.getParentFile().exists()){
            try {
                file.getParentFile().mkdirs();
                if (!file.exists()) {
                    file.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getSerialVersionUID() {
        StringBuffer uid = new StringBuffer(19);
        for (int i = 0; i <= 17; i++) {   //生成一个6位的序列号
            int spy = (int) (Math.random() * 10);
            uid.append(spy);
        }
        uid.append("L");
        return uid.toString();
    }
}
