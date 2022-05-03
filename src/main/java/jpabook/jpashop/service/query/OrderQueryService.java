package jpabook.jpashop.service.query;

import jpabook.jpashop.api.OrderApiController;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true) //읽기 전용 트랜잭션
@RequiredArgsConstructor
public class OrderQueryService { // 컨트롤러의 변환 로직을 따로 QueryService로 가져와서 모든 작업 처리하는 방식.

    //한 Repository나 Service에 핵심 비즈니스 로직과 화면에 맞춘 복잡한 조회 로직은  서로 라이프사이클이 다르므로, 분리하는 편이 좋다.
    //OrderService: 핵심 비즈니스 로직
    //OrderQueryService: 화면이나 API에 맞춘 서비스 (주로 읽기 전용 트랜잭션 사용)

    private final OrderRepository orderRepository;

    public List<OrderDto> ordersV2OsivOff() { //쿼리가 1번 나감.
        List<Order> orders = orderRepository.findAllByString(new OrderSearch()); //Entity에서 orders 조회 (Entity 타입)
        List<OrderDto> result = orders.stream()  //Entity에서 Dto로 변환
                .map(OrderDto::new)
                .collect(toList());

        return result;
    }
}
