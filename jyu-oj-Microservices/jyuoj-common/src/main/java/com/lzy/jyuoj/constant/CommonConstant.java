package sspu.zzx.sspuoj.constant;

/**
 * 通用常量
 *
 * @author ZZX
 * @from SSPU
 */
public interface CommonConstant
{

    /**
     * 升序
     */
    String SORT_ORDER_ASC = "ascend";

    /**
     * 降序
     */
    String SORT_ORDER_DESC = "descend";

    /**
     * 保存智能对话提问问题的内容key前缀
     */
    String AI_CHAT_PREFIX = "AI-CHAT-TOKEN:";

    /**
     * 保存智能对话的历史记录key前缀
     */
    String AI_CHAT_HISTORY_PREFIX = "AI-CHAT-HISTORY-TOKEN:";

    /**
     * rsa私钥键值
     */
    String RSA_PRIVATE_KEY = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMA1NecIretk9vRXgosnssQS2maP\n" +
            "Qb7UeMDxDd6Opp6xoPVMDR2onNvS+TjtP1Vn8NGmkKVJwsYgyt96gROjpgULsyvaV5O+EyxgWpse\n" +
            "iduinov44O+ANRTPiLuH8fLtzTz7VzXwOe1Et7ehWje/DMyE1OqYh7HTcFhKC55j4qJjAgMBAAEC\n" +
            "gYBk3MdveJ+DslThffaUPX3S5K3VOgHfOTIw1Y5YpvUKu93iO0l6sdu2g2yjEeS1VbkK22R2SFG8\n" +
            "lMLBKVEI1Eyu2RM5khK7PjepJdC4skh7TwygJjxkY7UtEZxRQ0+BayhckWLhEyCF78UhR9cgKGtS\n" +
            "/7pYsaj04CwRva9PexNDaQJBAPY/rAn+wdpaBRJ5dDwqpUisVlLvCYcsvYojynQqOVi/tRukUtua\n" +
            "gQzRfjlDhF/t1gHU3GVkGzsNucycsF/HtScCQQDH0bQLenLa1ghk3AjAbyzEaHAmb5b+RNDpc/Tx\n" +
            "gM9OR10eU+DXBVIPAo4Ug5a1dy0oBPkB11378Gq2ce+RpMZlAkEAkA4Rk8tQFm+hFfytLeF4zbnb\n" +
            "yiqCdWyL+TsU3b4xzCRiS6hmvId3RUtsvw0rbH8TOgEPadUVRQezkOp9F5sZ8wJBAJBcnOs+CAS6\n" +
            "ZU4Y+emtaHZtzbi3HiuNpDFFqU1hdmRjgo8KooI2Qda+Tc/cUeHAvEsRbKU755bBVxeAsUhT91EC\n" +
            "QFIKdtNZM+HDXIFcofN21zoLk0VTAZ5tj8jzRyF1bKdaf6EtEH1L3c+CZ/q5UGzPjcTv3X8F3Awq\n" +
            "EFwU8cvhT9Q=\n";

    /**
     * 定时任务时间周期，单位为秒
     */
    int QUARTZ_TIME_PERIOD = 60 * 60 * 2;

    /**
     * 邮箱验证码有效期：5分钟
     */
    int EMAIL_CODE_TIMEOUT = 60 * 5;

    /**
     * ai对话有效期：10分钟
     */
    int AI_TIME_OUT = 60 * 10;

}
