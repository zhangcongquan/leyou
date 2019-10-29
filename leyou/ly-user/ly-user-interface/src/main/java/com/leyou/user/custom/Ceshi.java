package com.leyou.user.custom;

import java.util.HashMap;
import java.util.Map;

public class Ceshi {
    public static void main(String[] args) {
        String str = trimStar("**ab*c***");
        System.out.println("str = " + str);
    }

    public static String trimStar(String srcString){
        String replace = srcString.replaceAll("\\*", " ");
        String trim = replace.trim();
        String replace1 = trim.replaceAll(" ", "\\*");
        return replace1;
    }
}
