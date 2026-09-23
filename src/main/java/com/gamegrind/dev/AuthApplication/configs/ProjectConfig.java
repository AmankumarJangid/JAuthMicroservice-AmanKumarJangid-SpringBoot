package com.gamegrind.dev.AuthApplication.configs;

import com.gamegrind.dev.AuthApplication.services.UserService;
import com.gamegrind.dev.AuthApplication.services.impl.UserServiceImpl;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectConfig {

    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }
}
