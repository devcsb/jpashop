package jpabook.jpashop.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.*;
//         스프링 부트 신규 설정 (엔티티(필드) 테이블(컬럼))
//        1. 카멜 케이스 => 언더스코어(memberPoint member_point)
//        2. .(점) => _(언더스코어)
//        3. 대문자 => 소문자

@Entity
@Table(name = "orders") // 관례를 벗어나는 테이블명을 사용하므로 수동으로 명시해줌. // 명시 없을 시, order를 찾음.
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {
    //    모든 연관관계는 지연로딩으로 설정!
    //    즉시로딩( EAGER )은 예측이 어렵고, 어떤 SQL이 실행될지 추적하기 어렵다. 특히 JPQL을 실행할 때 N+1 문제가 자주 발생한다.
    //    JPQL : select o From order o; -> SQL : select * from order  ==> N+1문제 발생. order 가져오는 쿼리 + order에 엮인 member를 가져오기 위한 쿼리 n번
    //    연관된 엔티티를 함께 DB에서 조회해야 하면, fetch join또는 엔티티 그래프 기능을 사용한다.
    //    @XToOne(OneToOne, ManyToOne) 관계는 기본이 즉시로딩이므로 직접 지연로딩으로 설정해야 한다.

    /*Cascade 옵션 : Entity의 상태 변화를 전이시키는 옵션. 주로 ALL옵션을 사용. ALL : 상위 엔터티에서 하위 엔터티로 모든 작업을 전파*/
    /*Cascade의 범위에 대한 고민 => 참조하는 주인이 private Owner일 경우에만 사용하자.
     ex) Order는 OrderItem과 Delivery를 관리한다. OrderItem은 Order만 참조하고, Delivery도 Order만 참조한다. 다른 복잡한 관계가 얽혀있지 않으므로.
     복잡하계 여기 저기서 참조하는 엔티티의 경우, 각각 따로 persist 해야한다.*/
    // Cascade 옵션을 주면, 만약 orderItems에 A,B,C 컬렉션 3개를 담아놓고,
    // order를 저장[persist(order)]하면 Entity 상태가 전파되어, orderItems도 같이 persist된다.

    //CasCade 사용하지 않을 때 persist()코드 작성해야 할 순서:
    //    persist(orderItemA)
    //    persist(orderItemB)
    //    persist(orderItemC)
    //    persist(order)        //이렇게 따로따로 해줘야함.

    //Cascade 권한 줬을 때:
    //    persist(order)        //order만 persist상태로 바꾸면 orderItems 엔티티의 상태도 persist로 전파되어 A,B,C 모두 저장(persist상태로 변화)됨.

    @Id
    @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = LAZY) // member에 대한 * : 1 연관관계 설정  // 기본 패치전략 : EAGER
    @JoinColumn(name = "member_id") // 매핑할 컬럼을 설정. foreign key를 적어준다.
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) // @XToMany는 기본 패치전략이 Lazy로딩이므로 그대로 둬도 된다.
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = LAZY, cascade = CascadeType.ALL)  //OneToOne 관계에서는 FK를 어디에 두어도 상관없으나, 엑세스를 많이 하는 쪽에 FK를 두는 편이 좋다.
    @JoinColumn(name = "delivery_id") //FK가 Order테이블에 있으므로 연관관계의 주인임을 설정.
    private Delivery delivery;

    private LocalDateTime orderDate; // 원래 Date 타입을 쓰면 날짜관련 어노테이션 매핑을 해줘야하지만,
    // java 8 이상에서는 LocalDateTime을 쓰면 하이버네이트가 알아서 자동으로 지원해준다.

    @Enumerated(EnumType.STRING)
    private OrderStatus status; //주문상태 [ORDER, CANCEL]

    //==연관관계 편의 메서드==//  //한번에 양방향 관계를 설정(양방향 연관관계를 하나로 묶어주는) 메소드  //핵심적으로 컨트롤 하는 쪽에 메소드를 둔다.
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    /*    아래 코드를 메인 메서드에서 호출 하면서 적어줘야할 일을 연관관계 편입 메소드로 미리 원자적으로 묶어준다.*/
//    public static void main(String[] args) {
//        Member member = new Member();
//        Order order = new Order();
//
//        member.getOrders().add(order);
//        order.setMember(member);
//    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    /*모듈의 응집도를 높이기 위해서 바깥에서 호출하면서 해줘야할 비즈니스 로직을 엔티티 안에 한 덩어리로 묶어놓음*/
    //==생성 메서드==//
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) { //...으로 파라미터 여러개 넘김 (가변인자)
        Order order = new Order();  //오더 생성
        //연관관계 매핑
        order.setMember(member);
        order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER); //처음 상태를 ORDER로 강제해놓음.
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    //==비즈니스 로직==//
    /*
     * 주문 취소
     * */
    public void cancel() { //cancel()호출시 JPA가 dirty checking해서 Order.OrderStatus, OrderItem.stockQuantity에 대한 업데이트 쿼리가 날아간다.
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }

        this.setStatus(OrderStatus.CANCEL); //취소 처리
        for (OrderItem orderItem : orderItems) { //OrderItems의 재고를 원상 복구
            orderItem.cancel();
        }
    }

    //==조회 로직==//
    /*
     * 전체 주문 가격 조회
     * */
    public int getTotalPrice() {  //주문상품 엔티티의 가격을 모두 합한 가격 => 총 가격 구하기
        int totalPrice = 0;
        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;

        /* 위 코드를 자바8의 스트림으로 간단하게 표현하면 */
//        return orderItems.stream()
//        .mapToInt(OrderItem::getTotalPrice)
//        .sum();
    }


}
