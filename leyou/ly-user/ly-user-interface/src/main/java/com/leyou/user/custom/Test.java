package com.leyou.user.custom;

public class Test {
    public static void main(String[] args) {
        int n = 2;
        int a[] = new int[n];
        for (int k = 0; k < n; k++) {
            a[k] = 0;
        }
        int i = 0,call_n = 0,out_n = 0;
        while (true){
            if (a[i] == 0){
                if (out_n == (n-1))
                    break;
                call_n++;
                call_n %= 3;
                if (call_n == 0){
                    a[i] = 1;
                    out_n++;
                }
            }
            i++;
            i %= n;
        }
        System.out.println(i + 1);
    }
}
