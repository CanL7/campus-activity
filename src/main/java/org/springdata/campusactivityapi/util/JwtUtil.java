package org.springdata.campusactivityapi.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springdata.campusactivityapi.common.LoginUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil{
    private static final String CLAIM_ROLE = "role";

    private final SecretKey key;//SecretKey 类型是官方接口要求
    private final long expireMillis;//过期时间 毫秒

    //构造器 把值传进来 注意 这里不是Lombok的Value注解 而是Spring家的
    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expire-minutes}") long expireMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8 ));// → SecretKey；不足 32 字节抛 WeakKeyException
        this.expireMillis = expireMinutes * 60 * 1000;

    }

    //genereate token
    public String generate(long userId, Integer role){

        Date now = new Date();

        return Jwts.builder()
                .subject(String.valueOf(userId))//sub 放userId
                .claim(CLAIM_ROLE,role)//自定义 用claim
                .issuedAt(now)//jwt发行日期
                .expiration(new Date(now.getTime() + expireMillis))//过期时间
                .signWith(key)//用key签名 signiture
                .compact();//整合
    }

    //parse token 拿身份
    public LoginUser parse(String token){
        Claims payload = Jwts.parser()
                .verifyWith(key)//配置项：用这个密钥
                .build()//把前面的配置组装成一个 JwtParser（解析器）
                .parseSignedClaims(token)//1.拆成三段 2.读取alg 3.算签名比对 4.
                .getPayload();

        String sub = payload.getSubject();
        Integer role = payload.get(CLAIM_ROLE, Integer.class);
        return new LoginUser(Long.valueOf(sub),role);
    }




}