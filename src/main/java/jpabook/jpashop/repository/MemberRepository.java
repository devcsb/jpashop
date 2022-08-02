package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
/*개발자는 인터페이스만 만들면 된다. 구현체는 스프링 데이터 JPA가 애플리케이션 실행시점에 주입해준다.*/
public interface MemberRepository extends JpaRepository<Member, Long> {

    //findByName => select m from Member m where m.name = :name 의 jpql을 대신 짜준다.
    // 구현체를 만들 필요 없이, spring-data-jpa가 findBy 뒤 문자열의 이름을 가지는 컬럼을 알아서 판단해서 쿼리를 짬.
    List<Member> findByName(String name);
}
