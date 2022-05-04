package jpabook.jpashop.service;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    /*
     * 주문
     * */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {

        //엔티티 조회
        Member member = memberRepository.findById(memberId).get();
        Item item = itemRepository.findOne(itemId);

        //배송정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        //주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count); // 생성자 대신 정적 생성 메서드를 통해 객체 생성

        /*협업 시에 다른 개발자가 이런 식으로 다른 방식으로 생성하는 경우가 생길 수 있으므로, 기본 생성자의 접근제한자를 지정해서 사전에 막아야 함.*/
//        OrderItem orderItem1 = new OrderItem();
//        orderItem1.setCount();

        //주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        //주문 저장
        orderRepository.save(order); //Cascade 옵션을 all로 설정했기 때문에, persist()를 order 하나만 해줘도, orderItem과 delivery 모두 영속성컨텍스트에 persist된다.

        return order.getId();
    }

    /*
     * 주문 취소
     * */
    @Transactional
    public void cancelOrder(Long orderId) {
        //주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);
        //주문 취소
        order.cancel(); //JPA가 dirty checking을 해서 변경된 부분만 업데이트하는 쿼리가 날아간다.
    }

    /*
    * 주문 검색
    * */
    public List<Order> findOrders(OrderSearch orderSearch) {
        return orderRepository.findAll(orderSearch);
    }
}
