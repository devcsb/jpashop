package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    /*변경감지를 이용한 방법 */
    @Transactional //트랜잭션이 커밋됨 => JPA는 flush()를 날림 => 영속성 컨텍스트에서 변경된 값을 update하는 쿼리가 날아감.
    public Item updateItem(Long itemId, String name, int price, int stockQuantity) {
        Item findItem = itemRepository.findOne(itemId);  //id로 db에 있는 영속상태의 item을 찾아옴.
        //setter를 사용한 안좋은 예. 조금만 복잡해져도 도대체 어디서 변경이 되는지 알아보기 힘들다.
        findItem.setName(name);
        findItem.setPrice(price);
        findItem.setStockQuantity(stockQuantity);

        /*업데이트는 setter를 남발하는 것이 아닌, Entity 내에 의미있는 메서드를 만들어서 변경하도록 해야 한다. 어디서 바뀌는지 쉽게 추적이 가능*/
//        findItem.change(price, name, stockQuantity);

        //파라미터 개수가 많으면, Dto를 활용하자.
        return findItem;
    }


    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }

}
