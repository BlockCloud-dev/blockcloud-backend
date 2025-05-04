package com.blockcloud;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HelloController handles basic HTTP requests for demonstration purposes.
 * This controller returns a simple greeting message.
 * 
 * @author Yeongseon
 */
@RestController
public class HelloController {

    /**
     * GET endpoint that returns a greeting message.
     * 
     * @return A static greeting string.
     */
    @GetMapping("/")
    public String hello() {
        return "Hello, World!";
    }
}
