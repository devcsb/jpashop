package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class SimpleOrderQueryDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;
/*의존관계를 한 방향으로 설계해야 한다. controller -> service -> repository*/
    //Dto가 Entity를 파라미터로 받는 것은 큰 문제가 되지 않는다. 중요하지 않은 곳에서 중요한 Entity를 의존하는 것이기 때문.
    public SimpleOrderQueryDto(Order order) {
        orderId = order.getId();
        name = order.getMember().getName();  //LAZY 초기화 : 영속성 컨텍스트에 올라온 해당 객체(Member)를 명시해준 매핑컬럼(member_id)를 이용해서 찾아보고, 없으면 DB에서 가져온다.
        orderDate = order.getOrderDate();
        orderStatus = order.getStatus();
        address = order.getDelivery().getAddress();  //LAZY 초기화
    }
}
