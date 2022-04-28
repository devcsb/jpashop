package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.xml.transform.Result;
import java.util.List;
import java.util.stream.Collectors;

//@Controller @ResponseBody  // => 두 어노테이션을 포함하는 어노테이션 : @RestController
/*@ResponseBody 가 안붙어있으면 그냥 viewResolver에 의해 해당 경로의 view페이지로 렌더링이 되고,
 @ResponseBody를 붙이면 리턴되는 타입에 따라 MessageConvert로 변환이 이루어진 후 반환. 주로 json을 반환하므로  MappingJacksonHttpMessageConverter
 에 의해 json으로 변환되어 반환한다.
*/
@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    /**
     * 조회 V1: 응답 값으로 엔티티를 직접 외부에 노출한다.
     * 문제점
     * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
     * - 기본적으로 엔티티의 모든 값이 노출된다.
     * - 응답 스펙을 맞추기 위해 로직이 추가된다. (@JsonIgnore, 별도의 뷰 로직 등등)
     * - 실무에서는 같은 엔티티에 대해 API가 용도에 따라 다양하게 만들어지는데, 한 엔티티에 각각의
     API를 위한 프레젠테이션 응답 로직을 담기는 어렵다.
     * - 엔티티가 변경되면 API 스펙이 변한다.
     * - 추가로 컬렉션을 직접 반환하면 항후 API 스펙을 변경하기 어렵다.(별도의 Result 클래스
     생성으로 해결)
     * 결론
     * - API 응답 스펙에 맞추어 별도의 DTO를 반환한다.
     */
//조회 V1: 안 좋은 버전, 모든 엔티티가 노출, @JsonIgnore -> 이건 정말 최악, api가 이거 하나인가! 화면에 종속적이지 마라!
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    /**
     * 조회 V2: 응답 값으로 엔티티가 아닌 별도의 DTO를 반환한다.
     */
    @GetMapping("/api/v2/members")
    public Result memberV2() {  //람다의 기본식 : (매개변수) -> {실행문}
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream() //findMembers로 스트림 생성
                .map(m -> new MemberDto(m.getName()))  // 스트림내 요소들에 대해 함수가 적용된 결과의 새로운 요소로 매핑해준다. m(findMembers 의 alias) 을 새 Dto의 필드에 저장
                .collect(Collectors.toList()); // stream의 아이템들을 List 자료형으로 변환

        return new Result(collect.size(),collect);
    }

    @Data
    @AllArgsConstructor
    static class Result<T>{ //응답 값의 껍데기 역할을 하는 클래스
        private int count;  // 한 번 감싸면, 이렇게 속성을 추가할 수 있다.
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto{
        private String name;
    }

    /**
     * 등록 V1: 요청 값으로 Member 엔티티를 직접 받는다. (Entity(Member)와 api가 1:1로 맵핑)
     * 문제점
     * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다. => entity가 바뀌면 api 스펙도 바뀌는 문제 발생!
     * - 엔티티에 API 검증을 위한 로직이 들어간다. (@NotEmpty 등등)
     * - 실무에서는 회원 엔티티를 위한 API가 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 모든 요청 요구사항을 담기는 어렵다.
     * - 엔티티가 변경되면 API 스펙이 변한다.
     *
     * 결론
     * - API 요청 스펙에 맞추어 별도의 DTO를 파라미터로 받는다.
     */
    //Entity로 그대로 받으면, api 스펙을 까보지 않는 이상, 해당 Entity의 어느 필드까지 파라미터로 넘어오는 건지 알 수가 없다.
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {  // @RequestBody => json으로 넘어온 데이터를 Member로 맵핑시킨다
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    //DTO 사용한 버전. Entity 필드가 바뀌면 컴파일 오류가 나므로, setter함수 이름만 바꿔주면 api스펙은 그대로 놔두고 쓸 수 있다.
    // Dto를 만들어서 받으면 Dto만 봐도 api 스펙에서 넘어오는 값을 유추할 수 있다. Validation도 api 스펙에 맞게 Dto에서 따로 따로 정할 수 있다.
    /**
     * 등록 V2: 요청 값으로 Member 엔티티 대신에 별도의 DTO를 받는다.
     */
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request) {

        memberService.update(id, request.getName()); // CQS(Command Query Separation)을 지키기 위해, update된 member를 바로 반환받지 않고, 한 줄 밑 코드로 다시 조회함.
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data
    static class UpdateMemberRequest{
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data //Dto
    static class CreateMemberRequest {
        @NotEmpty
        private String name;
    }

    @Data  //Lombok
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

}
