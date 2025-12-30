package com.ruoyi.common.license;

/**
 * Simple tool to get machine code without Chinese characters
 * Usage: mvn exec:java -Dexec.mainClass="com.ruoyi.common.license.GetMachineCode" -q
 */
public class GetMachineCode {
    public static void main(String[] args) {
        String machineCode = MachineCodeUtil.getMachineCode();
        System.out.println("=====================================");
        System.out.println("Machine Code / 机器码");
        System.out.println("=====================================");
        System.out.println(machineCode);
        System.out.println("=====================================");
    }
}
