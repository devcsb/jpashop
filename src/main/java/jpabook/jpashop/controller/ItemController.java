package jpabook.jpashop.controller;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookForm());
        return "items/createItemForm";
    }

    @PostMapping("/items/new")
    public String create(BookForm form) {
        /*일일히 setter로 주입하기보다, 정적 팩토리 메서드를 만들어놓고 호출하여 쓰는 것이 좋은 설계방식임.
         * 예제라서 이렇게 할 뿐, 실무에서는 setter를 닫아놓고 정적팩토리메서드로 생성할 것. */
        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());

        itemService.saveItem(book);
        return "redirect:/";
    }

    @GetMapping("/items")
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }

    @GetMapping("items/{itemId}/edit") //PathVariable을 바인딩
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {
        Book item = (Book) itemService.findOne(itemId); //반환값이 Item 타입이지만 예제를 심플하게 하기 위해 Book으로 타입 캐스팅

        // ctrl 두 번 누른채로 화살표 위 아래로 커서 복사 가능
        BookForm form = new BookForm();
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setAuthor(item.getAuthor());
        form.setIsbn(item.getIsbn());

        model.addAttribute("form", form);
        return "items/updateItemForm";
    }

    /*  @ModelAttribute를 사용하면 HTTP 파라미터 데이터를 Java 객체에 맵핑한다.
        따라서 객체의 필드에 접근해 데이터를 바인딩할 수 있는 생성자 혹은 setter 메서드가 필요하다.
        Query String 및 Form 형식이 아닌 데이터는 처리할 수 없다.*/
//    ① @ModelAttribute 어노테이션이 붙은 객체를 자동으로 생성한다.
//    ② 생성된 오브젝트에(info) HTTP로 넘어 온 값들을 자동으로 바인딩한다.
//    ③ @ModelAttribute 어노테이션이 붙은 객체가 자동으로 Model 객체에 추가된다.
    @PostMapping("items/{itemId}/edit")
    public String updateItem(@PathVariable Long itemId, @ModelAttribute("form") BookForm form) {
        //한 번에 setter 작성하는 법: DTO나 Form에서 필드값을 모두 가져온다. ctrl 두번+ 방향키로 커서 복사하는 법 이용.
        //빈 공간에 복사 후 커서 복사하여 shift + Tap으로 공백 제거 후 다시 복사, 대문자 변환 단축키 활용하여 작성( ctrl + shift + U)
        /*form에서 itemid를 조작할 수 있으므로 권한 체크하는 로직이 있어야한다.*/

        /*준영속 상태의 객체*/
        /*book의 모든 필드를 다 주입하지 않은 채로 영속성컨텍스트에 merge()하면, 해당 필드는 DB에 null값으로 들어가므로. merge는 웬만해선 사용하지 말자*/
//      Book 객체는 이미 DB에 한번 저장되어서 식별자가 존재한다.
//      이렇게 임의로 만들어낸 엔티티도 기존 식별자를 가지고 있으면 준영속 엔티티로 볼 수 있다.
//        Book book = new Book();  //new로 새로 객체를 만들었지만, setId로 기존 엔티티의 식별자를 넣었으므로 준영속
//        book.setId(form.getId());
//        book.setName(form.getName());
//        book.setPrice(form.getPrice());
//        book.setStockQuantity(form.getStockQuantity());
//        book.setAuthor(form.getAuthor());
//        book.setIsbn(form.getIsbn());
//        itemService.saveItem(book);

        /*위 코드처럼 컨트롤러에서 어설프게 엔티티를 생성하지 말자.*/
        itemService.updateItem(itemId, form.getName(), form.getPrice(), form.getStockQuantity());  //이렇게 하거나, 데이터가 많으면, DTO를 만들어서 전달

        return "redirect:/items";

    }
}
