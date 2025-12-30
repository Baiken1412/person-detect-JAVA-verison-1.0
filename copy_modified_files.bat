@echo off
chcp 65001 >nul
echo ========================================
echo 复制修改文件到临时目录
echo ========================================

set TARGET_DIR=modified_files_%date:~0,4%%date:~5,2%%date:~8,2%

echo.
echo 创建目标目录: %TARGET_DIR%
mkdir "%TARGET_DIR%"
mkdir "%TARGET_DIR%\templates"
mkdir "%TARGET_DIR%\templates\track"
mkdir "%TARGET_DIR%\java"
mkdir "%TARGET_DIR%\config"
mkdir "%TARGET_DIR%\static"
mkdir "%TARGET_DIR%\sql"

echo.
echo 复制前端模板文件...
copy "src\main\resources\templates\sy.html" "%TARGET_DIR%\templates\" >nul
copy "src\main\resources\templates\track\event-list.html" "%TARGET_DIR%\templates\track\" >nul
copy "src\main\resources\templates\track\daily-report.html" "%TARGET_DIR%\templates\track\" >nul

echo 复制Java文件...
copy "src\main\java\com\ruoyi\project\caseapp\track\service\impl\CompositeEventServiceImpl.java" "%TARGET_DIR%\java\" >nul
copy "src\main\java\com\ruoyi\common\config\SystemConfig.java" "%TARGET_DIR%\java\" >nul
copy "src\main\java\com\ruoyi\project\system\config\controller\SystemConfigController.java" "%TARGET_DIR%\java\" >nul
copy "src\main\java\com\ruoyi\framework\config\ShiroConfig.java" "%TARGET_DIR%\java\" >nul

echo 复制配置文件...
copy "src\main\resources\application.yml" "%TARGET_DIR%\config\" >nul 2>&1
if errorlevel 1 (
    copy "src\main\resources\application-druid.yml" "%TARGET_DIR%\config\" >nul 2>&1
)

echo 复制静态资源...
copy "src\main\resources\static\图片1.png" "%TARGET_DIR%\static\" >nul 2>&1
if errorlevel 1 (
    echo [警告] 未找到图片1.png，请手动复制
)

echo 复制SQL脚本...
copy "insert_dict_data.sql" "%TARGET_DIR%\sql\" >nul 2>&1
copy "cleanup_duplicate_dict.sql" "%TARGET_DIR%\sql\" >nul 2>&1

echo.
echo ========================================
echo 复制完成！
echo 文件已复制到: %TARGET_DIR%
echo ========================================
echo.
echo 请将整个 %TARGET_DIR% 文件夹复制到公司电脑
echo.

pause
