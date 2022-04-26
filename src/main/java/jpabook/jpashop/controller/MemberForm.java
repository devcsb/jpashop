package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class MemberForm {

    // implementation 'org.springframework.boot:spring-boot-starter-validation' 최신 스프링 부트에서는 직접 추가해야 함.
    @NotEmpty(message = "회원 이름은 필수입니다.") // javax.validation의 기능
    private  String name;

    private  String city;
    private String street;
    private String zipcode;
}
