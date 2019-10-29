package com.leyou.user.custom;

import java.io.UnsupportedEncodingException;
import java.util.Random;

public class Example {

    /**
     *
     * <pre>
     * 以字符字节的形式操作字节截取。
     * </pre>
     *
     * @param src
     * @param length
     * @param charset
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String subStringInChars(String src, int length, String charset) throws UnsupportedEncodingException {
        int srcLength = src.length();

        int i = 0;
        int len = 0;
        int rollback = 0;
        while (true) {
            if (len >= length || length>=srcLength) {
                break;
            }

            if (src.charAt(i) < 256) {
                rollback=0;
                len += 1;
            } else {
                rollback=1;
                len += 2;
            }
            i+=1;
        }

        if (len!=length){
            i-=rollback;
        }

        return src.substring(0, i);
    }

    /**
     *
     * <pre>
     * 逐字节截取字符。
     * </pre>
     *
     * @param src
     * @param length
     * @param charset
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String subStringInBytes(String src, int length, String charset) throws UnsupportedEncodingException {
        if (charset == null) {
            charset = "GBK";
        }

        int firstStartScope = 129 - 1;
        int firstEndScope = 254 + 1;

        int secondStartScope = 64 - 1;
        int secondEndScope = 254 + 1;

        byte[] bytes = src.getBytes(charset);
        int i = 0;
        while (i < bytes.length) {
            int b1 = bytes[i] & 0xFF;
            int b2 = bytes[i+1] & 0xFF;

            if (b1 > firstStartScope && b1 < firstEndScope && b2 > secondStartScope && b2 < secondEndScope) {

                if (i+1==length){
                    i=length-1;
                    break;
                }else if (i+1>length){
                    i=length;
                    break;
                }

                i+=2;
            }else{
                i+=1;

                if (i==length){
                    break;
                }
            }
        }

        return new String(bytes, 0, i, charset);
    }

    /**
     *
     * <pre>
     * 面试官认可的操作方法
     * </pre>
     *
     * @param str
     * @param subSLength
     * @param charset
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String subStringInStrings(String str, int subSLength, String charset) throws UnsupportedEncodingException {
        if (charset == null) {
            charset = "GBK";
        }

        if (str == null) {
            return "";
        } else {
            int tempSubLength = subSLength;//截取字节数
            int strLength = str.length();
            String subStr = str.substring(0, strLength < subSLength ? strLength : subSLength);//截取的子串
            int subStrByetsL = subStr.getBytes(charset).length;//截取子串的字节长度
            while (subStrByetsL > tempSubLength) {
                int subSLengthTemp = --subSLength;
                subStr = str.substring(0, subSLengthTemp > strLength ? strLength : subSLengthTemp);
                subStrByetsL = subStr.getBytes(charset).length;
                //subStrByetsL = subStr.getBytes().length;
            }
            return subStr;
        }
    }

    private static Random random = new Random();

    public static String getRandomString(int randomLength) {
//        String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int strLength = random.nextInt(randomLength);
        while (strLength < randomLength / 2) {
            strLength = random.nextInt(randomLength);
        }
        char[] chars = new char[strLength];
        for (int i = 0; i < chars.length; i++) {
            if (random.nextInt(3) > 0) {
                chars[i] = (char) random.nextInt(Byte.MAX_VALUE);//base.charAt(random.nextInt(base.length()));
            }else{
                chars[i] =  (char) (0x4e00 + (int) (Math.random() * (0x9fa5 - 0x4e00 + 1)));
            }
        }

        return new String(chars);
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        long start = System.currentTimeMillis();
        long s1 = 0;
        long s2 = 0;
        long s3 = 0;
        Random random = new Random();
        int randomLength = 10000;
        for (int i = 0; i < 100; i++) {
            String str = getRandomString(randomLength);
//            String str = "我ABC汗";
            //            System.out.println(str);
            if (str.length() == 0) {
                continue;
            }

            int randomLeng = random.nextInt(str.length());
//            int randomLeng = 6;
//            subStringInBytes(str, randomLeng, null);

            start = System.currentTimeMillis();
            String str1 = subStringInBytes(str, randomLeng, null);
            //            System.out.println(str1);
            s1 += (System.currentTimeMillis() - start);

            start = System.currentTimeMillis();
            String str2 = subStringInStrings(str, randomLeng, null);
            //            System.out.println(str2);
            s2 += (System.currentTimeMillis() - start);

            start = System.currentTimeMillis();
            String str3 = subStringInChars(str, randomLeng, null);
            //            System.out.println(str3);
            s3 += (System.currentTimeMillis() - start);

            if (!str1.equals(str2) || !str2.equals(str3) || !str1.equals(str3)) {
                System.out.println(i);
                System.out.println(str);
                System.out.println(randomLeng);
                System.out.println(str1);
                System.out.println(str2);
                System.out.println(str3);
                break;
            }
        }

        System.out.println("字节截取:"+s1);
        System.out.println("字符串截取:"+s2);
        System.out.println("字符截取:"+s3);

    }

}
