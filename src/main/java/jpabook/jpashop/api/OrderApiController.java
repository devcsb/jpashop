package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import jpabook.jpashop.service.query.OrderQueryService;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     * V1. 엔티티 직접 노출
     * - Hibernate5Module 모듈 등록, LAZY=null 처리
     * - 양방향 관계 문제 발생 -> @JsonIgnore
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();   //OSIV를 끄면, 여기서 에러 터짐. getMember()의 값이 프록시인데 초기화를 못해서 에러 발생. 영속성 컨텍스트가 없으므로.
            order.getDelivery().getAddress();

            List<OrderItem> orderItems = order.getOrderItems(); //OrderItem 초기화

            orderItems.stream().forEach(o -> o.getItem().getName()); //OrderItem 내부에 item이 있으므로 그것의 이름을 얻어와야하므로 프록시 초기화.
        }
        return all;
    }

    /*
     *  V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
     * - 트랜잭션 안에서 지연 로딩 필요
     */
    //엔티티를 외부에 노출시키지 말라는 뜻은, 단순히 Dto로 감싸는 걸로 끝내라는 말이 아니라, 의존관계에 엮인 모든 Entity를 Dto로 전부 바꿔서 써야한다는 뜻이다.
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() { // N+1
        List<Order> orders = orderRepository.findAllByString(new OrderSearch()); //Entity에서 orders 조회 (Entity 타입)
        List<OrderDto> result = orders.stream()  //Entity에서 Dto로 변환
                .map(OrderDto::new)
                .collect(toList());

        return result;
    }

    /*
     *  V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
     * - 페이징 시에는 N 부분을 포기해야함(대신에 batch fetch size? 옵션 주면 N -> 1 쿼리로 변경 가능)
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() { //쿼리가 1번 나감.
        List<Order> orders = orderRepository.findAllWithItem();

        List<OrderDto> result = orders.stream()
                .map(OrderDto::new)
                .collect(toList());

        return result;
    }

    /**
     * V3.1 엔티티를 조회해서 DTO로 변환 페이징 고려
     * - ToOne 관계는 fetch join으로 최적화하고, 나머지 ToMany관계는 지연로딩 + batch size 옵션으로 최적화.
     * - 컬렉션 관계는 hibernate.default_batch_fetch_size (글로벌 설정), @BatchSize(개별 설정)로 최적화한다.
     * batch size 옵션을 사용하면, 컬렉션이나, 프록시 객체를 한꺼번에 설정한 size 만큼 IN 쿼리로 조회한다.
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                        @RequestParam(value = "limit", defaultValue = "100") int limit) {

        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        List<OrderDto> result = orders.stream()
                .map(OrderDto::new)
                .collect(toList());

        return result;
    }

    /*
     *  V4. JPA에서 DTO로 바로 조회, 컬렉션 N 조회 (1 + N Query)
     * - 페이징 가능
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();

    }

    /*
     * V5. JPA에서 DTO로 바로 조회, 컬렉션 1 조회 최적화 버전 (1 + 1 Query)
     * - 페이징 가능
     * */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllbyDto_optimization();

    }

    /*
     * V6. JPA에서 DTO로 바로 조회, 플랫 데이터(1Query) (1 Query)
     * - 페이징 불가능! (데이터 row가 多쪽으로 뻥튀기 되므로)
     *쿼리는 한번이지만 조인으로 인해 DB에서 애플리케이션에 전달하는 데이터에 중복 데이터가 추가되므로 상황에 따라(가져오는 양이 클 때) V5 보다 더 느릴 수 도 있다.
     **/
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllbyDto_flat();// 데이터 중복이 발생! 수작업으로 중복제거 해야함.

        //OrderFlatDto를 OrderQueryDto 형태로 loop 돌면서 직접 변환
        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(),  //Dto에서 EqualsAndHashCode 기준 설정
                                o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                                o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
                        e.getKey().getAddress(), e.getValue())) //e.getValue()로 위에서 만든 컬렉션(OrderQueryItemDto)를 가져옴
                .collect(toList());
    }


    private final OrderQueryService orderQueryService;

    /*
     * OSIV OFF 일 때, Command와 Query를 분리하는 방식의 예.
     * */
    @GetMapping("/api/osiv-off/orders")
    public List<jpabook.jpashop.service.query.OrderDto> ordersV2OsivOff() { //쿼리가 1번 나감.
        return orderQueryService.ordersV2OsivOff();
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
                    .collect(toList());  //OrderItem을 Dto로 변환
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
