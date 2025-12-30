package com.ruoyi.project.caseapp.core.task;

import com.alibaba.fastjson.JSONObject;
import com.ruoyi.framework.config.RuoYiConfig;
import com.ruoyi.project.caseapp.track.domain.AppTrack;
import com.ruoyi.project.caseapp.track.service.IAppTrackService;
import com.ruoyi.common.utils.IpUtils;
import com.ruoyi.project.caseapp.roomip.domain.AppRoomip;
import com.ruoyi.project.caseapp.roomip.service.IAppRoomipService;
import com.ruoyi.project.caseapp.core.util.HikVedioUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component("caseappTask")
public class CaseappTask {

    @Autowired
    private IAppTrackService appTrackService;

    @Autowired
    private IAppRoomipService appRoomipService;

    @Value("${server.port}")
    private String serverPort;
    @Value("${hkpt.ip}")
    private String ip;
    @Value("${hkpt.port}")
    private String port;
    @Value("${hkpt.appKey}")
    private String appKey;
    @Value("${hkpt.appSecret}")
    private String appSecret;
    private static final String FFMPEG_PATH = "ffmpeg";
    // 视频下载接口的地址/api/video/v1/cameras/playbackURLs
    String url = "/api/video/v1/cameras/playbackURLs";


    public void caseapp(){

        ExecutorService executorService = Executors.newFixedThreadPool(5);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        int gpu = 0;
        // 修改：使用getRealHostIp()替代getHostIp()，解决公司环境下获取到localhost的问题
        String cxip = IpUtils.getRealHostIp();
        System.out.println("视频下载任务使用的IP地址: " + cxip);
        HikVedioUtil hikVedioUtil = new HikVedioUtil();
        hikVedioUtil.fz(ip,port,appKey,appSecret);

        AppTrack appTrack = new AppTrack();
        appTrack.setJqzt("0");
        List<AppTrack> appTracks = appTrackService.selectAppTrackList(appTrack);
        for (AppTrack track : appTracks){
            if(track.getPssj()!=null){
                AppRoomip appRoomip = appRoomipService.selectAppRoomipById(track.getQyid());
                    String wjdz = RuoYiConfig.getProfile()+"/"+track.getId()+".mp4";
                    String wjmc = track.getId()+".mp4";
                    Map<String,Object> map = new HashMap<>();

                    // 获取开始和结束时间
                    Date kssjNew = track.getPssj();
                    Date jssjNew = track.getJssj();

                    // 保护逻辑：如果jssj为NULL，使用默认时长
                    // 这种情况通常发生在人员只被检测到一次，没有触发记录合并的情况
                    if (jssjNew == null && kssjNew != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(kssjNew);
                        // 默认截取30秒视频（可根据实际需求调整）
                        cal.add(Calendar.SECOND, 30);
                        jssjNew = cal.getTime();
                        // 记录警告日志
                        System.out.println("警告：轨迹记录ID=" + track.getId() + " 的jssj为NULL，使用默认时长30秒");
                    }

                    map.put("kssj",kssjNew);
                    map.put("jssj",jssjNew);
                    map.put("ip",appRoomip.getIp());
                    map.put("wjdz",wjdz);
                    map.put("wjmc",wjmc);
                    map.put("gpu",gpu % 2);
                    gpu++;
                    track.setJqzt("2");
                    // 计算视频时长：jssjNew减去kssjNew，转换为秒
                    if(kssjNew != null && jssjNew != null) {
                        long durationMillis = jssjNew.getTime() - kssjNew.getTime();
                        long durationSeconds = durationMillis / 1000;
                        track.setSpsc(String.valueOf(durationSeconds));
                    }
                    appTrackService.updateAppTrack(track);
                    executorService.submit(() ->{
                        try{
                            JSONObject jsonObject = new JSONObject();
                            // 将日期转换为ISO 8601 格式进行传参
                            String kssj = hikVedioUtil.getISO8601TimestampFromDateStr((Date) map.get("kssj"));
                            String jssj = hikVedioUtil.getISO8601TimestampFromDateStr((Date) map.get("jssj"));
                            jsonObject.put("cameraIndexCode",(String) map.get("ip"));  // 摄像头的唯一标识，应该是从数据库里获取
                            jsonObject.put("recordLocation",0); // 存储类型,0：中心存储 1：设备存储
                            jsonObject.put("protocol","rtsp");    // 取流协议
                            jsonObject.put("transmode",1);     // 传输协议 1是TCP，0是udp
                            jsonObject.put("beginTime",kssj);      // 需要截取的开始时间 ISO8601
                            jsonObject.put("endTime",jssj);       // 需要截取的结束时间 ISO8601
                            jsonObject.put("uuid","");
                            // 调用接口 （视频截取的话应该是返回一个rtsp的视频流）
                            String result = hikVedioUtil.vedioCut(url, jsonObject);
                            // 根据调用接口返回的参数拿到返回的rtsp数据
                            String urlrtsp = hikVedioUtil.getContentByJson(result, "url");
                            if(urlrtsp!=null){
                                urlrtsp = urlrtsp+"?beginTime="+kssj+"&endTime="+jssj;
                                int exitCode = convertToMp4((int) map.get("gpu"),urlrtsp,(String) map.get("wjdz"));
                                if(exitCode==0){
                                    track.setJqzt("1");
                                    track.setSpdz("https://"+cxip+":"+serverPort+"/profile/"+wjmc);
                                }else{
                                    track.setJqzt("2");
                                }
                            }else{
                                track.setJqzt("2");
                            }
                        }catch (Exception e){
                            track.setJqzt("2");
                        }
                        appTrackService.updateAppTrack(track);
                    });
                }
        }
    }

    /**
     * 视频转码
     * */
    private Integer convertToMp4(Integer gpu, String urlrtsp, String wjmc) {
        int exitCode = 0;
        try {
            //启用GPU
            /*String[] command = {
                    FFMPEG_PATH,
                    "-err_detect", "ignore_err",
                    "-y",
                    "-hwaccel_device", String.valueOf(gpu),
                    "-hwaccel", "cuda",
                    "-i", urlrtsp,
                    "-c:v", "h264_nvenc",wjmc
            };*/
            //启用CPU
            String[] command = {
                    FFMPEG_PATH,
                    "-err_detect", "ignore_err",
                    "-y",
                    "-rtsp_transport", "tcp",
                    "-i", urlrtsp,  // 输入RTSP流
                    "-c:v", "libx264",  // 替换为CPU编码器
                    wjmc  // 输出文件路径
            };
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.inheritIO();
            Process process = processBuilder.start();

            exitCode = process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exitCode;
    }

}
