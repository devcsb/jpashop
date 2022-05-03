package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

/*
 *JUnit 5에서는 JUnit 4에서 제공하던 @RunWith를 쓸 필요가 없고
 * @ExtendWith를 사용해야 하지만, 이미 @SpringBootTest에 메타 애노테이션으로 적용되어 있기 때문에
 * @ExtendWith(SpringExtension.class)를 생략할 수 있다.
 *
 *  */
@SpringBootTest //스프링 띄운 상태에서 테스트하기 위해 필요
@Transactional  //@Transactional이 테스트케이스에 있으면, 테스트가 끝난다음 바로 롤백을 하는 것이 기본값임. 그대로 커밋 시키려면  @rollback(false) 옵션을 주면 됨.
class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em; //롤백은 하지만 flush 해줘서 db에 쿼리가 나가는 걸 보기 위해서 em 주입

    @Test
//    @Rollback(value = false)
    void 회원가입() throws Exception{
        //given
        Member member = new Member();
        member.setName("kim");
        
        //when
        Long saveId = memberService.join(member);

        //then
        em.flush(); // 쓰기 지연 sql저장소의 쿼리를 db에 수동 flush
        assertEquals(member, memberRepository.findOne(saveId));
        //JPA에서 같은 트랜잭션 안에서 id값(pk값)이 같은 엔티티는 영속성 컨텍스트에서 똑같은 값을 가지므로.

    }
    
    @Test //중복회원일 때 성공하는 테스트
    void 중복_회원_예외() throws Exception{
        //given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");

        //when
        memberService.join(member1);

        //then
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            memberService.join(member2);
        });

        assertEquals("이미 존재하는 회원입니다.", thrown.getMessage());

        //assertEquals로 비교 없이 그냥 아래와 같이 해도 정상적으로 됨.
        // assertThrows(IllegalStateException.class, () -> {
        //            memberService.join(member2);
        //        });

    }
}