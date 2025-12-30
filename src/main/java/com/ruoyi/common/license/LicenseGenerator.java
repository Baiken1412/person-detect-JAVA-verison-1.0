package com.ruoyi.common.license;

import com.alibaba.fastjson.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

/**
 * 许可证生成工具
 * 用于为指定机器码生成带有效期的许可证文件
 *
 */
public class LicenseGenerator {

    /**
     * AES加密密钥（16位）- 必须与LicenseValidator中的密钥一致
     */
    private static final String SECRET_KEY = "RuoYi2025License";

    /**
     * 默认有效期（天数）
     */
    private static final int DEFAULT_VALIDITY_DAYS = 365; // 1年

    /**
     * 为指定机器码生成许可证文件（默认1年有效期）
     *
     * @param machineCode 机器码
     * @param outputPath  输出文件路径
     * @return true-生成成功，false-生成失败
     */
    public static boolean generateLicense(String machineCode, String outputPath) {
        return generateLicense(machineCode, outputPath, DEFAULT_VALIDITY_DAYS);
    }

    /**
     * 为指定机器码生成许可证文件（自定义有效期）
     *
     * @param machineCode   机器码
     * @param outputPath    输出文件路径
     * @param validityDays  有效期天数
     * @return true-生成成功，false-生成失败
     */
    public static boolean generateLicense(String machineCode, String outputPath, int validityDays) {
        try {
            // 计算过期时间
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, validityDays);
            Date expireDate = calendar.getTime();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String expireDateStr = sdf.format(expireDate);

            // 创建许可证信息JSON对象
            JSONObject licenseInfo = new JSONObject();
            licenseInfo.put("machineCode", machineCode);
            licenseInfo.put("expireDate", expireDateStr);
            licenseInfo.put("createDate", sdf.format(new Date()));

            // 加密许可证信息
            String licenseData = licenseInfo.toJSONString();
            byte[] encryptedData = encrypt(licenseData);

            // 写入文件
            Files.write(Paths.get(outputPath), encryptedData);

            System.out.println("许可证生成成功!");
            System.out.println("========================================");
            System.out.println("文件路径: " + outputPath);
            System.out.println("机器码: " + machineCode);
            System.out.println("生成时间: " + sdf.format(new Date()));
            System.out.println("有效期: " + validityDays + " 天");
            System.out.println("过期时间: " + expireDateStr);
            System.out.println("========================================");

            return true;
        } catch (Exception e) {
            System.err.println("许可证生成失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * AES加密
     */
    private static byte[] encrypt(String data) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(data.getBytes("UTF-8"));
    }

    /**
     * 主方法 - 用于生成许可证
     * 运行此类可以为指定机器码生成许可证文件
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=====================================");
        System.out.println("     若依系统 - 许可证生成工具");
        System.out.println("=====================================");
        System.out.println();

        try {
            // 选择模式
            System.out.println("请选择模式:");
            System.out.println("1. 为当前机器生成许可证");
            System.out.println("2. 为指定机器码生成许可证");
            System.out.print("请输入选项 (1/2): ");

            String choice = scanner.nextLine().trim();

            String machineCode;
            if ("1".equals(choice)) {
                // 为当前机器生成
                machineCode = MachineCodeUtil.getMachineCode();
                System.out.println();
                System.out.println("当前机器码: " + machineCode);
            } else if ("2".equals(choice)) {
                // 为指定机器码生成
                System.out.print("请输入目标机器码: ");
                machineCode = scanner.nextLine().trim();
            } else {
                System.err.println("无效的选项!");
                return;
            }

            // 输入有效期
            System.out.println();
            System.out.print("请输入有效期天数 (默认: 365天/1年): ");
            String validityDaysStr = scanner.nextLine().trim();
            int validityDays = DEFAULT_VALIDITY_DAYS;
            if (!validityDaysStr.isEmpty()) {
                try {
                    validityDays = Integer.parseInt(validityDaysStr);
                    if (validityDays <= 0) {
                        System.err.println("有效期必须大于0，使用默认值365天");
                        validityDays = DEFAULT_VALIDITY_DAYS;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("输入无效，使用默认值365天");
                    validityDays = DEFAULT_VALIDITY_DAYS;
                }
            }

            // 输入输出路径
            System.out.print("请输入许可证文件保存路径 (默认: license.dat): ");
            String outputPath = scanner.nextLine().trim();
            if (outputPath.isEmpty()) {
                outputPath = "license.dat";
            }

            // 生成许可证
            System.out.println();
            System.out.println("正在生成许可证...");
            boolean success = generateLicense(machineCode, outputPath, validityDays);

            if (success) {
                System.out.println();
                System.out.println("=====================================");
                System.out.println("许可证生成完成!");
                System.out.println("请将 " + outputPath + " 文件复制到目标机器的程序根目录");
                System.out.println("=====================================");
            }
        } finally {
            scanner.close();
        }
    }
}
