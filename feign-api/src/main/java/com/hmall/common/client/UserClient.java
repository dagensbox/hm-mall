package com.hmall.common.client;

import com.hmall.common.pojo.Address;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;


/**
 * @author 12141
 */
@FeignClient("userservice")
public interface UserClient {

    @GetMapping("/address/{id}")
    Address findAddressById(@PathVariable("id") Long id);
}
