package jpabook.jpashop.exception;

/*
* Exception 작성법: RuntimeException을 상속받고, 메서드 오버라이딩 해준다.
* */
public class NotEnoughStockException extends RuntimeException{ // Exception trace가 쭉 나오도록 하기 위해 메서드 오버라이딩 필요
    public NotEnoughStockException() {
        super();
    }

    public NotEnoughStockException(String message) {
        super(message);
    }

    public NotEnoughStockException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotEnoughStockException(Throwable cause) {
        super(cause);
    }


}
