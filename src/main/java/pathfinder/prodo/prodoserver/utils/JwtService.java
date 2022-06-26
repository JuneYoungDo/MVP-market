package pathfinder.prodo.prodoserver.utils;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pathfinder.prodo.prodoserver.config.BaseException;
import pathfinder.prodo.prodoserver.config.BaseResponseStatus;
import pathfinder.prodo.prodoserver.user.UserRepository;
import pathfinder.prodo.prodoserver.user.UserService;
import pathfinder.prodo.prodoserver.user.VO.User;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserRepository userRepository;

    public boolean isValidUser(Long userId) {
        if (userRepository.existsById(userId) == false) return false;
        User user = userRepository.getById(userId);
        if (user.isDeleted()) return false;
        return true;
    }

    public String createJwt(Long userId) {
        Date now = new Date();
        Long expiredTime = 1000 * 60L * 60L * 24L;  // 유효기간 24시간

        Date nowExpiredTime = new Date();
        nowExpiredTime.setTime(nowExpiredTime.getTime() + expiredTime);

        return Jwts.builder()
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(nowExpiredTime)
                .signWith(SignatureAlgorithm.HS256, Secret.JWT_SECRET_KEY)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        Date now = new Date();
        Long expiredTime = 1000 * 60L * 60L * 24L * 60L;  // 유효기간 60일

        Date nowExpiredTime = new Date();
        nowExpiredTime.setTime(nowExpiredTime.getTime() + expiredTime);

        return Jwts.builder()
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(nowExpiredTime)
                .signWith(SignatureAlgorithm.HS256, Secret.REFRESH_SECRET_KEY)
                .compact();
    }

    // Header 에서 Authorization 으로 jwt 추출
    public String getJwt() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes())
                .getRequest();
        return request.getHeader("Authorization");
    }

    // Header 에서 Authorization 으로 refreshToken 추출
    public String getRefreshJwt() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes())
                .getRequest();
        return request.getHeader("refreshToken");
    }

    // Jwt 검증
    public Boolean verifyJwt(String jwt) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(Secret.JWT_SECRET_KEY)
                    .parseClaimsJws(jwt)
                    .getBody();
            Long userId = claims.get("userId", Long.class);
            if (!isValidUser(userId)) return false;
        } catch (ExpiredJwtException e) {       // 토큰 만료
            System.out.println(e);
            return false;
        } catch (Exception e) {         // 그 외 에러
            System.out.println(e);
            return false;
        }
        return true;
    }

    // refresh Token 검증
    public Boolean verifyRefreshToken(String jwt) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(Secret.REFRESH_SECRET_KEY)
                    .parseClaimsJws(jwt)
                    .getBody();
            Long userId = claims.get("userId", Long.class);
            if (!isValidUser(userId)) return false;
        } catch (ExpiredJwtException e) {       // 토큰 만료
            System.out.println(e);
            System.out.println("here");
            return false;
        } catch (Exception e) {         // 그 외 에러
            System.out.println(e);
            return false;
        }
        return true;
    }

    public Long getUserId() throws BaseException {
        String accessToken = getJwt();
        if (accessToken.equals("") || accessToken.length() == 0)
            throw new BaseException(BaseResponseStatus.EMPTY_JWT);
        if (!verifyJwt(accessToken))
            throw new BaseException(BaseResponseStatus.INVALID_JWT);
        Jws<Claims> claims;

        claims = Jwts.parser()
                .setSigningKey(Secret.JWT_SECRET_KEY)
                .parseClaimsJws(accessToken);
        return claims.getBody().get("userId", Long.class);
    }

    public Long getUserIdFromRefreshToken() throws BaseException {
        String accessToken = getRefreshJwt();
        if (accessToken.equals("") || accessToken.length() == 0)
            throw new BaseException(BaseResponseStatus.EMPTY_JWT);
        if (!verifyJwt(accessToken))
            throw new BaseException(BaseResponseStatus.INVALID_JWT);
        Jws<Claims> claims;

        claims = Jwts.parser()
                .setSigningKey(Secret.REFRESH_SECRET_KEY)
                .parseClaimsJws(accessToken);
        return claims.getBody().get("userId", Long.class);
    }
}
