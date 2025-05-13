//package com.dechub.tanishq.controller;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//
//import java.util.List;
//
//@Controller
//public class PageController {
//
//    @GetMapping("/{pageName}")
//    public String servePage(@PathVariable String pageName) {
//        // List of valid pages you want to serve this way
//        List<String> validPages = List.of("index", "events", "home");
//
//        if (validPages.contains(pageName)) {
//            return pageName; // will serve about.html, contact.html, etc.
//        }
//
//        // Fall back to 404 or another error page
//        return "error/404"; // assumes you have error/404.html
//    }
//}
//
