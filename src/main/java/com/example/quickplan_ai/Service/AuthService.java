package com.example.quickplan_ai.Service;

import com.example.quickplan_ai.dto.*;
import com.example.quickplan_ai.entity.User;
import com.example.quickplan_ai.entity.UserToken;
import com.example.quickplan_ai.entity.VerificationCode;
import com.example.quickplan_ai.mapper.UserMapper;
import com.example.quickplan_ai.mapper.UserTokenMapper;
import com.example.quickplan_ai.mapper.VerificationCodeMapper;
import com.example.quickplan_ai.util.JwtUtil;
import com.example.quickplan_ai.util.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

/**
 * 认证服务
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserTokenMapper userTokenMapper;

    @Autowired
    private VerificationCodeMapper verificationCodeMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordUtil passwordUtil;

    @Value("${verification.code.length:6}")
    private int codeLength;

    @Value("${verification.code.expiration:300}")
    private int codeExpiration;

    @Value("${verification.code.daily-limit:10}")
    private int dailyLimit;

    /**
     * 发送验证码
     */
    public void sendVerificationCode(String phone, String type, String ipAddress) {
        // 检查今天发送次数
        int todayCount = verificationCodeMapper.countTodayByPhone(phone);
        if (todayCount >= dailyLimit) {
            throw new RuntimeException("今天发送验证码次数已达上限");
        }

        // 生成验证码
        String code = generateCode();

        // 保存到数据库
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setPhone(phone);
        verificationCode.setCode(code);
        verificationCode.setType(type);
        verificationCode.setStatus(0);
        verificationCode.setIpAddress(ipAddress);
        verificationCode.setExpiresAt(LocalDateTime.now().plusSeconds(codeExpiration));
        verificationCode.setCreatedAt(LocalDateTime.now());

        verificationCodeMapper.insert(verificationCode);

        // TODO: 集成真实短信服务（阿里云短信/腾讯云短信等）
        // 当前为开发环境，验证码直接在日志中打印
        // 生产环境需要替换为真实的短信发送逻辑
        logger.info("发送验证码到手机号: {}, 验证码: {} (开发环境)", phone, code);
    }

    /**
     * 手机号注册
     */
    @Transactional
    public LoginResponse registerByPhone(PhoneRegisterRequest request, HttpServletRequest httpRequest) {
        // 验证验证码 (兼容register和login类型)
        VerificationCode verificationCode = verificationCodeMapper.findValidByPhone(request.getPhone(), "register");
        if (verificationCode == null) {
            verificationCode = verificationCodeMapper.findValidByPhone(request.getPhone(), "login");
        }

        if (verificationCode == null || !verificationCode.getCode().equals(request.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 检查手机号是否已注册
        if (userMapper.existsByPhone(request.getPhone()) > 0) {
            throw new RuntimeException("手机号已注册");
        }

        // 创建用户
        User user = new User();
        user.setUserId(generateUserId());
        user.setPhone(request.getPhone());

        // 优先使用前端传来的昵称
        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            user.setNickname(request.getNickname());
        } else {
            user.setNickname("用户" + request.getPhone().substring(7));
        }

        user.setAvatar(generateDefaultAvatar(user.getUserId()));
        user.setStatus(1);
        user.setLoginType("phone");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 保存密码
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordUtil.encode(request.getPassword()));
            logger.info("用户注册设置密码: phone={}, password已加密", request.getPhone());
        }

        userMapper.insert(user);
        logger.info("用户注册成功: userId={}, phone={}, nickname={}", user.getUserId(), user.getPhone(), user.getNickname());

        // 标记验证码已使用
        verificationCodeMapper.markAsUsed(verificationCode.getId());

        // 生成Token并返回
        return generateLoginResponse(user, httpRequest);
    }

    /**
     * 邮箱注册
     */
    @Transactional
    public LoginResponse registerByEmail(EmailRegisterRequest request, HttpServletRequest httpRequest) {
        // 检查邮箱是否已注册
        if (userMapper.existsByEmail(request.getEmail()) > 0) {
            throw new RuntimeException("邮箱已注册");
        }

        // 创建用户
        User user = new User();
        user.setUserId(generateUserId());
        user.setEmail(request.getEmail());
        user.setPassword(passwordUtil.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getEmail().split("@")[0]);
        user.setAvatar(generateDefaultAvatar(user.getUserId()));
        user.setStatus(1);
        user.setLoginType("email");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);

        // 生成Token并返回
        return generateLoginResponse(user, httpRequest);
    }

    /**
     * 手机号登录
     */
    @Transactional
    public LoginResponse loginByPhone(PhoneLoginRequest request, HttpServletRequest httpRequest) {
        // 验证验证码
        VerificationCode verificationCode = verificationCodeMapper.findValidByPhone(request.getPhone(), "login");
        if (verificationCode == null || !verificationCode.getCode().equals(request.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 查找用户
        User user = userMapper.findByPhone(request.getPhone());

        // 如果用户不存在,自动注册
        if (user == null) {
            user = new User();
            user.setUserId(generateUserId());
            user.setPhone(request.getPhone());
            user.setNickname("用户" + request.getPhone().substring(7));
            user.setAvatar(generateDefaultAvatar(user.getUserId()));
            user.setStatus(1);
            user.setLoginType("phone");
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            userMapper.insert(user);
        }

        // 检查账号状态
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        // 标记验证码已使用
        verificationCodeMapper.markAsUsed(verificationCode.getId());

        // 更新最后登录信息
        String ipAddress = httpRequest.getRemoteAddr();
        userMapper.updateLastLogin(user.getUserId(), LocalDateTime.now(), ipAddress);

        // 生成Token并返回
        return generateLoginResponse(user, httpRequest);
    }

    /**
     * 邮箱登录
     */
    @Transactional
    public LoginResponse loginByEmail(EmailLoginRequest request, HttpServletRequest httpRequest) {
        // 查找用户
        User user = userMapper.findByEmail(request.getEmail());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证密码
        if (user.getPassword() == null || !passwordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("邮箱或密码错误");
        }

        // 检查账号状态
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        // 更新最后登录信息
        String ipAddress = httpRequest.getRemoteAddr();
        userMapper.updateLastLogin(user.getUserId(), LocalDateTime.now(), ipAddress);

        // 生成Token并返回
        return generateLoginResponse(user, httpRequest);
    }

    /**
     * 刷新Token
     */
    @Transactional
    public LoginResponse refreshToken(String refreshToken, HttpServletRequest httpRequest) {
        // 验证RefreshToken
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("RefreshToken无效或已过期");
        }

        // 从数据库查询Token
        UserToken userToken = userTokenMapper.findByRefreshToken(refreshToken);
        if (userToken == null || userToken.getStatus() != 1) {
            throw new RuntimeException("RefreshToken无效");
        }

        // 检查是否过期
        if (userToken.getRefreshTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("RefreshToken已过期");
        }

        // 查询用户
        User user = userMapper.findByUserId(userToken.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 使旧Token失效
        userTokenMapper.invalidateByUserId(user.getUserId());

        // 生成新Token
        return generateLoginResponse(user, httpRequest);
    }

    /**
     * 登出
     */
    public void logout(String accessToken) {
        userTokenMapper.invalidateByAccessToken(accessToken);
    }

    /**
     * 生成登录响应
     */
    private LoginResponse generateLoginResponse(User user, HttpServletRequest request) {
        // 生成Token
        String accessToken = jwtUtil.generateAccessToken(user.getUserId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

        // 保存Token到数据库
        UserToken userToken = new UserToken();
        userToken.setUserId(user.getUserId());
        userToken.setAccessToken(accessToken);
        userToken.setRefreshToken(refreshToken);
        userToken.setAccessTokenExpiresAt(LocalDateTime.now().plusSeconds(jwtUtil.getAccessTokenExpiration()));
        userToken.setRefreshTokenExpiresAt(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenExpiration()));
        userToken.setIpAddress(request.getRemoteAddr());
        userToken.setUserAgent(request.getHeader("User-Agent"));
        userToken.setStatus(1);
        userToken.setCreatedAt(LocalDateTime.now());
        userToken.setUpdatedAt(LocalDateTime.now());

        userTokenMapper.insert(userToken);

        // 构造响应
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getUserId());
        response.setToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(jwtUtil.getAccessTokenExpiration());

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setUserId(user.getUserId());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setCreatedAt(user.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        userInfo.setLoginType(user.getLoginType());

        response.setUserInfo(userInfo);

        return response;
    }

    /**
     * 生成用户ID
     */
    private String generateUserId() {
        return "user_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 生成默认头像
     */
    private String generateDefaultAvatar(String userId) {
        return "https://api.dicebear.com/7.x/avataaars/svg?seed=" + userId;
    }
}
