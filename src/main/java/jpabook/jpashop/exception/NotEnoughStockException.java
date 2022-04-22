package jpabook.jpashop.exception;

/*
* Exception 작성법: RuntimeException을 상속받고, 메서드 오버라이딩 해준다.
* */
public class NotEnoughStockException extends RuntimeException{
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
