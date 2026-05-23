package com.basic.sericve.auth.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.basic.api.dto.auth.ForgotPasswordResetDTO;
import com.basic.api.dto.auth.RegisterDTO;
import com.basic.api.vo.auth.OnlineUserVO;
import com.basic.api.vo.auth.TokenVO;
import com.basic.api.vo.sysUser.UserVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.ResultEnum;
import com.basic.core.security.model.LoginSession;
import com.basic.core.security.service.AuthTokenService;
import com.basic.core.security.util.JwtUtil;
import com.basic.dao.sysUser.entity.SysUser;
import com.basic.sericve.auth.service.IAuthService;
import com.basic.sericve.auth.util.LoginRequestUtils;
import com.basic.sericve.sysUser.service.ISysUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final ISysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    @Override
    public Long register(RegisterDTO registerDTO) {
        if (sysUserService.getUserByUsername(registerDTO.getUsername()) != null) {
            throw new BusinessException(ResultEnum.USER_ALREADY_EXIST);
        }

        if (StringUtils.hasText(registerDTO.getPhone()) && isPhoneBound(registerDTO.getPhone())) {
            throw new BusinessException(ResultEnum.PHONE_ALREADY_BIND);
        }

        if (StringUtils.hasText(registerDTO.getEmail()) && isEmailBound(registerDTO.getEmail())) {
            throw new BusinessException(ResultEnum.EMAIL_ALREADY_BIND);
        }

        SysUser user = new SysUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setAvatar(registerDTO.getAvatar());
        user.setStatus((byte) 1);
        sysUserService.save(user);
        return user.getId();
    }

    @Override
    public void resetForgottenPassword(ForgotPasswordResetDTO resetDTO) {
        SysUser user = sysUserService.getUserByUsername(resetDTO.getUsername());
        if (user == null) {
            throw new BusinessException(ResultEnum.USER_NOT_EXIST);
        }

        if (!isMatchedContact(user, resetDTO.getContact())) {
            throw new BusinessException(ResultEnum.PARAM_ILLEGAL);
        }

        user.setPassword(passwordEncoder.encode(resetDTO.getNewPassword()));
        sysUserService.updateById(user);
        authTokenService.forceLogout(user.getId());
    }

    @Override
    public TokenVO login(String username, String password, HttpServletRequest request) {
        SysUser user = sysUserService.getUserByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultEnum.USER_NOT_EXIST);
        }

        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException(ResultEnum.ACCOUNT_DISABLED);
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ResultEnum.PASSWORD_ERROR);
        }

        UserVO userVO = sysUserService.getUserById(user.getId());
        List<String> deptNames = userVO == null || userVO.getDeptNames() == null
                ? List.of()
                : userVO.getDeptNames();
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), deptNames);
        authTokenService.saveLoginSession(buildLoginSession(user, token, request));

        TokenVO tokenVO = new TokenVO();
        tokenVO.setToken(token);
        return tokenVO;
    }

    @Override
    public void logout(String token) {
        authTokenService.deleteLoginToken(token);
    }

    @Override
    public List<OnlineUserVO> getOnlineUsers() {
        return authTokenService.listOnlineSessions()
                .stream()
                .map(this::toOnlineUserVO)
                .toList();
    }

    @Override
    public void forceLogout(Long userId) {
        authTokenService.forceLogout(userId);
    }

    private LoginSession buildLoginSession(SysUser user, String token, HttpServletRequest request) {
        LoginSession session = new LoginSession();
        session.setToken(token);
        session.setUserId(user.getId());
        session.setUsername(user.getUsername());
        session.setNickname(user.getNickname());
        session.setAvatar(user.getAvatar());
        session.setLoginTime(LocalDateTime.now());
        session.setLoginIp(LoginRequestUtils.resolveMaskedIp(request));
        session.setBrowser(LoginRequestUtils.resolveBrowser(request));
        session.setOs(LoginRequestUtils.resolveOs(request));
        return session;
    }

    private OnlineUserVO toOnlineUserVO(LoginSession session) {
        OnlineUserVO onlineUserVO = new OnlineUserVO();
        onlineUserVO.setUserId(session.getUserId());
        onlineUserVO.setUsername(session.getUsername());
        onlineUserVO.setNickname(session.getNickname());
        onlineUserVO.setAvatar(session.getAvatar());
        onlineUserVO.setLoginTime(session.getLoginTime());
        onlineUserVO.setLoginIp(session.getLoginIp());
        onlineUserVO.setBrowser(session.getBrowser());
        onlineUserVO.setOs(session.getOs());
        return onlineUserVO;
    }

    private boolean isPhoneBound(String phone) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getPhone, phone);
        return sysUserService.count(wrapper) > 0;
    }

    private boolean isEmailBound(String email) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getEmail, email);
        return sysUserService.count(wrapper) > 0;
    }

    private boolean isMatchedContact(SysUser user, String contact) {
        return StringUtils.hasText(contact)
                && (contact.equals(user.getPhone()) || contact.equals(user.getEmail()));
    }

}
