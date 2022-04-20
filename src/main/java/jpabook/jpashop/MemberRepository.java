package jpabook.jpashop;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class MemberRepository {

    @PersistenceContext
    private EntityManager em; //EntityManager 생성

    public Long save(Member member) {
        em.persist(member);
        return member.getId(); // CQS(Command Query Separation) 원칙에 의거, member객체가 아닌 member의 ID값만 반환한다.
                                //insert는 id만 반환하고(아무것도 없으면 조회가 안되니), update는 아무것도 반환하지 않고, 조회는 내부의 변경이 없는 메서드로 설계
    }

    public Member find(Long id) {
        return em.find(Member.class, id);
    }
}
