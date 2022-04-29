package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * xToOne
 * Order
 * Order -> member
 * Order -> delivery
 * */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    /**
     * V1. 엔티티 직접 노출
     * - Hibernate5Module 모듈 등록, LAZY=null 처리
     * - 양방향 관계 문제 발생 -> @JsonIgnore
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() { //연관된 관계가 얽혀있을 때, Entity를 직접 호출하면 무슨 일이 발생하는지 보여주기 위함.
        List<Order> all = orderRepository.findAllByString(new OrderSearch());  //모든 주문 들고오기
        for (Order order : all) {
            order.getMember().getName();  //Lazy 강제 초기화  //getMember()까지는 프록시 객체를 가져오지만, getName()하면 어쩃든 db에서 값을 가져와야 하므로.
            order.getDelivery().getAddress(); //Lazy 강제 초기화
        }
        return all;  /* 위의 Lazy 강제 초기화 없이 그냥 Entity 그대로 반환하면, Order 와 Member 사이에서 무한루프가 돈다. 이를 막으려면 양방향이 걸린 모든 속성에 @JsonIgnore를 걸어준다*/
    }


}
