package com.cloudblog.comment.feign;

import com.cloudblog.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/user/{id}")
    Result<UserDTO> getUserById(@PathVariable("id") Long id);
}
