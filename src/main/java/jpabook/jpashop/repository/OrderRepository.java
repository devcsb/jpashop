package jpabook.jpashop.repository;

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

    /*
     * JPA의 distinct는 SQL에 distinct를 추가하고, 더해서 같은 엔티티가
     * 조회되면, 애플리케이션에서 중복을 걸러준다. 이 예에서 order가 컬렉션 페치 조인 때문에 중복 조회 되는
     * 것을 막아준다.
     * db의 distinct는 모든 값이 같아야 중복제거 되지만, JPA의 distinct는 Root Entity가 중복인 경우 그걸 애플리케이션단에서 다시 걸러준다.
     *
     *  xToMany 관계에서 fetch join(collection fetch join)을 하면 절대 안된다!
     *  일대다 join을 한 순간, 데이터 row가 多쪽을 기준으로 뻥튀기 되면서 페이징이 기준이 완전히 틀어지므로 DB에서 페이징을 할 수 없음.
     *  결국 하이버네이트는 경고 로그를 남기면서 모든 데이터를 DB에서 읽어오고, 메모리에서 페이징 해버린다(매우 위험하다)
     *
     * 컬렉션 페치 조인은 1개만 사용할 수 있다. 컬렉션 둘 이상에 fetch join을 사용하면 안된다. ex) 1 : N : M 관계에서 불가능
     * */
    public List<Order> findAllWithItem() {
        // select distinct o from Order o => Order의 id값이 같으면, 그중 하나만 남겨서 반환해줌.(중복제거)
        return em.createQuery(
                        "select distinct o from Order o" +
                                " join fetch o.member m" +
                                " join fetch o.delivery d" +
                                " join fetch o.orderItems oi" +
                                " join fetch oi.item i", Order.class)
//                .setFirstResult(1)
//                .setMaxResults(100)
                .getResultList();
    }

    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery(
                        "select o from Order o" +
                                " join fetch o.member m" +
                                " join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

    }
}
