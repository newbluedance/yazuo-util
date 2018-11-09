package com.yazuo.start;

import com.yazuo.entity.ExceptionCode;
import com.yazuo.utils.JDBCutil;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author lichunfeng 仅内部使用 生成唯一异常码
 */
public class GenerateExceptionCodeService {

    private String projectPath;

    /**
     * 生成异常码
     * @return
     */
    public String  generateExceptCode() {
        //获取配置文件数据
        List<ExceptionCode> exceptionCodes = getProperties();
        List<ExceptionCode> dbCodeList = JDBCutil.executeQuery("select * from exception_code",new ExceptionCode(),null);
        //将配置文件中的配置更新到表
        saveCur(exceptionCodes,dbCodeList);
        //生成新的异常码
        ExceptionCode properCode = new ExceptionCode(getNextCode(), null);
        properCode.setCreateTime(new Date());
        properCode.setCreateUser(GenerateEntityService.getProperties().getProperty("author"));
        JDBCutil.executeUpdate("insert into exception_code(code,message,create_user,create_time) values(?,?,?,?)",properCode.getCode(),properCode.getMessage(),properCode.getCreateUser(),properCode.getCreateTime());
        return properCode.getCode();
    }

    /**
     * 生成前端异常码json文件
     */
    public String generateJsonForUI() {
        List<ExceptionCode> exceptionCodeList = getProperties();

        StringBuffer re = new StringBuffer("export default {\n");
        for (int i = 0; i < exceptionCodeList.size(); i++) {
            re.append("  '").append(exceptionCodeList.get(i).getCode()).append("': '")
                .append(exceptionCodeList.get(i).getMessage()).append("'");
            if (i != exceptionCodeList.size() - 1) {
                re.append(",");
            }
            re.append("\n");
        }
        re.append("}");

        return re.toString();
    }

    /**
     * 获取配置文件中所有业务异常码
     */
    private List<ExceptionCode> getProperties() {

        projectPath=GenerateEntityService.getProperties().getProperty("projectPath");
        Properties properties = new Properties();
        List<ExceptionCode> exceptionCodes = new ArrayList<>(100);
        try {
            File f = new File(projectPath+"src\\main\\resources\\i18n\\messages.properties");

            InputStream in = new BufferedInputStream(new FileInputStream(f));
            properties.load(new InputStreamReader(in, "utf-8"));
            Set<Map.Entry<Object, Object>> entrySet = properties.entrySet();

            for (Map.Entry<Object, Object> entry : entrySet) {
                Long code = Long.parseLong(entry.getKey().toString());
                if (code > 303020101000L && code <= 303020101999L) {
                    exceptionCodes.add(new ExceptionCode(code.toString(), entry.getValue().toString()));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Collections.sort(exceptionCodes, new Comparator<ExceptionCode>() {
            @Override
            public int compare(ExceptionCode o1, ExceptionCode o2) {

                return (int) (Long.parseLong(o1.getCode()) - Long.parseLong(o2.getCode()));
            }
        });
        return exceptionCodes;
    }

    /**
     * 更新配置文件到数据库
     */
    private void saveCur(List<ExceptionCode> list, List<ExceptionCode> dbCodeList) {
        for (ExceptionCode properCode : list) {
            ExceptionCode dbCode = getOneDbExceptionCode(properCode.getCode(), dbCodeList);
            if (dbCode == null) {
                JDBCutil.executeUpdate("insert into exception_code(code,message,create_time) values(?,?,?)",properCode.getCode(),properCode.getMessage(),new Date());
            } else if (dbCode.getMessage() == null || !dbCode.getMessage().equals(properCode.getMessage())) {
                properCode.setId(dbCode.getId());
                properCode.setUpdateTime(new Date());
                JDBCutil.executeUpdate("update exception_code set code=?, message=?, update_time=? where id= ?",properCode.getCode(),properCode.getMessage(),properCode.getUpdateTime(),properCode.getId());
            }
        }

    }

    private ExceptionCode getOneDbExceptionCode(String code, List<ExceptionCode> dbCodeList) {
        Stream<ExceptionCode> stream = dbCodeList.stream().filter(d -> d.getCode().equals(code));
        if (stream != null) {
            List<ExceptionCode> exceptionCodes =stream.collect(Collectors.toList());
            if(exceptionCodes!=null &&exceptionCodes.size()>0){
                return exceptionCodes.get(0);
            }

        }
            return null;
    }

    /**
     * 获得当前下一个异常码
     * @return
     */
    private String getNextCode(){
        Connection conn = JDBCutil.getConnection();
        PreparedStatement ps = null;

        ArrayList<?> list = new ArrayList<>();
        try {
            ps = conn.prepareStatement("SELECT max(code)+1 maxCode FROM `exception_code`");
            ResultSet rs = ps.executeQuery();//获得结果集
            while (rs.next()) {
                return rs.getString("maxCode");
            }

    } catch (Exception e) {
        e.printStackTrace();
    } finally {
            JDBCutil.close(ps);
            JDBCutil.close(conn);
    }
    return null;

}
}
