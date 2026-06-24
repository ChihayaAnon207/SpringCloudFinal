package com.cloudblog.blog.feign;

import com.cloudblog.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserClientFallback implements UserClient {

    @Override
    public Result<UserDTO> getUserById(Long id) {
        log.warn("[Sentinel] user-service 熔断降级，userId: {}", id);
        UserDTO fallbackUser = new UserDTO();
        fallbackUser.setId(id);
        fallbackUser.setNickname("用户已注销");
        fallbackUser.setAvatarUrl("");
        return Result.success(fallbackUser);
    }
}
