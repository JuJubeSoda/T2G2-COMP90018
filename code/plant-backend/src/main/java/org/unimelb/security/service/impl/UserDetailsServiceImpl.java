package org.unimelb.security.service.impl;

import org.unimelb.security.vo.SecurityUser;
import org.unimelb.user.entity.User;
import org.unimelb.user.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Resource
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.getUserByName(username);

        if(user == null){
            throw  new UsernameNotFoundException("User not found: " + username);
        }
        return new SecurityUser(user);
    }
}