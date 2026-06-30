package com.web.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class CommonUtil {
    
    public static String getRandomString(Integer num) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < num; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    
    public static void checkMap(Map<String, Object> params) {
        boolean page=true,limit=true,sort=true,order=true;
        Iterator<Map.Entry<String, Object>> iter = params.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Object> info = iter.next();
            Object key = info.getKey();
            Object value = info.getValue();
            if(value==null || "".equals(value) || "null".equals(value)){
                iter.remove();
            }else if("page".equals(key)){page=false;
            }else if("limit".equals(key)){limit=false;
            }else if("sort".equals(key)){sort=false;
            }else if("order".equals(key)){order=false;
            }
        }
        if(page){params.put("page","1");}
        if(limit){params.put("limit","10");}
        if(sort){params.put("sort","id");}
        if(order){params.put("order","desc");}
    }
}
