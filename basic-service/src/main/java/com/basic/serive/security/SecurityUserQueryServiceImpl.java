package com.basic.serive.security;

import com.basic.core.security.model.LoginUser;
import com.basic.core.security.spi.SecurityUserQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SecurityUserQueryServiceImpl implements SecurityUserQueryService {

//    @Autowired
//    private SysUserMapper userMapper;

    @Override
    public LoginUser loadByUsername(String username) {
//        SysUserEntity user = userMapper.selectByUsername(username);
//        if (user == null || user.getDeleted() == 1) return null;
//        if (user.getStatus() == 0) throw new RuntimeException("用户已禁用");
//
//        LoginUser u = new LoginUser();
//        u.setUserId(user.getId());
//        u.setUsername(user.getUsername());
//        u.setPassword(user.getPassword());
//        u.setEnabled(true);
//        return u;

        //TODO
        return null;
    }
}
