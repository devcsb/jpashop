package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    /**
     * V1. 엔티티 직접 노출
     * - Hibernate5Module 모듈 등록, LAZY=null 처리
     * - 양방향 관계 문제 발생 -> @JsonIgnore
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();

            List<OrderItem> orderItems = order.getOrderItems(); //OrderItem 초기화

            orderItems.stream().forEach(o -> o.getItem().getName()); //OrderItem 내부에 item이 있으므로 그것의 이름을 얻어와야하므로 프록시 초기화.
        }
        return all;
    }

    /*
    *  V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
    * - 트랜잭션 안에서 지연 로딩 필요
    * */
    //엔티티를 외부에 노출시키지 말라는 뜻은, 단순히 Dto로 감싸는 걸로 끝내라는 말이 아니라, 의존관계에 엮인 모든 Entity를 Dto로 전부 바꿔서 써야한다는 뜻이다.
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch()); //Entity에서 orders 조회 (Entity 타입)
        List<OrderDto> result = orders.stream()  //Entity에서 Dto로 변환
                .map(OrderDto::new)
                .collect(Collectors.toList());

        return result;
    }

    //    No serializer found for class jpabook.jpashop.api.OrderApiController$OrderDto and no properties discovered to create BeanSerializer
    //    오류 중 no properties는 대부분 getter setter 가 없어서 발생.
    @Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
//        private List<OrderItem> orderItems; //지금은 OrderItem의 Entity를 반환하고 있다. 하지만 OrderItem 도 Dto로 다 바꿔야한다.
        private List<OrderItemDto> orderItems; //Dto형태로 받음.

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());  //OrderItem을 Dto로 변환
        }
    }

    @Getter
    static class OrderItemDto {

        private String itemName; //상품 명
        private int orderPrice;  //주문 가격
        private int count;  //주문 수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
