package com.dechub.tanishq.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ReactResourceResolver implements ResourceResolver {
    private static final String REACT_DIR = "/static/";
    private static final String REACT_STATIC_DIR = "static";
    private static final String CHECKLIST_DIR = "checklist/";

    private static final String GLOBAL_DIR = "globalPage/";

    private static final String CHECKLIST_ASSESTS_DIR = "assets";
    private static final String GLOBAL_ASSESTS_DIR = "globalAssets";


    private Resource index = new ClassPathResource(REACT_DIR + "index.html");
    private Resource checklistIndex = new ClassPathResource(REACT_DIR + CHECKLIST_DIR + "index.html");
    private Resource createIndex = new ClassPathResource(REACT_DIR + CHECKLIST_DIR + "create.html");
    private Resource formIndex = new ClassPathResource(REACT_DIR + CHECKLIST_DIR + "form.html");
    private Resource verifyIndex = new ClassPathResource(REACT_DIR + CHECKLIST_DIR + "verify.html");
    private Resource checkIndex = new ClassPathResource(REACT_DIR + CHECKLIST_DIR + "checklist.html");
    private Resource loaderIndex = new ClassPathResource(REACT_DIR + CHECKLIST_DIR + "loader.html");
    private Resource thankyouIndex = new ClassPathResource(REACT_DIR + CHECKLIST_DIR + "thankyou.html");

    private Resource eventsIndex = new ClassPathResource(REACT_DIR +"events.html");
    private Resource globalPageIndex = new ClassPathResource(REACT_DIR +GLOBAL_DIR+"celebrate.html");

    // Add resources for assets (images, CSS, JS, etc.)
    private Resource checklistAssets = new ClassPathResource(REACT_DIR + CHECKLIST_DIR + "assets/");
    private Resource globalAssets = new ClassPathResource(REACT_DIR + GLOBAL_DIR + "globalAssets/");
    private Resource eventsAssets = new ClassPathResource(REACT_DIR+REACT_STATIC_DIR+"/lol/");
    private List<String> rootStaticFiles = Arrays.asList(
            "asset-manifest.json", "logo.png", "manifest.json", "robots.txt"
    );

    @Override
    public Resource resolveResource(
            HttpServletRequest request, String requestPath,
            List<? extends Resource> locations, ResourceResolverChain chain) {

        return resolve(requestPath, locations);
    }

    @Override
    public String resolveUrlPath(
            String resourcePath, List<? extends Resource> locations,
            ResourceResolverChain chain) {

        Resource resolvedResource = resolve(resourcePath, locations);
        if (resolvedResource == null) {
            return null;
        }
        try {
            return resolvedResource.getURL().toString();
        } catch (IOException e) {
            return resolvedResource.getFilename();
        }
    }



    private Resource resolve(
            String requestPath, List<? extends Resource> locations) {

        if (requestPath == null) return null;

        if (requestPath.startsWith("events")) {
            return eventsIndex;
        }else if (rootStaticFiles.contains(requestPath)
                || requestPath.startsWith(REACT_STATIC_DIR)) {
            return new ClassPathResource(REACT_DIR + requestPath);
        }else if (requestPath.startsWith(CHECKLIST_ASSESTS_DIR)) {
            return new ClassPathResource(REACT_DIR +CHECKLIST_DIR +requestPath);
        }else if (requestPath.startsWith(GLOBAL_ASSESTS_DIR)) {
            return new ClassPathResource(REACT_DIR +GLOBAL_DIR +requestPath);
        }
        else if (requestPath.startsWith("checklist")) {
            return checklistIndex;
        }else if (requestPath.startsWith("lol")) {
            // Handle requests for assets inside checklist folder
            return eventsAssets;
        }
        else if (requestPath.startsWith("create")) {
            return createIndex;
        } else if (requestPath.startsWith("form")) {
            return formIndex;
        } else if (requestPath.startsWith("loader")) {
            return loaderIndex;
        } else if (requestPath.startsWith("check")) {
            return checkIndex;
        } else if (requestPath.startsWith("thankyou")) {
            return thankyouIndex;
        }else if (requestPath.startsWith("verify")) {
            return verifyIndex;
        }
        else if (requestPath.startsWith("selfie")) {
            return index;
        }
        else if(requestPath.startsWith("globalAssets")){
            return globalAssets;
        }

        else if (requestPath.startsWith("assets")) {
            // Handle requests for assets inside checklist folder
            return checklistAssets;
        }
        else {
            return globalPageIndex;
        }
    }
}
