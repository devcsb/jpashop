package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

//@Controller @ResponseBody  // => 두 어노테이션을 포함하는 어노테이션 : @RestController
/*@ResponseBody 가 안붙어있으면 그냥 viewResolver에 의해 해당 경로의 view페이지로 렌더링이 되고,
 @ResponseBody를 붙이면 리턴되는 타입에 따라 MessageConvert로 변환이 이루어진 후 반환. 주로 json을 반환하므로  MappingJacksonHttpMessageConverter
 에 의해 json으로 변환되어 반환한다.
*/
@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    /* Entity(Member)와 api가 1:1로 맵핑되어 있음. entity가 바뀌면 api 스펙도 바뀌는 문제 발생! 절대 Entity를 그대로 쓰지 말고 DTO를 사용해야함.*/
    //Entity로 그대로 받으면, api 스펙을 까보지 않는 이상, 해당 Entity의 어느 필드까지 파라미터로 넘어오는 건지 알 수가 없다.
    /**
     * 등록 V1: 요청 값으로 Member 엔티티를 직접 받는다.
     * 문제점
     * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
     * - 엔티티에 API 검증을 위한 로직이 들어간다. (@NotEmpty 등등)
     * - 실무에서는 회원 엔티티를 위한 API가 다양하게 만들어지는데, 한 엔티티에 각각의 API를
     위한 모든 요청 요구사항을 담기는 어렵다.
     * - 엔티티가 변경되면 API 스펙이 변한다.
     * 결론
     * - API 요청 스펙에 맞추어 별도의 DTO를 파라미터로 받는다.
     */
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

    @Data //Dto
    static class CreateMemberRequest {
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
