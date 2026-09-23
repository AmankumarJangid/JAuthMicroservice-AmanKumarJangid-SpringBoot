package com.gamegrind.dev.AuthApplication;

import com.gamegrind.dev.AuthApplication.services.OtpService;
import com.gamegrind.dev.AuthApplication.services.impl.OtpServiceImpl;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Scanner;

@SpringBootTest
@RequiredArgsConstructor
class AuthApplicationTests {

//	private static OtpServiceImpl otp;
	@Test
	void contextLoads() {

	}

//	static void main() {
//		Scanner sc = new Scanner(System.in);
//		boolean runLoop = true;
//		while( runLoop ){
//			System.out.println(otp.generateRandomOtp(6));
//
//			System.out.print("Generate again : \"yes\" or \"no\"");
//			String shouldRun = sc.next();
//			runLoop = shouldRun.trim().equalsIgnoreCase("yes");
//		}
//
//	}

}
