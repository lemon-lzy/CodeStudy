package com.lzy.codestudybackend.constant;

public interface RedisConstant {

    /**
     * 用户签到记录的 Redis Key 前缀
     */
    String USER_SIGN_IN_REDIS_KEY_PREFIX = "user:signins";

    /**
     * 获取用户签到记录的 Redis Key
     * @param year 年份
     * @param userId 用户 id
     * @return 拼接好的 Redis Key  user:sign_in:年份：用户ID
     */
    static String getUserSignInRedisKey(int year, long userId) {
        return String.format("%s:%s:%s", USER_SIGN_IN_REDIS_KEY_PREFIX, year, userId);
    }


    /**
     * 获取用户签到排行榜 Redis Key
     * 例返回 code-study:user:signs_in:rank:2025
     */
    static String getUserSignInRankKey(int year) {
        return String.format("code-study:user:sign_in:rank:%d", year);
    }

    /**
     * 获取用户月度签到排行榜 Redis Key
     * 例返回 code-study:user:sign_in:rank:2025:08
     */
    static String getUserSignInMonthlyRankKey(int year, int month) {
        return String.format("code-study:user:sign_in:rank:%d:%02d", year, month);
    }

    /**
     * 获取用户总签到排行榜 Redis Key
     * 返回code-study:user:sign_in:rank:total
     */
    static String getUserSignInTotalRankKey() {
        return "code-study:user:sign_in:rank:total";
    }

}
