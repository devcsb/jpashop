package jpabook.jpashop;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter
@Entity //JPA가 관리할 객체
@Setter // Lombok이 대신 생성
public class Member {

    @Id @GeneratedValue //@Id = 데이터베이스 PK와 매핑
    private Long id;
    private String username;
}
