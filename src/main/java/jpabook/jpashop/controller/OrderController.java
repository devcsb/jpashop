package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.service.ItemService;
import jpabook.jpashop.service.MemberService;
import jpabook.jpashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MemberService memberService;
    private final ItemService itemService;

    @GetMapping("/order")
    public String createForm(Model model) {
        List<Member> members = memberService.findMembers();
        List<Item> items = itemService.findItems();

        model.addAttribute("members", members);
        model.addAttribute("items", items);

        return "order/orderForm";
    }

    /* @RequestParam : 사용자가 요청시 전달하는 값을 Controller의 매개변수로 1:1 맵핑할 때 사용되는 어노테이션 */
    @PostMapping("/order")
    public String order(@RequestParam("memberId") Long memberId,
                        @RequestParam("itemId") Long itemId,
                        @RequestParam("count") int count) {

        /*바깥에서 Entity를 찾아서 넣는 것 보단, 아래처럼 식별자만 넘겨주고,
        Service단 트랜잭션 안에서 엔티티를 찾고 비즈니스 로직을 진행하는 편이 좋다.*/
        /*Entity를 찾을 때는 항상 트랜잭션 안에서 찾자. (영속성 컨텍스트의 dirty checking 등을 이용할 수 있는 등, 편하다)*/
        orderService.order(memberId, itemId, count);

        return "redirect:/orders";
    }

    @GetMapping("/orders")
    public String orderList(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model) {
        List<Order> orders = orderService.findOrders(orderSearch);// 단순 조회일 경우 service에 위임을 굳이 할 필요는 없다.
//        model.addAttribute("orderSearch", orderSearch);  // @ModelAttribute를 사용하면 자동 객체생성, Form으로 넘어온 값 자동 바인딩, model에 자동 추가 해줌. 해당 코드가 생략되어 있다고 생각하면 됨.
        model.addAttribute("orders", orders);

        return "order/orderList";
    }

    @PostMapping("/orders/{orderId}/cancel")
    public String cancleOrder(@PathVariable("orderId") Long orderId) {
        orderService.cancelOrder(orderId);
        return "redirect:/orders";
    }
}
