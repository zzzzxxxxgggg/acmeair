/*******************************************************************************
* Copyright 2017 Huawei Technologies Co., Ltd
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.acmeair.hystrix;

import com.acmeair.service.UserService;
import com.acmeair.web.dto.CustomerInfo;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.NoSuchElementException;

abstract class UserCommand implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserCommand.class);
    private final RestTemplate restTemplate;

    @Value("${customer.service.name:customerServiceApp}")
    String customerServiceName;

    UserCommand(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
            }
        });
    }

    @HystrixCommand
    public CustomerInfo getCustomerInfo(String customerId) {
        String address = getCustomerServiceAddress();
        log.info("Sending GET request to remote customer at {} with customer id {}", address, customerId);
        ResponseEntity<CustomerInfo> resp = restTemplate.getForEntity(address + "/api/customer/{custid}", CustomerInfo.class, customerId);
        if (resp.getStatusCode() != HttpStatus.OK) {
            throw new NoSuchElementException("No such customer with id " + customerId);
        }
        log.info("Received response {} from remote customer at {} with customer id {}", resp.getBody(), address, customerId);
        return resp.getBody();
    }
    
    protected abstract String getCustomerServiceAddress();
}
