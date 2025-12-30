=====================================
   LICENSE SYSTEM - Quick Start Guide
=====================================

IMPORTANT: To avoid Chinese character encoding issues, use this English version tool!

=====================================
QUICK START
=====================================

Step 1: Double-click "LICENSE-TOOL-EN.bat"
Step 2: Select option [1] to get the machine code
Step 3: Select option [2] to generate license
        - Choose option 1 for current machine
        - OR option 2 for specific machine code
        - Enter validity days (default: 365 days / 1 year)
        - Press Enter to use default filename "license.dat"
Step 4: Copy "license.dat" to the target machine's program directory
Step 5: Run the program - it will auto-validate the license

=====================================
CURRENT MACHINE INFO
=====================================

Machine Code: C50D1B457934634C29DA135A458017A4

Note: This machine code is unique to this computer.
Use it to generate a license specifically for this machine.

=====================================
COMMAND LINE USAGE
=====================================

1. Get Machine Code:
   mvn exec:java -Dexec.mainClass="com.ruoyi.common.license.GetMachineCode" -q

2. Generate License:
   mvn exec:java -Dexec.mainClass="com.ruoyi.common.license.LicenseGenerator" -q

3. Validate License:
   mvn exec:java -Dexec.mainClass="com.ruoyi.common.license.LicenseValidator" -q

=====================================
LICENSE VALIDITY SETTINGS
=====================================

Default: 365 days (1 year)
Common settings:
- Trial version: 30 days
- 1 year: 365 days
- 2 years: 730 days
- Permanent: 36500 days (100 years)

=====================================
FAQ
=====================================

Q: Where should I put the license file?
A: Place "license.dat" in the same directory as "ruoyi.jar"

Q: How to change the default validity period?
A: Edit: src/main/java/com/ruoyi/common/license/LicenseGenerator.java
   Line 30: private static final int DEFAULT_VALIDITY_DAYS = 365;

Q: How to change the encryption key?
A: Edit both files and use the same key:
   - LicenseValidator.java Line 30: SECRET_KEY
   - LicenseGenerator.java Line 26: SECRET_KEY

Q: The license file is successfully generated in current directory!
A: Yes! Check for "license.dat" file (128 bytes)

=====================================
FILES INCLUDED
=====================================

Scripts:
- LICENSE-TOOL-EN.bat         Main tool (English, no encoding issues)
- 获取机器码.bat               Get machine code (Chinese)
- 生成许可证.bat               Generate license (Chinese)
- 测试许可证.bat               Test license (Chinese)
- *.ps1 files                PowerShell scripts (Chinese)

Documentation:
- LICENSE-README.txt         This file (English)
- 许可证使用说明.txt           Chinese documentation

Generated:
- license.dat                License file (encrypted)

=====================================
TROUBLESHOOTING
=====================================

If you see Chinese characters displayed incorrectly:
1. Use LICENSE-TOOL-EN.bat (English version, recommended)
2. The core functionality works fine regardless of display issues
3. Machine code output is always in English/numbers (no encoding issues)

=====================================
For more information, contact technical support
=====================================
