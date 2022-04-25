package jpabook.jpashop.domain;


import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable // 임베디드 타입임을 선언. @Embeddable: 값 타입을 정의하는 곳에 표시
@Getter  // @Setter 를 제거하고, 생성자에서 값을 모두 초기화해서 변경 불가능한 클래스를 만들었다.
public class Address {

    private String city;
    private String street;
    private String zipcode;

    protected Address() {
        //JPA 스펙상 엔티티나 임베디드 타입( @Embeddable )은 자바 기본 생성자(default constructor)를
        // public 또는 protected 로 설정해야 한다. 더 안전한 protected로 생성하도록 하자.
        // 이런 제약을 두는 것은 JPA 구현 라이브러리가 객체를 생성할 때 리플랙션 같은 기술을 사용할 수 있도록 지원해야 하기 때문이다.
    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }

}
