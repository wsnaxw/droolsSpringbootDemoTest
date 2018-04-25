package com.test.sd;

import com.test.service.DroolsService;
import com.test.service.Dservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@SpringBootApplication
@ComponentScan("com.test.*")
public class SdApplication {

	@Autowired
	private Dservice dservice1;

	private DroolsService dservice = new DroolsService();

	@RequestMapping("/test")
	@ResponseBody
	private String testMessage(@RequestParam("value") String value){
		//http://127.0.0.1:8081/test?value=Hello%20World
		return dservice.execRules1();
//		return dservice1.execRules("test",value);
	}


	@RequestMapping("/test1")
	@ResponseBody
	private String testMessage2(){
		//http://127.0.0.1:8081/test1
		return dservice.execRules2();

	}


	public static void main(String[] args) {
		SpringApplication.run(SdApplication.class, args);
	}
}
