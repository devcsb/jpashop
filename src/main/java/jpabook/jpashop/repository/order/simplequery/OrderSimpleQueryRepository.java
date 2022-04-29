package jpabook.jpashop.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager em;

    //JPA는 기본적으로 Entity나 VO(Embedabble타입)만 반환할 수 있다. Dto에 넣으려면 new 명령어를 사용해서 dto를 생성하여 사용한다.
    public List<OrderSimpleQueryDto> findOrderDtos() {
//        return em.createQuery("select o from Order o" +
//                " join o.member m" +
//                " join o.delivery d", OrderSimpleQueryDto.class) //select o 의 값이 Dto로 매칭 안됨.
//                .getResultList();

        //new operation에서 엔티티를 바로 넘기는 것은 불가능.
        return em.createQuery("select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderSimpleQueryDto.class) //select o 의 값이 Dto로 매칭 안됨.
                .getResultList();


    }

}
