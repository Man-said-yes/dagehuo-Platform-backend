package com.gdmu.util;

import java.util.Random;

public class NicknameGenerator {
    private static final Random random = new Random();

    private static final String[] NICK_PREFIX = {
        "晚风", "星河", "山野", "雾屿", "甜柚", "软糖", "清川", "凛冬",
        "温茶", "碎冰", "星野", "云栖", "橘猫", "鹿屿", "屿风", "森屿",
        "奶盖", "海盐", "桃酥", "青柠", "墨染", "月落", "风栖", "南枝",
        "雾岛", "浅夏", "凉笙", "安夏", "初晴", "晚星", "雾眠", "枕星",
        "橘子", "荔枝", "芋圆", "泡芙", "芝士", "气泡", "晚风", "雾里",
        "赴月", "观星", "折月", "揽星", "枕风", "渡川", "寻月", "知南"
    };

    private static final String[] NICK_CORE = {
        "旅人", "信笺", "拾光", "寄信", "寻梦", "赴野", "听风", "观海",
        "告白", "心动", "漫游", "出逃", "日记", "心事", "岛屿", "晚风",
        "邮递员", "收藏家", "空想家", "漫游者", "追光者", "拾荒者",
        "小记", "随笔", "来信", "日落", "日出", "山海", "月亮", "星星",
        "汽水", "晚风", "温柔", "浪漫", "欢喜", "偏爱", "余生", "梦境",
        "星河", "山野", "雾霭", "晴空", "晚风", "星光", "云朵", "鲸落"
    };

    private static final String[] NICK_SUFFIX = {
        "", "°", "✨", "⁺", "₊", "01", "07", "11", "17", "23",
        "の", "♪", "✿", "·", "゛", "づ", "ち", "ξ", "ʚ", "ɞ",
        "_", "-", "•", "◦", "⋆", "₊˚", "˚⁺", "˖", "✧", "✦",
        "゜", "ゝ", "ゞ", "っ", "り", "ゅ", "ょ", "ー", "゛", "゜"
    };

    public static String generate() {
        String prefix = NICK_PREFIX[random.nextInt(NICK_PREFIX.length)];
        String core = NICK_CORE[random.nextInt(NICK_CORE.length)];
        String suffix = NICK_SUFFIX[random.nextInt(NICK_SUFFIX.length)];

        return prefix + core + suffix;
    }

    public static String generateWithNumber() {
        String nickname = generate();
        int number = random.nextInt(1000);
        return nickname + number;
    }
}
