package jpabook.jpashop.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j  // Lombok으로 아래 log 생성 코드를 대체.
public class HomeController {

//    Logger log = LoggerFactory.getLogger(getClass());  //org.slf4j.Logger 사용하여 이런 식으로 로거를 뽑을 수 있음. => Lombok 어노테이션으로 대체 가능.

    @RequestMapping("/")
    public String home() {
        log.info("home controller");  //로그 찍기
        return "home";
    }
}
