package jpabook.jpashop.domain.item;

import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) //상속관계 전략을 부모테이블에서 잡아준다. 현재는 SINGLE_TABLE 전략을 쓰고 있음.
@DiscriminatorColumn(name = "dtype") //구분해줄 컬럼명 명시
@Getter @Setter
public class Item {

    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    //==비즈니스 로직==//
    // 객체지향적으로 생각하면, 데이터를 가지고 있는 쪽에 비즈니스 메서드가 있는 편이 좋다. (모듈의 응집도 향상)
    /*

    * stock 증가
    * */
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    /*
    * stock 감소
    * */
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if (restStock < 0) {
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = restStock;
    }

    //이런 식으로 엔티티의 필드값을 변경해야할 일이 있으면, 외부에서 계산해서 setter를 이용해서 주입하는 것이 아니라,
    // 엔티티 안에서 핵심 비지니스 메서드를 가지고 변경해야 한다.

}
