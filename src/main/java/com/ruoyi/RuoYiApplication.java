package com.ruoyi;

import com.ruoyi.common.license.LicenseValidator;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;

/**
 * 启动程序
 *
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class RuoYiApplication
{
    public static void main(String[] args)
    {
        // ==================== 许可证验证（已临时禁用） ====================
        // 注意：正式部署时请取消注释以启用许可证验证
        /*
        System.out.println("正在验证许可证...");
        boolean isLicenseValid = LicenseValidator.validate();

        if (!isLicenseValid) {
            System.err.println("========================================");
            System.err.println("  许可证验证失败，程序无法启动！");
            System.err.println("  请联系管理员获取有效的许可证文件");
            System.err.println("========================================");
            System.exit(1); // 退出程序
            return;
        }
        System.out.println("许可证验证通过，正在启动系统...");
        System.out.println("========================================");
        */
        // ==================== 许可证验证结束 ====================

        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(RuoYiApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  若依启动成功   ლ(´ڡ`ლ)ﾞ");
    }
    @Bean
    public ServletWebServerFactory servletContainer(){
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(createHTTPConnector());
        return tomcat;
    }
    private Connector createHTTPConnector(){
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setSecure(false);
        connector.setPort(8099);//http端口
        return connector;
    }
}