//package com.qqcn.sms.utils;
//
//import com.aliyun.dysmsapi20170525.Client;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//@Component
//public class AliyunSmsUtil {
//
//    @Value("${wj.aliyun.accessKeyId}")
//    private String accessKeyId;
//    @Value("${wj.aliyun.accessKeySecret}")
//    private String accessKeySecret;
//    @Value("${wj.aliyun.signName}")
//    private String signName;
//    @Value("${wj.aliyun.templateCode}")
//    private String templateCode;
//
//    /**
//     * @param phone 电话
//     * @param code 验证码
//     * @return
//     */
//    public boolean sendSms(String phone,String code){
//        try {
//            Client client = createClient(accessKeyId, accessKeySecret);
//
//            com.aliyun.dysmsapi20170525.models.SendSmsRequest sendSmsRequest = new com.aliyun.dysmsapi20170525.models.SendSmsRequest()
//                    .setSignName(signName)
//                    .setTemplateCode(templateCode)
//                    .setPhoneNumbers(phone)
//                    .setTemplateParam("{\"code\":\""+code+"\"}");
//            com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
//
//            client.sendSmsWithOptions(sendSmsRequest, runtime);
//
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    /**
//     * 使用AK&SK初始化账号Client
//     * @param accessKeyId
//     * @param accessKeySecret
//     * @return Client
//     * @throws Exception
//     */
//    public  com.aliyun.dysmsapi20170525.Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
//        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
//                // 必填，您的 AccessKey ID
//                .setAccessKeyId(accessKeyId)
//                // 必填，您的 AccessKey Secret
//                .setAccessKeySecret(accessKeySecret);
//        // Endpoint 请参考 https://api.aliyun.com/product/Dysmsapi
//        config.endpoint = "dysmsapi.aliyuncs.com";
//        return new com.aliyun.dysmsapi20170525.Client(config);
//    }
//
//
//}