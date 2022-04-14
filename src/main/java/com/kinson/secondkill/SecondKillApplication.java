package com.kinson.secondkill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * @author
 * @Describe 应用启动类
 * @date
 */
// mapper扫描路径
@MapperScan({"com.kinson.secondkill.mapper"})
@SpringBootApplication
public class SecondKillApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecondKillApplication.class, args);
    }

}
