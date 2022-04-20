package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders") // 관례를 벗어나는 테이블명을 사용하므로 수동으로 명시해줌. // 명시 없을 시, order를 찾음.
@Getter @Setter
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne // member에 대한 * : 1 연관관계 설정
    @JoinColumn(name = "member_id") // 매핑할 컬럼을 설정. foreign key를 적어준다.
    private Member member;

    @OneToMany(mappedBy = "order") //order
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne  //OneToOne 관계에서는 FK를 어디에 두어도 상관없으나, 엑세스를 많이 하는 쪽에 FK를 두는 편이 좋다.
    @JoinColumn(name = "delivery_id") //FK가 Order테이블에 있으므로 연관관계의 주인임을 설정.
    private Delivery delivery;

    private LocalDateTime orderDate; // 원래 Date 타입을 쓰면 날짜관련 어노테이션 매핑을 해줘야하지만,
    // java 8 이상에서는 LocalDateTime을 쓰면 하이버네이트가 알아서 자동으로 지원해준다.

    private OrderStatus status; //주문상태 [ORDER, CANCEL]
}
