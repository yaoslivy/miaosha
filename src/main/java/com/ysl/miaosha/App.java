package com.ysl.miaosha;


import com.ysl.miaosha.dao.UserDOMapper;
import com.ysl.miaosha.dataobject.UserDO;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
//通过scanBasePackages属性指定包扫描的路径（如不指定，会默认扫描主程序类所在的包路径以及子包下的类）
@SpringBootApplication(scanBasePackages = {"com.ysl.miaosha"})//三个注解的总和@Configuration： 用于定义一个配置类;@EnableAutoConfiguration ：Spring Boot会自动根据你jar包的依赖来自动配置项目;@ComponentScan： 告诉Spring 哪个packages 的用注解标识的类 会被spring自动扫描并且装入bean容器。
@RestController//是@ResponseBody和@Controller的组合注解
@MapperScan("com.ysl.miaosha.dao") //扫描指定位置的所有mapper类作为Mapper映射文件
public class App
{
    @Autowired //使用byType方式的;根据属性类型在容器中寻找bean类
    private UserDOMapper userDOMapper;

    @RequestMapping("/")//配置url映射;响应的url=localhost:8080/
    public String home() {
        UserDO userDO = userDOMapper.selectByPrimaryKey(1);
        if (userDO == null) {
            return "用户对象不存在";
        } else {
            return userDO.getName();
        }
    }
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        SpringApplication.run(App.class, args);
    }
}
