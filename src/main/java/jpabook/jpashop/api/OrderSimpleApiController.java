package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.SimpleOrderQueryDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne
 * Order
 * Order -> member
 * Order -> delivery
 */
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

    /**
     * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
     * - 단점: 지연로딩으로 쿼리 N번 호출
     * 쿼리가 총 1 + N + N번 실행된다. (v1과 쿼리수 결과는 같다.)
     * order 조회 1번(order 조회 결과 수가 N이 된다.)
     * order -> member 지연 로딩 조회 N 번
     * order -> delivery 지연 로딩 조회 N 번
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() { // List로 반환하지 말고 Result 객체를 생성하여 한 번 감싸서 반환할 것. 예제라서 생략.
        //ORDER 2개 // N+1 문제 발생! => ORDER 조회 1 + 회원 N(ORDER 개수가 2개이므로 N =2) + 배송 N
        /*EAGER로 해결이 안되는 이유 : 일단 처음 ORDER를 들고옴 -> 까보니 연관관계에 EAGER가 있음을 발견 -> 한 번에 들고 오려고 수많은 쿼리가 날아감*/
        List<Order> orders = orderRepository.findAllByString(new OrderSearch()); //List 그대로 반환 말고 Dto로 바꿈.

        List<SimpleOrderDto> result = orders.stream() //orders를 stream으로 돌리면서 map으로 order를 SimpleOrderDto로 변환하고, collect()로 다시 List로 변환해서 반환.
                .map(o -> new SimpleOrderDto(o))  //.map(SimpleOrderDto::new) 이런식으로 람다 레퍼런스 이용한 축약 가능
                .collect(Collectors.toList());

        return result;
    }

    /**
     * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
     * - fetch join으로 쿼리 1번 호출
     * 참고: fetch join에 대한 자세한 내용은 JPA 기본편 참고(정말 중요함)
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    @GetMapping("/api/v4/simple-orders")
    public List<SimpleOrderQueryDto> ordersV4() {
        return orderRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        //Dto가 Entity를 파라미터로 받는 것은 큰 문제가 되지 않는다. 중요하지 않은 곳에서 중요한 Entity를 의존하는 것이기 때문.
        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();  //LAZY 초기화 : 영속성 컨텍스트에 올라온 해당 객체(Member)를 명시해준 매핑컬럼(member_id)를 이용해서 찾아보고, 없으면 DB에서 가져온다.
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();  //LAZY 초기화
        }
    }

}
