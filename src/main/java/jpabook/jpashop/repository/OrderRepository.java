package jpabook.jpashop.repository;

import jpabook.jpashop.api.OrderSimpleApiController;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAll(OrderSearch orderSearch) {

        //jpql 작성하는 createQuery() 의 매개변수 : String qlString, Class<Object> resultClass
        /*모든 검색 옵션값이 있을 때의 쿼리*/
        return em.createQuery("select o from Order o join o.member m" +
                        " where o.status = :status" +  //+로 문자열 합치므로, 맨 앞에 한 칸 띄어쓰기 해줌.
                        " and m.name like :name", Order.class)
                .setParameter("status", orderSearch.getOrderStatus())  //파라미터 바인딩
                .setParameter("name", orderSearch.getMemberName())
//                .setFirstResult(100)  // 100번째부터 가져온다. (페이징 시 사용)
                .setMaxResults(1000)  // 최대 1000건을 가져온다.
                .getResultList();

        /*아무 검색옵션 값이 없을 때의 쿼리.*/
//        return em.createQuery("select o from Order o join o.member m", Order.class)
//                .setMaxResults(1000)  // 최대 1000건을 가져온다.
//                .getResultList();

    }

    //동적쿼리를 생성하는 방법은  (P.61참조)
    // 1. jpql 쿼리를 문자로 생성하는 방법, 2. JPA Criteria를 사용하는 방법
    // 3.Querydsl을 사용하는 방법이 있으나 방법 1,2는 너무 복잡해서 3번을 통해 해결한다.

    /*JPQL 쿼리를 문자로 생성하는 방법*/
    public List<Order> findAllByString(OrderSearch orderSearch) {

        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

    /*
     * JPA Criteria를 이용한 동적쿼리 사용법
     * */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" +
                            orderSearch.getMemberName() + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건
        return query.getResultList();
    }


    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(  //order, member, delivery를 한번에 다 가져옴. fetch는 JPA의 문법.
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();

    }

    public List<SimpleOrderQueryDto> findOrderDtos() {

    }
}
