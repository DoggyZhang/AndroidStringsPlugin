package com.doggyzhang.plugin.checker;

import org.apache.commons.lang3.*;

import java.util.function.*;
import java.util.regex.*;

public class XMLPlaceHolderChecker {

    private static final String REGEX = "(%\\d\\$s)|(%s)|(%\\d\\$d)|(%d)|(%\\d\\$f)|(%f)";

    private static final Pattern pattern = Pattern.compile(REGEX);

    /**
     * 检查词条是否存在占位符表达式, 并统计个数
     */
    public static long countPlaceHolder(String input) {
        if (StringUtils.isEmpty(input)) {
            return 0;
        }
        Matcher matcher = pattern.matcher(input);
        //matcher.results().forEach(matchResult -> System.out.println(matchResult.group()));
        return matcher.results().count();
    }
}
