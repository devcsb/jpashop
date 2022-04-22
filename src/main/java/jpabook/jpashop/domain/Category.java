package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.*;
//@ManyToMany 는 편리한 것 같지만, 중간 테이블( CATEGORY_ITEM )에 컬럼을 추가할 수 없고, 세밀하게 쿼
//        리를 실행하기 어렵기 때문에 실무에서 사용하기에는 한계가 있다. 중간 엔티티( CategoryItem 를 만들고
//@ManyToOne , @OneToMany 로 매핑해서 사용하자.

@Entity
@Getter @Setter
public class Category {

    @Id @GeneratedValue
    @Column(name = "category_id")
    private Long id;

    private String name;

    @ManyToMany // RDDMS는 엔티티와 달리 곧바로 다대다 관계가 성립되지 않으므로 일대다, 다대일로 풀어내는 중간 테이블을 매핑해준다.
    @JoinTable(name = "category_item", // 중간 테이블 매핑
            joinColumns = @JoinColumn(name = "category_id"), // 현재 엔티티를 참조하는 외래키(중간테이블의 컬럼) 지정
            inverseJoinColumns = @JoinColumn(name = "item_id"))  // 반대방향 엔티티를 참조하는 외래키 지정
    private List<Item> items = new ArrayList<>();

    //계층형 구조 매핑. 대상이 자신 엔티티일 뿐, 연관관계 설정은 다른 엔티티와 맺을 때와 같다.
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id") //카테고리는 여러 부모가 가지는 하나의 속성이다. ManyToOne
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> child = new ArrayList<>(); //카테고리는 여러 자식을 가지고 있다. OneToMany.

    //== 연관관계 편의 메서드 ==//
    public void addChildCategory(Category child) {
        this.child.add(child);
        child.setParent(this);
    }
}


