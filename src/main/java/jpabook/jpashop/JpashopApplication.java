package jpabook.jpashop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // 메인 클래스가 위치한 패키지  및 그 하위에 모든 컴포넌트 스캔 대상을 스캔하여 빈에 자동등록시킴.
public class JpashopApplication {

	public static void main(String[] args) {

		SpringApplication.run(JpashopApplication.class, args);
	}

}
