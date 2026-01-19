package com.example.demo.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestUtils {

    private final Environment env;

    @Autowired
    public RestUtils(Environment env) {
        this.env = env;
    }

    //
//    @Value("${movie.api.key}")
//    private String apiKey;
//    @Value("${movie.api.base.url}")
//    private String baseUrl;
//    //    @Value("${movie.genres}")
////    private  String[] genres ;
//    @Value("${movie.genres}")
//    private List<String> genres;
//    @Value("${movie.ports}")
//    private int[] ports;
//    @Value("${movie.page.size:100}")
//    private int pageSize;
//    @Value("${movie.is.sort:false}")
//    private Boolean isSort;

    @Autowired
    @Qualifier("restTemplateLong")
    private RestTemplate restTemplate;


    private void test() {
//        boolean isDevProfileActive = env.acceptsProfiles("dev");
//        System.out.println("isDevProfileActive: " + isDevProfileActive);
//        String[] defaultProfiles = env.getDefaultProfiles();
//
//        for(String profile : defaultProfiles){
//            System.out.println(profile);
//        }

//        String[] activeProfiles = env.getActiveProfiles();
//
//        for(String profile : activeProfiles){
//            System.out.println(profile);
//        }
//        String apiKey = env.getProperty("movie.api.key");
//        String apiKey = env.getProperty("movie.api.key",String.class,"default");
//        if (env.containsProperty("movie.api.key")) {
//             apiKey = env.getProperty("movie.api.key");
//        }else{
//            apiKey = "default";
//        }
//        System.out.println("apiKey: " + apiKey);

//        String[] genres = new String[]{env.getProperty("movie.genres")};
//        System.out.println(isSort);
//        System.out.println(pageSize);
//        for (String genre : genres) {
//            System.out.println(genre);
//        }
//
//        for (Integer port : ports) {
//            System.out.println(port);
//        }
    }

}
