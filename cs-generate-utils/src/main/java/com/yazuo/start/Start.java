package com.yazuo.start;

import java.util.Scanner;

/**
 * @author lichunfeng
 * @date 2018/10/24 11:43
 */
public class Start {

    public static void main(String[] args) {
        while (true){
            try {
                GenerateEntityService entityService = new GenerateEntityService();
                GenerateExceptionCodeService  exceptionCodeService =new GenerateExceptionCodeService();

                System.out.println("1:生成实体类 2:生成Dto 3:生成Mapper 4:生成mapper.xml 5:生成已上全部 6:生成新的异常码 7:生成前端异常码json文件 0:exit");
                int num=0;
                do {
                    try {
                        Scanner scanner = new Scanner(System.in);
                        num = Integer.parseInt(scanner.next());
                    } catch (Exception e) {
                        System.out.println("输入有误请重新输入!");
                    }
                }
                while (num < 0 || num > 7);

                switch (num) {
                    case 0:
                        return;
                    case 1:
                        entityService.GenerateEntity();
                        break;
                    case 2:
                        entityService.GenerateDto();
                        break;
                    case 3:
                        entityService.GenerateMapper();
                        break;
                    case 4:
                        entityService.GenerateMapperXml();
                        break;
                    case 5:
                        entityService.GenerateEntity();
                        entityService.GenerateDto();
                        entityService.GenerateMapper();
                        entityService.GenerateMapperXml();
                        break;
                    case 6:
                        System.out.println(exceptionCodeService.generateExceptCode());
                        break;
                    case 7:
                        System.out.println(exceptionCodeService.generateJsonForUI());
                        break;
                    default:

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }




    }

}
