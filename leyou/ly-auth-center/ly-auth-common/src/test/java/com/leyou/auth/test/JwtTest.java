package com.leyou.auth.test;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {

    private static final String pubKeyPath = "D:/heima/rsa/rsa.pub";

    private static final String priKeyPath = "D:/heima/rsa/rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "heime62");
    }

    //在所有的测试执行之前，先加载公钥和私钥
    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(666L, "黑马62"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MzIsInVzZXJuYW1lIjoiaGVpbWE2MiIsImV4cCI6MTU1NTQwNTgwMH0.p_5U7oEsS2soIEibyPhcjeUWbu6781tAwMblD-kHs_H_wtZqldceYrEUNtphNeUpZED9gaPI28gPbJqf3i3DiF1s4phOw-T7bJG5aNAG2BNm0iuG0FXO40j451OkTCWPNg4HCIXwIXRpq6ez0LJ4AKWBwvF_a0ii6bp7H3kZ6po";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}
