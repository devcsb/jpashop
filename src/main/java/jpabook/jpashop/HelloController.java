package jpabook.jpashop;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("hello")
    public String hello(Model model) { //org.springframework.ui.Model에 attribute를 담아 넘긴다.
        model.addAttribute("data", "hello!!!");
        return "hello"; //. html이 자동으로 붙어서 resource/template안의 파일명과 매칭된다.
    }
}
