package com.tao.common.util;

import java.util.Random;

/**
 * 昵称生成工具
 */
public class NickNameUtil {

    private static final String[] PREFIX = {"快乐", "阳光", "微笑", "清风", "暖阳", "微风", "清晨", "午后",
            "星月", "云端", "林间", "远山", "海风", "麦田", "漫步", "聆听", "追风", "寻梦"};
    private static final String[] SUFFIX = {"少年", "旅人", "先生", "同学", "学妹", "学长", "学姐",
            "宝宝", "达人", "玩家", "用户", "居民", "过客", "故人", "新人", "达人"};

    private static final Random RANDOM = new Random();

    /**
     * 生成随机昵称
     */
    public static String generate() {
        return generate(8);
    }

    /**
     * 生成指定长度的随机昵称
     * 长度小于等于 8 时使用 "前缀+后缀" 模式
     * 长度大于 8 时在尾部追加随机数字
     */
    public static String generate(int length) {
        if (length <= 0) {
            return generate();
        }
        String prefix = PREFIX[RANDOM.nextInt(PREFIX.length)];
        String suffix = SUFFIX[RANDOM.nextInt(SUFFIX.length)];
        String base = prefix + suffix;
        if (length <= base.length()) {
            return base.substring(0, length);
        }
        StringBuilder sb = new StringBuilder(base);
        for (int i = base.length(); i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}