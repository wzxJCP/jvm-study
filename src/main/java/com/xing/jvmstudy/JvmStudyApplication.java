package com.xing.jvmstudy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@SpringBootApplication
public class JvmStudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(JvmStudyApplication.class, Arrays.toString(args));
    }

}
