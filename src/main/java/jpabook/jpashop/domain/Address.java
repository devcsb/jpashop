package jpabook.jpashop.domain;


import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable // 임베디드 타입임을 선언. @Embeddable: 값 타입을 정의하는 곳에 표시

@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

}
