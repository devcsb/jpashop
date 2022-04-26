package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/members/new")
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm()); // 컨트롤러에서 뷰로 넘어갈 때 memberForm의 빈 껍데기 객체를 실어서 넘긴다. validation 등을 사용하기 위해.
        return "members/createMemberForm";
    }


    /*member Entity를 바로 바인딩 받지 않고 따로 memberForm을 만드는 이유
     * 1. 폼에서 넘어오는 값과 엔티티 필드가 정확히 일치 하지 않으므로
     * 2. 폼에서 넘어오는 validation과 실제 도메인이 원하는 validation이 다를 수 있으므로 따로 적용하기 위해
     * 3. Entitiy로 다 받으면 코드가 지저분해지고 Entity에 화면에 종속적인 기능이 계속 생기고 모듈간의 결합도가 높아진다.*/

    /* 반드시 Entity는 최대한 순수하게 유지해야한다. 핵심 비즈니스 로직만 담겨야 한다.
    * 화면에 맞는 api는 Form객체나 DTO를 사용하도록 하자.
    * */
    @PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult result) {  //파라미터 앞에 @Valid를 붙여서 validation 기능 사용

        //BindingResult를 사용하면 오류를 담아서 처리할 수 있다.

        if (result.hasErrors()) {
            return "members/createMemberForm";  //에러가 발생했어도 이미 form에 들어온 값들은 form에 담겨서 뷰로 다시 가져간다.
        }

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());

        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);

        return "redirect:/"; // 회원가입 같은 기능은 재로딩 되면 안좋기 때문에 홈으로 redirect
    }

//    지금은 서버사이드렌더링을 하는 데다가, Entity를 손댈 곳이 없어서 그대로 뿌렸지만,
//    조금 복잡해지면 DTO로 변환을 해서 화면에 꼭 필요한 데이터만 출력하도록 하자.
    /*API를 만들 때는 **절대로** Entity를 외부로 반환하면 안된다!
    * 1. 엔티티에 필드가 추가되면 API 스펙도 같이 변하기 때문에 매우 불완전한 API 스펙이 된다.
    * 2. 필드에 password 같은 값이 노출되는 보안 문제 또한 발생.
    *  */
    @GetMapping("/members")
    public String list(Model model) {
        List<Member> members = memberService.findMembers();  //ctrl + alt + N 으로 inline 할 수 있다.
        model.addAttribute("members", members);
        return "members/memberList";
    }
}
