package com.fengchao.miniapp.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Random;

public class RandomString {

    public static String buildRefundNo(String appId){
        Long timeStampMs = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        Long timeStampS = timeStampMs/1000;
        String timeStamp = timeStampS.toString();
        Random random = new Random();

        String triRandom = random.nextInt(1000) + "";
        StringBuilder sb = new StringBuilder();
        int randLength = triRandom.length();
        if (randLength < 3) {
            for (int i = 1; i <= 3 - randLength; i++)
                sb.append("0");
        }
        sb.append(triRandom);

        return appId + timeStamp + sb.toString();

    }
}
