package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import static javax.persistence.FetchType.*;

@Entity
@Getter @Setter
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name= "order_id")
    private Order order;

    private int orderPrice; // 주문 가격
    private int count; //주문 수량

    //==생성 메서드==//
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
