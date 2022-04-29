package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Delivery {
    @Id
    @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @JsonIgnore
    @OneToOne(mappedBy = "delivery") //OneToOne 관계에서는 FK를 어디에 두어도 상관없으나, 엑세스를 많이 하는 쪽에 FK를 두는 편이 좋다.
    private Order order;

    @Embedded
    private Address address;

    //EnumType은 반드시 STRING으로 해야한다.
    @Enumerated(EnumType.STRING)   //Enum타입은 Enumerated 어노테이션을 넣어야한다. // 기본값은 ORDINAL. 1,2,3... 숫자로 들어가므로 중간에 Enum값이 바뀌면 큰일난다.
    private DeliveryStatus status;  //READY, COMP
}
