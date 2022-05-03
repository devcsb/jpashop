package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    /**
     * 컬렉션은 별도로 조회
     * Query: 루트 1번, 컬렉션 N 번
     * 단건 조회에서 많이 사용하는 방식.
     */
    public List<OrderQueryDto> findOrderQueryDtos() {

        //루트 조회(toOne 코드를 모두 한번에 조회)  // ToMany관계는 join 시 row수가 늘어나므로
        List<OrderQueryDto> result = findOrders(); // Query 1번 실행 -> 가져온 row 수 = N개

        //루프를 돌면서 컬렉션 추가(추가 쿼리 실행)
        result.forEach(o -> { //OrderQueryDto 생성자에서 채우지 못한 Item을 반복문 돌려서 채움
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());  //Query N번 실행
            o.setOrderItems(orderItems);  //가져온 id값을 OrderQueryDto의 orderItems에 넣음.
        });
        return result;
    }

    /**
     * 최적화
     * Query: 루트 1번, 컬렉션 1번
     * 데이터를 한꺼번에 처리할 때 많이 사용하는 방식
     * 컬렉션 구하는 쿼리를 in절로 한번에 가져오고 어플리케이션단 memory에서 값을 매칭하는 방법.
     */
    public List<OrderQueryDto> findAllbyDto_optimization() {
        //루트 조회(toOne 코드를 모두 한번에 조회)
        List<OrderQueryDto> result = findOrders();  //쿼리 1번 실행

        List<Long> orderIds = toOrderIds(result); //stream으로 id 전부 추출

        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(orderIds); // OrderItem 모두 가져온 뒤 Map으로 변환

        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId()))); //memory에 올려둔 orderItem을 찾아서 order에 넣어준다.

        return result;
    }

    /**
     * 1:N 관계(컬렉션)를 제외한 나머지를 한번에 조회
     */
    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                        "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                                " from Order o" +
                                " join o.member m" +
                                " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

    /**
     * 1:N 관계인 orderItems 조회
     */
    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                        "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                                " from OrderItem oi" +
                                " join oi.item i" +
                                " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    /* stream으로 id 전부 추출 */
    private List<Long> toOrderIds(List<OrderQueryDto> result) {
        List<Long> orderIds = result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
        return orderIds;
    }

    /* 추출한 id값을 in절로 OrderItem 모두 가져옴  //쿼리 1번 실행 */
    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery(
                        "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                                " from OrderItem oi" +
                                " join oi.item i" +
                                " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream() // key가 orderId, 값이 OrderItemQueryDto인 Map 으로 변환.
                .collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));
        return orderItemMap;
    }


    public List<OrderFlatDto> findAllbyDto_flat() {

        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d" +
                        " join o.orderItems oi" +
                        " join oi.item i", OrderFlatDto.class)
                .getResultList();
    }
}
