package jpabook.jpashop;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;


/**
 * 총 주문 2개
 * userA
   * JPA1 BOOK
   * JPA2 BOOK
 * userB
   * SPRING1 BOOK
   * SPRING2 BOOK
 * */
@Component // 1. 컴포넌트 스캔됨.
@RequiredArgsConstructor
public class InitDb {

    private final InitService initService;

    // 2. 스프링 빈이 다 엮인 뒤, init함수 실행.(@PostConstruct 역할)
    @PostConstruct  //애플리케이션 로딩 시점에 실행되는 초기화 메서드
    public void init() {
        initService.dbInit1();  //스프링 라이프사이클 때문에 초기화메서드에서 트랜잭션 먹이고 하는 것들이 잘 안되므로, 아래 처럼 별도의 빈으로 등록해서 사용한다.
        initService.dbInit2();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {
        private final EntityManager em;

        /*메서드 복사 후, 변수 다르게 한 뒤 공통부분 드래그 + ctrl + alt + M 으로 메서드 추출*/
        public void dbInit1() {
            Member member = createMember("userA", "서울", "테헤란로1번길", "14464");
            em.persist(member);

            Book book1 = createBook("JPA1 BOOK", 10000, 100);
            em.persist(book1);

            Book book2 = createBook("JPA2 BOOK", 20000, 100);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 2);

            Delivery delivery = createDelivery(member);
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);//orderItem을 여러개 넘김. 배열로 넘어감.
            em.persist(order);

        }

        private Delivery createDelivery(Member member) {
            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());
            return delivery;
        }


        public void dbInit2() {
            Member member = createMember("userB", "부산", "달맞이로 1번길", "72085");
            em.persist(member);

            Book book1 = createBook("SPRING1 BOOK", 20000, 200);
            em.persist(book1);

            Book book2 = createBook("SPRING2 BOOK", 40000, 300);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 20000, 3);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 40000, 4);

            Delivery delivery = createDelivery(member);
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);//orderItem을 여러개 넘김. 배열로 넘어감.
            em.persist(order);

        }

        //메소드 이름 라인에 커서 두고 CTRL + SHIFT + 화살표 => 메서드 블록 위치 조절
        private Member createMember(String name, String city, String street, String zipcode) {
            Member member = new Member();
            member.setName(name);
            member.setAddress(new Address(city, street, zipcode));
            return member;
        }

        private Book createBook(String name, int price, int stockQuantity) {
            Book book1 = new Book();
            book1.setName(name);
            book1.setPrice(price);
            book1.setStockQuantity(stockQuantity);
            return book1;
        }
    }
}
