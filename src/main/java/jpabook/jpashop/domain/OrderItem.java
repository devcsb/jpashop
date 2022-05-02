package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jpabook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;

import static javax.persistence.FetchType.*;

//@BatchSize(size = 100)  // 컬렉션이 아닌 ToOne 관계일 때는 엔티티 클래스에 적용
@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Lombok의 기능. 기본 생성자의 접근제한자를 protected로 설정한다.
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @JsonIgnore
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name= "order_id")
    private Order order;

    private int orderPrice; // 주문 가격
    private int count; //주문 수량

    /*정적 팩토리 메서드 방식으로만 생성 가능하게 강제하기 위하여 생성자의 접근제한자를 protected로 바꾼다.*/
    //JPA는 기본생성자를 protected 까지 설정할 수 있게 허용한다.
//    protected OrderItem() {
//    }
    /*기본 생성자를 Lombok의 @NoArgsConstructor(access = AccessLevel.PROTECTED) 로 대체할 수 있다.*/

    //==정적 팩토리 메서드==//
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        //할인 정책이 생길 수 있기 때문에 item의 price를 orderPrice로 그대로 적용하지 않고 따로 가져간다.
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        item.removeStock(count); //item의 재고에서 count만큼 빼줌
        return orderItem;
    }

    //==비즈니스 로직==//
    /*주문 취소(재고 수량 원상복구)*/
    public void cancel() {
        getItem().addStock(count); //재고를 가져와서 주문수량만큼 늘려준다. 원상복구
    }

    //==조회 로직==//
    /*주문상품의 총 가격 구하기*/
    public int getTotalPrice() {
        return getOrderPrice() * getCount();
    }
}
