package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        if (item.getId() == null) { //id값이 없을 때 == item을 새로 생성해야 할 경우
            em.persist(item);
        }else{ // id값이 있을 때 ==이미 db에 등록되있는 값을 가져온 경우 == item을 업데이트 해야하는 경우
           /*가급적 merge는 쓰지 않는다. merge는 전체 필드를 업데이트시킴. 필드값 누락시 null값이 들어가므로 위험함.*/
            em.merge(item);
        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }
}
