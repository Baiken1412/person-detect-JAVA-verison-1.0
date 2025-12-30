# 资产视频监控系统 - 部署手册

**版本**: 4.7.8
**最后更新**: 2025-12-27
**适用环境**: Windows Server / Linux

---

## 📋 目录

1. [环境要求](#环境要求)
2. [准备工作](#准备工作)
3. [数据库部署](#数据库部署)
4. [应用配置](#应用配置)
5. [应用部署](#应用部署)
6. [SSL证书配置](#ssl证书配置)
7. [启动验证](#启动验证)
8. [常见问题](#常见问题)

---

## 环境要求

### 硬件要求
- **CPU**: 4核以上
- **内存**: 8GB以上（推荐16GB）
- **磁盘**:
  - 系统盘：50GB以上
  - 数据盘：500GB以上（用于存储视频文件）
  - 可选：磁盘阵列（用于长期存储）

### 软件要求
- **操作系统**: Windows Server 2016+ 或 CentOS 7+
- **JDK**: 1.8 (必须是1.8版本)
- **MySQL**: 5.7+ 或 8.0+
- **Redis**: 5.0+
- **Maven**: 3.6+ (开发环境)
- **中文字体**: 黑体/宋体/微软雅黑（用于PDF生成）

---

## 准备工作

### 1. 安装JDK 1.8

#### Windows环境
```cmd
1. 下载JDK 1.8: https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html
2. 安装到: C:\Program Files\Java\jdk1.8.0_xxx
3. 配置环境变量:
   - JAVA_HOME = C:\Program Files\Java\jdk1.8.0_xxx
   - Path 添加: %JAVA_HOME%\bin
4. 验证: java -version
```

#### Linux环境
```bash
# CentOS/RHEL
sudo yum install java-1.8.0-openjdk java-1.8.0-openjdk-devel

# Ubuntu/Debian
sudo apt-get install openjdk-8-jdk

# 验证
java -version
```

### 2. 安装MySQL

#### Windows环境
```cmd
1. 下载MySQL 8.0: https://dev.mysql.com/downloads/mysql/
2. 安装并设置root密码
3. 创建数据库: caseappdb
```

#### Linux环境
```bash
# CentOS/RHEL
sudo yum install mysql-server
sudo systemctl start mysqld
sudo systemctl enable mysqld

# Ubuntu/Debian
sudo apt-get install mysql-server

# 设置密码并创建数据库
mysql -u root -p
CREATE DATABASE caseappdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 安装Redis

#### Windows环境
```cmd
1. 下载Redis for Windows: https://github.com/microsoftarchive/redis/releases
2. 解压到: C:\Redis
3. 修改redis.windows.conf:
   requirepass 123456
4. 启动: redis-server.exe redis.windows.conf
```

#### Linux环境
```bash
# CentOS/RHEL
sudo yum install redis
sudo systemctl start redis
sudo systemctl enable redis

# 设置密码
redis-cli
CONFIG SET requirepass "123456"
CONFIG REWRITE
```

---

## 数据库部署

### 1. 导入数据库脚本

```bash
# 方式1：命令行导入
mysql -u root -p caseappdb < sql/caseappdb.sql

# 方式2：使用Navicat或其他工具
1. 连接到MySQL
2. 选择数据库 caseappdb
3. 导入SQL文件
```

### 2. 验证数据库

```sql
USE caseappdb;

-- 检查关键表是否存在
SHOW TABLES LIKE 'app_%';
SHOW TABLES LIKE 'sys_%';

-- 检查复合事件表
SELECT COUNT(*) FROM app_composite_event;

-- 检查轨迹表
SELECT COUNT(*) FROM app_track;
```

---

## 应用配置

### 1. 修改数据库配置

**文件位置**: `src/main/resources/application-druid.yml`

```yaml
spring:
    datasource:
        druid:
            master:
                url: jdbc:mysql://localhost:3306/caseappdb?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
                username: root
                password: 你的数据库密码
```

### 2. 修改Redis配置

**文件位置**: `src/main/resources/application.yml`

```yaml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
    password: 你的Redis密码
```

### 3. 修改文件上传路径

**文件位置**: `src/main/resources/application.yml`

```yaml
ruoyi:
  # Windows示例
  profile: D:/ruoyi/uploadPath

  # Linux示例
  # profile: /home/ruoyi/uploadPath
```

**创建上传目录**:
```bash
# Windows
mkdir D:\ruoyi\uploadPath

# Linux
sudo mkdir -p /home/ruoyi/uploadPath
sudo chown -R tomcat:tomcat /home/ruoyi/uploadPath
```

### 4. 修改海康配置（如果使用海康摄像头）

**文件位置**: `src/main/resources/application.yml`

```yaml
hkpt:
  # 视频下载文件夹
  xzwjjmc: E:\apache-tomcat-9.0.68\webapps\videopath
  # 海康第三方IP
  ip: 192.168.1.21
  # 海康第三方端口
  port: 443
  # 海康第三方APPKEY
  appKey: 你的AppKey
  # 海康第三方APPSECRET
  appSecret: 你的AppSecret
  # 业务区业务中心编号
  baqbh: 你的编号
```

### 5. 修改服务端口（可选）

**文件位置**: `src/main/resources/application.yml`

```yaml
server:
  port: 8090  # 修改为你想要的端口
```

---

## 应用部署

### 方式1：使用Maven打包部署（推荐）

#### Step 1: 编译打包

```bash
# 进入项目目录
cd D:\work\caseapp

# Windows
mvn clean package -DskipTests

# Linux
./mvnw clean package -DskipTests
```

打包成功后，会生成: `target/ruoyi.jar`

#### Step 2: 上传到服务器

```bash
# 使用SCP上传（Linux）
scp target/ruoyi.jar user@server:/opt/app/

# 或使用FTP工具上传到服务器
```

#### Step 3: 创建启动脚本

**Windows启动脚本** (`start.bat`):
```batch
@echo off
echo 正在启动资产视频监控系统...
java -jar -Xms512m -Xmx2048m ruoyi.jar
pause
```

**Linux启动脚本** (`start.sh`):
```bash
#!/bin/bash
echo "正在启动资产视频监控系统..."

# 设置JVM参数
JAVA_OPTS="-Xms512m -Xmx2048m -XX:+UseG1GC"

# 后台启动
nohup java $JAVA_OPTS -jar ruoyi.jar > logs/app.log 2>&1 &

echo "启动完成，PID: $!"
echo "查看日志: tail -f logs/app.log"
```

**Linux停止脚本** (`stop.sh`):
```bash
#!/bin/bash
echo "正在停止资产视频监控系统..."

# 查找进程
PID=$(ps -ef | grep ruoyi.jar | grep -v grep | awk '{print $2}')

if [ -z "$PID" ]; then
    echo "未找到运行中的进程"
else
    kill -15 $PID
    echo "已发送停止信号，PID: $PID"

    # 等待进程结束
    sleep 5

    # 检查是否成功停止
    if ps -p $PID > /dev/null; then
        echo "进程未正常停止，强制终止..."
        kill -9 $PID
    fi

    echo "停止完成"
fi
```

#### Step 4: 赋予执行权限（Linux）

```bash
chmod +x start.sh
chmod +x stop.sh
```

#### Step 5: 启动应用

```bash
# Windows
start.bat

# Linux
./start.sh
```

### 方式2：使用systemd管理（Linux推荐）

#### Step 1: 创建服务文件

**文件位置**: `/etc/systemd/system/app-service.service`

```ini
[Unit]
Description=资产视频监控系统
After=network.target mysql.service redis.service

[Service]
Type=simple
User=ruoyi
Group=ruoyi
WorkingDirectory=/opt/app/app-service
ExecStart=/usr/bin/java -Xms512m -Xmx2048m -XX:+UseG1GC -jar /opt/app/app-service/ruoyi.jar
ExecStop=/bin/kill -15 $MAINPID
Restart=on-failure
RestartSec=10
StandardOutput=append:/opt/app/app-service/logs/stdout.log
StandardError=append:/opt/app/app-service/logs/stderr.log

[Install]
WantedBy=multi-user.target
```

#### Step 2: 创建运行用户

```bash
sudo useradd -r -s /bin/false ruoyi
sudo mkdir -p /opt/app/app-service/logs
sudo chown -R ruoyi:ruoyi /opt/app/app-service
```

#### Step 3: 启用并启动服务

```bash
# 重新加载systemd配置
sudo systemctl daemon-reload

# 启用服务（开机自启）
sudo systemctl enable app-service

# 启动服务
sudo systemctl start app-service

# 查看状态
sudo systemctl status app-service

# 查看日志
sudo journalctl -u app-service -f
```

#### Step 4: 常用命令

```bash
# 启动
sudo systemctl start app-service

# 停止
sudo systemctl stop app-service

# 重启
sudo systemctl restart app-service

# 查看状态
sudo systemctl status app-service

# 查看日志
sudo journalctl -u app-service -f
```

---

## SSL证书配置

系统默认使用HTTPS，需要配置SSL证书。

### 1. 生成自签名证书（开发/测试环境）

```bash
# 进入项目resources目录
cd src/main/resources

# 生成keystore
keytool -genkey -alias tomcat -keyalg RSA -keysize 2048 -validity 3650 -keypass 123456 -storepass 123456 -keystore tomcat.keystore

# 填写证书信息
# CN=localhost
# OU=IT Department
# O=Your Company
# L=Your City
# ST=Your State
# C=CN
```

### 2. 使用正式证书（生产环境）

```bash
# 将你的证书文件复制到resources目录
cp your-cert.keystore src/main/resources/tomcat.keystore

# 修改application.yml中的密码
server:
  ssl:
    key-store: tomcat.keystore
    key-store-password: 你的keystore密码
    key-alias: tomcat
    key-password: 你的key密码
```

### 3. 禁用SSL（仅HTTP，不推荐）

**文件位置**: `src/main/resources/application.yml`

注释掉SSL配置:
```yaml
server:
  # ssl:
  #   key-store: tomcat.keystore
  #   key-store-password: 123456
  #   key-alias: tomcat
  #   key-password: 123456
```

---

## 启动验证

### 1. 检查端口占用

```bash
# Windows
netstat -ano | findstr "8090"

# Linux
netstat -tlnp | grep 8090
```

### 2. 访问系统

浏览器打开:
- **HTTPS**: https://localhost:8090
- **HTTP**: http://localhost:8090（如果禁用了SSL）

### 3. 默认账号

```
管理员账号: admin
密码: admin123
```

### 4. 检查关键功能

- ✅ 登录系统
- ✅ 查看首页（复合事件列表）: https://localhost:8090/caseapp/track
- ✅ 查看轨迹列表: https://localhost:8090/caseapp/track/eventList
- ✅ 导出事件包（选择事件后点击"导出事件包"按钮）
- ✅ 检查桌面是否生成导出文件夹

### 5. 检查日志

```bash
# 查看应用日志
tail -f logs/sys-info.log

# 查看错误日志
tail -f logs/sys-error.log

# 查看访问日志
tail -f logs/sys-access.log
```

---

## 常见问题

### Q1: 启动时报错"找不到或无法加载主类"

**原因**: JAVA_HOME配置错误或JDK版本不对

**解决**:
```bash
# 检查Java版本
java -version

# 必须是1.8版本
# 如果不是，请安装JDK 1.8
```

### Q2: 无法连接数据库

**原因**: 数据库配置错误或数据库未启动

**解决**:
```bash
# 检查MySQL是否运行
# Windows
sc query MySQL80

# Linux
systemctl status mysqld

# 测试数据库连接
mysql -h localhost -u root -p -D caseappdb

# 检查配置文件中的用户名密码是否正确
```

### Q3: 无法连接Redis

**原因**: Redis未启动或密码错误

**解决**:
```bash
# 检查Redis是否运行
# Windows
tasklist | findstr redis

# Linux
systemctl status redis

# 测试Redis连接
redis-cli -a 123456 ping

# 应返回 PONG
```

### Q4: 端口8090被占用

**解决**:
```bash
# 方式1: 修改端口
# 编辑 application.yml，修改 server.port

# 方式2: 停止占用端口的进程
# Windows
netstat -ano | findstr "8090"
taskkill /F /PID <PID>

# Linux
netstat -tlnp | grep 8090
kill -9 <PID>
```

### Q5: PDF导出失败，提示"无法找到中文字体"

**原因**: 系统未安装中文字体

**解决**:
```bash
# Windows: 检查 C:\Windows\Fonts\ 是否有以下字体
# - simhei.ttf (黑体)
# - simsun.ttc (宋体)
# - msyh.ttc (微软雅黑)

# Linux: 安装中文字体
sudo yum install wqy-microhei-fonts
# 或
sudo apt-get install fonts-wqy-microhei
```

### Q6: 事件包导出位置在哪里？

**默认位置**:
- Windows: `C:\Users\你的用户名\Desktop\事件包导出\`
- Linux: `/home/你的用户名/Desktop/事件包导出/`

### Q7: 内存不足，系统运行缓慢

**解决**: 调整JVM内存参数
```bash
# 编辑启动脚本，修改内存参数
java -jar -Xms1024m -Xmx4096m ruoyi.jar

# -Xms: 初始堆内存（建议1GB）
# -Xmx: 最大堆内存（建议4GB或更高）
```

### Q8: 视频文件无法播放

**原因**: 视频路径配置错误或文件不存在

**解决**:
```bash
# 检查配置
# application.yml中的 ruoyi.profile 路径
# 确保路径存在且有读写权限

# Windows
icacls D:\ruoyi\uploadPath /grant Everyone:F

# Linux
sudo chmod -R 755 /home/ruoyi/uploadPath
sudo chown -R ruoyi:ruoyi /home/ruoyi/uploadPath
```

---

## 性能优化建议

### 1. 数据库优化

```sql
-- 添加索引
ALTER TABLE app_composite_event ADD INDEX idx_start_time (start_time);
ALTER TABLE app_composite_event ADD INDEX idx_bzzt (bzzt);
ALTER TABLE app_track ADD INDEX idx_pssj (pssj);
ALTER TABLE app_track ADD INDEX idx_qymc (qymc);

-- 定期清理旧数据（可选）
DELETE FROM app_composite_event WHERE start_time < DATE_SUB(NOW(), INTERVAL 6 MONTH);
```

### 2. 数据库连接池

**文件位置**: `application-druid.yml`

```yaml
druid:
    initialSize: 10      # 增加初始连接数
    minIdle: 20         # 增加最小空闲连接
    maxActive: 50       # 增加最大活动连接
```

### 3. Redis缓存

```yaml
spring:
  redis:
    lettuce:
      pool:
        max-active: 200  # 增加最大连接数
        max-idle: 50     # 增加最大空闲连接
```

### 4. Tomcat线程池

```yaml
server:
  tomcat:
    threads:
      max: 800         # 根据并发量调整
      min-spare: 100
```

---

## 备份策略

### 1. 数据库备份

```bash
#!/bin/bash
# 每日备份脚本 (backup_db.sh)

BACKUP_DIR="/backup/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/caseappdb_$DATE.sql"

mkdir -p $BACKUP_DIR

mysqldump -u root -p123456 caseappdb > $BACKUP_FILE

# 压缩备份文件
gzip $BACKUP_FILE

# 删除30天前的备份
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete

echo "备份完成: $BACKUP_FILE.gz"
```

设置定时任务:
```bash
# 编辑crontab
crontab -e

# 每天凌晨2点执行备份
0 2 * * * /opt/scripts/backup_db.sh
```

### 2. 文件备份

```bash
#!/bin/bash
# 备份上传文件 (backup_files.sh)

SOURCE_DIR="/home/ruoyi/uploadPath"
BACKUP_DIR="/backup/files"
DATE=$(date +%Y%m%d)

rsync -av --delete $SOURCE_DIR $BACKUP_DIR/uploadPath_$DATE/

echo "文件备份完成: $BACKUP_DIR/uploadPath_$DATE/"
```

---

## 监控建议

### 1. 应用监控

访问Druid监控页面:
- URL: https://localhost:8090/druid/
- 账号: ruoyi
- 密码: 123456

### 2. 系统监控

```bash
# 检查磁盘空间
df -h

# 检查内存使用
free -h

# 检查CPU使用
top

# 检查Java进程
jps -l
```

### 3. 日志监控

```bash
# 实时查看错误日志
tail -f logs/sys-error.log

# 统计今日错误数量
grep "ERROR" logs/sys-error.log | grep $(date +%Y-%m-%d) | wc -l
```

---

## 联系支持

如遇到部署问题，请联系技术支持团队。

**部署手册版本**: v1.0
**最后更新时间**: 2025-12-27
