package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository  //스프링 빈으로 등록, JPA 예외를 스프링 기반 예외로 예외 변환
@RequiredArgsConstructor //Lombok이 제공하는 어노테이션
public class MemberRepository {

    /* 고전적 EntityManager 주입방식.*/
//    @PersistenceContext  //스프링이 생성한 엔티티 메니저( EntityManager ) 주입
//    private EntityManager em;  //스프링이 entityManager를 만들어서 em에 주입해준다.

    /* spring-data-jpa 라이브러리에서 제공해주는 EntityManager 주입방식*/
//    @Autowired
//    private EntityManager em;

    /*다시 Lombok 활용해서 @Autowired 제거 후 final 붙이고 @RequiredArgsConstructor 추가*/
    private final EntityManager em;

    //회원 저장
    public void save(Member member) {
        em.persist(member); //영속성 컨텍스트에 member를 넣는다. (트랜잭션이 commit 되는 시점에 DB에 insert 쿼리가 날아감.)
        // em.persist(member) => 영속성 컨텍스트에 member객체의 @Id 값이 key 로, member 엔티티가 value 형태로 저장됨. db에 들어간 시점이 아니어도 id필드에 값을 채워줌.
    }

    //id값으로 회원 하나 찾아서 반환
    public Member findOne(Long id) {
        return em.find(Member.class, id); //단건 조회
    }

    // 모든 멤버 찾아서 반환.
    public List<Member> findAll() {
        //JPQL : Entity 객체를 대상으로 SQL를 날리는 문법
        List<Member> result = em.createQuery("select m from Member m", Member.class)  //Member 객체 m에 대한 조회를 해라.
                .getResultList();
        return result;  // ctrl + alt + N 단축키로 변수에 담지 않고 바로 리턴하도록 합치는 것이 좋다.
    }

    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)  // :파라미터명 으로 파라미터 변수 바인딩
                .setParameter("name", name) //파라미터 주입
                .getResultList();
    }


}
