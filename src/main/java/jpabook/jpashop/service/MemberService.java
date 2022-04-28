package jpabook.jpashop.service;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; //@Transaction는 spring이 제공하는 것을 쓸 것.
import java.util.List;

//JPA의 모든 데이터 변경이나 로직은 트랜잭션 안에서 모두 실행하도록 한다. 클래스 레벨에서 @Transactional을 쓰면 public 메서드에 모두 적용됨.
@Service
@Transactional(readOnly = true) // db를 읽기만 하는 메서드가 많으므로, 클래스 레벨에서 readOnly = true 적용하고, 쓰기를 하는 메서드에서는 따로 어노테이션을 달아주는 방식으로 구현.
@RequiredArgsConstructor //Lombok 기능. 초기화 되지 않은 final 필드와 @NonNull 어노테이션이 붙은 필드에 대한 생성자 생성
public class MemberService {

    private final MemberRepository memberRepository;

    /*
    * 생성자 주입을 Lombok의 어노테이션으로 대체할 수 있음.
    * @AllArgsConstructor 모든 필드에 대한 생성자 생성.
    * @RequiredArgsConstructor 초기화 되지 않은 final 필드와 @NonNull 어노테이션이 붙은 필드에 대한 생성자 생성
    * */
//    @Autowired
//    public MemberService(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }

    /*
    *  회원 가입
    * */
    @Transactional // 기본값 : readOnly = true
    public Long join(Member member) {
        validateDuplicateMember(member);  //중복 회원 검증
        memberRepository.save(member);  // em.persist(member) => 영속성 컨텍스트에 member객체의 @Id 값이 key 로, member 엔티티가 value 형태로 저장됨. db에 들어간 시점이 아니어도 id필드에 값을 채워줌.
        return member.getId();  //그러므로 값을 꺼내면 항상 값이 있다는 것이 보장됨.
    }

    /*
    * 중복 회원 검증  // TIP : WAS가 여러대이므로, 두 클라이언트가 동시에 작업 수행하게되어 validation을 통과하게 되면, 같은 이름 회원이 2개 저장될 수 있으므로, DB단에서 name 컬럼을 유니크값으로 잡아주도록 한다.
    * */
    private void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getName()); // 멀티쓰레드 상황을 고려해서 DB에서 NAME을 Unique값으로 잡아주는 것이 안전하다.
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    /*
    * 회원 전체 조회
    * */
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    /*
    * 회원 단 건 조회
    * */
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }

    /*회원수정 api -6분부터 흐름 jpa 처리 흐름 참고*/
    @Transactional //트랜잭션 시작
    public void update(Long id, String name) { /* 만약, public member update() 이런식으로 변경한 member를 반환하게 되면, CQS 원칙을 위배하게 된다. */
        Member member = memberRepository.findOne(id); //영속성 컨텍스트에서 findOne해서 찾음 -> 찾은게 없으므로 DB에서 가져와서 영속성 컨텍스트에 올리고, member변수에 반환. 현재 member는 영속상태
        member.setName(name); //영속상태의 member의 속성값 변경.
    }//트랜잭션 종료되고 commit되기 직전 시점에 jpa가 flush()를 하고, DB commit이 일어난다.

    /*flush의 동작과정
    * - 변경 감지(dirty checking -> 수정된 Entity를 쓰기 지연 SQL 저장소에 등록 -> 지연 저장소의 Query를 DB에 전송
    * */
}
