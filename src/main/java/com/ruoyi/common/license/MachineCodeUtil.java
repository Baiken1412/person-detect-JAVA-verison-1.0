package com.ruoyi.common.license;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Enumeration;


public class MachineCodeUtil {

    /**
     * 获取机器码
     * 基于MAC地址、操作系统信息和CPU信息生成唯一标识
     *
     * @return 机器码字符串
     */
    public static String getMachineCode() {
        try {
            StringBuilder sb = new StringBuilder();

            // 获取MAC地址
            String macAddress = getMacAddress();
            sb.append(macAddress);

            // 获取操作系统信息
            String osName = System.getProperty("os.name");
            String osArch = System.getProperty("os.arch");
            sb.append(osName).append(osArch);

            // 获取CPU序列号（通过处理器标识）
            String cpuId = getCpuId();
            sb.append(cpuId);

            // 使用MD5加密生成最终机器码
            return getMD5(sb.toString());
        } catch (Exception e) {
            throw new RuntimeException("获取机器码失败", e);
        }
    }

    /**
     * 获取MAC地址
     */
    private static String getMacAddress() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);

            if (network == null) {
                // 如果获取不到，遍历所有网络接口
                Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
                while (networks.hasMoreElements()) {
                    network = networks.nextElement();
                    if (network != null && !network.isLoopback() && network.isUp()) {
                        break;
                    }
                }
            }

            if (network != null) {
                byte[] mac = network.getHardwareAddress();
                if (mac != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X", mac[i]));
                        if (i < mac.length - 1) {
                            sb.append("-");
                        }
                    }
                    return sb.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取CPU标识
     */
    private static String getCpuId() {
        try {
            // 获取处理器数量和可用处理器数量作为标识的一部分
            int processors = Runtime.getRuntime().availableProcessors();
            return String.valueOf(processors);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * MD5加密
     */
    private static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }

    /**
     * 主方法，用于测试获取当前机器的机器码
     */
    public static void main(String[] args) {
        String machineCode = getMachineCode();
        System.out.println("当前机器码: " + machineCode);
    }
}
