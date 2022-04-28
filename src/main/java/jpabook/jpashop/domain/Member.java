package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class  Member {
    @Id
    @GeneratedValue
    @Column(name = "member_id") // 컬럼명 따로 지정. 엔티티의 식별자는 id 를 사용하고 PK 컬럼명은 member_id 사용하기 위함.
    private Long id;  // em.persist(member) => 영속성 컨텍스트에 member객체의 @Id 값이 key 로, member 엔티티가 value 형태로 저장됨. db에 들어간 시점이 아니어도 id필드에 값을 채워줌.

    /* @NotEmpty를 사용함으로써, 프레젠테이션 계층에 필요한 검증 로직이 지금 엔티티에 들어가있음.*/
//    @NotEmpty // javax.validation의 @Valid를 붙여서 사용할 때, @NotEmpty 가 붙은 필드는 필수값으로 검증해준다.
    private String name;

    @Embedded // JPA의 내장타입인 임베디드 타입임을 알려줌. / @Embedded: 값 타입을 사용하는 곳에 표시
    private Address address;

//    @JsonIgnore  //필드 속성을 무시하는 Jacson의 어노테이션
    @OneToMany(mappedBy = "member") //order 엔티티의  member 필드가 매핑된 객체라고 알려줌. 읽기 전용 거울 객체가 된 것.
    private List<Order> orders = new ArrayList<>();  //컬렉션은 필드에서 바로 초기화 하는 것이 null 문제나 하이버네이트 매커니즘 문제에서 안전하며 간결하다.

//    public Member() {  //이렇게 하는 것 보다 필드에서 바로 초기화할 것.
//        orders = new ArrayList<>();
//    }
}
