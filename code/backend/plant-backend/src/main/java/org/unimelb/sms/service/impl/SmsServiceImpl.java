package org.unimelb.sms.service.impl;

//import com.qqcn.sms.service.SmsService;
//import com.qqcn.sms.utils.AliyunSmsUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//
//import java.util.Random;
//import java.util.concurrent.TimeUnit;

//@Service
//@Slf4j
//public class SmsServiceImpl implements SmsService {
//    @Autowired
//    private AliyunSmsUtil aliyunSmsUtil;
//    @Autowired
//    private RedisTemplate redisTemplate;
//
//    @Override
//    public void sendSmsCapthcha(String phone) {
//        // 生成6位随机数
//        Random random = new Random();
//        String code = String.format("%06d", random.nextInt(1000000));
//        // 发送
//        //aliyunSmsUtil.sendSms(phone, code);
//        log.debug("-----------> 发送验证码：" + code);
//        // 存入redis
//        redisTemplate.opsForValue().set(getCaptchaKey(phone, code), code, 5, TimeUnit.MINUTES);
//    }
//
//
//    @Override
//    public Boolean checkCapthcha(String phone, String code) {
//        String key = getCaptchaKey(phone, code);
//        Object obj = redisTemplate.opsForValue().get(key);
//        return obj != null ? true : false;
//    }
//
//    private String getCaptchaKey(String phone, String code) {
//        return "sms:" + phone + ":" + code;
//    }
//}
