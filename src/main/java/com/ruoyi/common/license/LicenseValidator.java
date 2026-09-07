package com.ruoyi.common.license;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 许可证验证工具类
 * 用于验证许可证文件是否与当前机器匹配及有效期
 *
 */
public class LicenseValidator {

    /**
     * 许可证文件路径（放在程序根目录）
     */
    private static final String LICENSE_FILE_PATH = "license.dat";

    /**
     * AES加密密钥（16位）- 可通过环境变量 LICENSE_SECRET_KEY 覆盖，未设置时使用开发默认值
     */
    private static final String SECRET_KEY = System.getenv().getOrDefault("LICENSE_SECRET_KEY", "RuoYi2025License");

    /**
     * 验证许可证
     *
     * @return true-许可证有效，false-许可证无效
     */
    public static boolean validate() {
        try {
            // 检查许可证文件是否存在
            File licenseFile = new File(LICENSE_FILE_PATH);
            if (!licenseFile.exists()) {
                System.err.println("错误: 未找到许可证文件 [" + LICENSE_FILE_PATH + "]");
                return false;
            }

            // 读取许可证文件内容
            byte[] encryptedData = Files.readAllBytes(Paths.get(LICENSE_FILE_PATH));

            // 检查文件是否为空
            if (encryptedData == null || encryptedData.length == 0) {
                System.err.println("错误: 许可证文件为空");
                return false;
            }

            // 解密许可证
            String decryptedData = decrypt(encryptedData);

            // 检查解密结果
            if (decryptedData == null || decryptedData.trim().isEmpty()) {
                System.err.println("错误: 许可证文件内容无效");
                return false;
            }

            // 解析许可证信息（JSON格式）
            JSONObject licenseInfo = JSON.parseObject(decryptedData);
            if (licenseInfo == null) {
                System.err.println("错误: 许可证数据格式错误");
                return false;
            }

            String licensedMachineCode = licenseInfo.getString("machineCode");
            String expireDate = licenseInfo.getString("expireDate");

            // 检查必要字段是否存在
            if (licensedMachineCode == null || licensedMachineCode.trim().isEmpty()) {
                System.err.println("错误: 许可证中缺少机器码信息");
                return false;
            }

            if (expireDate == null || expireDate.trim().isEmpty()) {
                System.err.println("错误: 许可证中缺少有效期信息");
                return false;
            }

            // 获取当前机器码
            String currentMachineCode = MachineCodeUtil.getMachineCode();

            // 1. 验证机器码是否匹配
            if (!currentMachineCode.equals(licensedMachineCode)) {
                System.err.println("错误: 许可证与当前机器不匹配");
                System.err.println("当前机器码: " + currentMachineCode);
                return false;
            }

            // 2. 验证有效期
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date expireDateObj = sdf.parse(expireDate);
            Date currentDate = new Date();

            if (currentDate.after(expireDateObj)) {
                System.err.println("错误: 许可证已过期");
                System.err.println("过期时间: " + expireDate);
                return false;
            }

            // 验证通过，显示许可证信息
            System.out.println("许可证验证成功");
            System.out.println("授权机器码: " + licensedMachineCode);
            System.out.println("有效期至: " + expireDate);

            // 计算剩余天数
            long diffInMillis = expireDateObj.getTime() - currentDate.getTime();
            long daysRemaining = diffInMillis / (1000 * 60 * 60 * 24);
            System.out.println("剩余有效天数: " + daysRemaining + " 天");

            return true;
        } catch (Exception e) {
            System.err.println("许可证验证失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * AES解密
     */
    private static String decrypt(byte[] encryptedData) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decrypted = cipher.doFinal(encryptedData);
        return new String(decrypted, "UTF-8");
    }

    /**
     * 验证许可证并返回错误信息
     *
     * @return 错误信息，如果验证通过则返回null
     */
    public static String validateWithMessage() {
        try {
            // 检查许可证文件是否存在
            File licenseFile = new File(LICENSE_FILE_PATH);
            if (!licenseFile.exists()) {
                String machineCode = MachineCodeUtil.getMachineCode();
                return "未找到许可证文件 [" + LICENSE_FILE_PATH + "]，请联系管理员获取有效的许可证文件。当前机器码: " + machineCode;
            }

            // 读取许可证文件内容
            byte[] encryptedData = Files.readAllBytes(Paths.get(LICENSE_FILE_PATH));

            // 检查文件是否为空
            if (encryptedData == null || encryptedData.length == 0) {
                String machineCode = MachineCodeUtil.getMachineCode();
                return "许可证文件为空，请联系管理员获取有效的许可证文件。当前机器码: " + machineCode;
            }

            // 解密许可证
            String decryptedData;
            try {
                decryptedData = decrypt(encryptedData);
            } catch (Exception e) {
                String machineCode = MachineCodeUtil.getMachineCode();
                return "许可证文件解密失败，文件可能已损坏。请联系管理员获取有效的许可证文件。当前机器码: " + machineCode;
            }

            // 检查解密结果
            if (decryptedData == null || decryptedData.trim().isEmpty()) {
                String machineCode = MachineCodeUtil.getMachineCode();
                return "许可证文件内容无效，请联系管理员获取有效的许可证文件。当前机器码: " + machineCode;
            }

            // 解析许可证信息（JSON格式）
            JSONObject licenseInfo;
            try {
                licenseInfo = JSON.parseObject(decryptedData);
            } catch (Exception e) {
                String machineCode = MachineCodeUtil.getMachineCode();
                return "许可证数据格式错误，请联系管理员获取有效的许可证文件。当前机器码: " + machineCode;
            }
            
            if (licenseInfo == null) {
                String machineCode = MachineCodeUtil.getMachineCode();
                return "许可证数据格式错误，请联系管理员获取有效的许可证文件。当前机器码: " + machineCode;
            }

            String licensedMachineCode = licenseInfo.getString("machineCode");
            String expireDate = licenseInfo.getString("expireDate");

            // 检查必要字段是否存在
            if (licensedMachineCode == null || licensedMachineCode.trim().isEmpty()) {
                String machineCode = MachineCodeUtil.getMachineCode();
                return "许可证中缺少机器码信息，请联系管理员获取有效的许可证文件。当前机器码: " + machineCode;
            }

            if (expireDate == null || expireDate.trim().isEmpty()) {
                String machineCode = MachineCodeUtil.getMachineCode();
                return "许可证中缺少有效期信息，请联系管理员获取有效的许可证文件。当前机器码: " + machineCode;
            }

            // 获取当前机器码
            String currentMachineCode = MachineCodeUtil.getMachineCode();

            // 1. 验证机器码是否匹配
            if (!currentMachineCode.equals(licensedMachineCode)) {
                return "许可证与当前机器不匹配，请联系管理员获取有效的许可证文件。当前机器码: " + currentMachineCode;
            }

            // 2. 验证有效期
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date expireDateObj;
            try {
                expireDateObj = sdf.parse(expireDate);
            } catch (Exception e) {
                String machineCode = MachineCodeUtil.getMachineCode();
                return "许可证有效期格式错误，请联系管理员获取有效的许可证文件。当前机器码: " + machineCode;
            }
            Date currentDate = new Date();

            if (currentDate.after(expireDateObj)) {
                return "许可证已过期，过期时间: " + expireDate + "，请联系管理员续期许可证。当前机器码: " + currentMachineCode;
            }

            // 验证通过
            return null;
        } catch (Exception e) {
            String machineCode = MachineCodeUtil.getMachineCode();
            return "许可证验证失败: " + e.getMessage() + "，请联系管理员获取有效的许可证文件。当前机器码: " + machineCode;
        }
    }

    /**
     * 获取许可证文件路径
     */
    public static String getLicenseFilePath() {
        return LICENSE_FILE_PATH;
    }

    /**
     * 测试方法
     */
    public static void main(String[] args) {
        boolean isValid = validate();
        if (isValid) {
            System.out.println("许可证验证通过，程序可以正常启动");
        } else {
            System.out.println("许可证验证失败，程序将无法启动");
            System.exit(1);
        }
    }
}
