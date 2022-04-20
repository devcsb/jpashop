package jpabook.jpashop;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

//@RunWith(SpringRunner.class)

/*
 *JUnit 5에서는 JUnit 4에서 제공하던 @RunWith를 쓸 필요가 없고
 * @ExtendWith를 사용해야 하지만, 이미 @SpringBootTest에 메타 애노테이션으로 적용되어 있기 때문에
 * @ExtendWith(SpringExtension.class)를 생략할 수 있다.
 *
 *  */
@SpringBootTest
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;

    //EntityManager를 통한 모든 데이터 변경은 항상 트랜잭션 안에서 이루어져야 한다!
    // @Transactional을 붙여서 트랜잭션을 걸어주지 않으면 No EntityManager with actual transaction available for current thread 에러가 발생함.

    //@Transactional이 테스트케이스에 있으면, 테스트가 끝난다음 바로 롤백을 하는 것이 기본값이므로, 따로 @rollback(false)옵션을 주었음.
    @Test
    @Transactional
    @Rollback(false)
    public void testMember() throws Exception{
        //given
        Member member = new Member();
        member.setUsername("memberA");

        //when
        Long saveId = memberRepository.save(member);
        Member findMember = memberRepository.find(saveId);

        //then
        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
        Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        Assertions.assertThat(findMember).isEqualTo(member);
        System.out.println("findMember == member: " + (findMember == member));
    }
}