# 什么是drools
Drools是为Java量身定制的基于Charles  Forgy的RETE算法的规则引擎的实现。具有了OO接口的RETE,使得商业规则有了更自然的表达。

### 应用场景
- 存在一个非常复杂的场景，难以完全定义，但问题确定的场景
- 没有已知或定义明确的算法解决方案
- 有不稳定需求，需要经常更新
- 需要快速做出决策，通常是基于部分数据量
- 输入和输出的参数不变，即：规则文件接收固定的参数，产生固定的输出。比如：根据个人信息计算可贷款金额，输入参数是个人信息（年龄，学历等等变化动态信息），规则各类条件权重值，输出可贷款金额；
- 输入和输出的POJO不变，即：规则文件接收固定类型的POJP，产生固定类型的POJO。
- 总之，复杂的场景,简单的规则

#### 特点

- 类似策略模式加强型（自定LHS,以及执行结果）
- 复杂规则不适用
- 事务问题
- 执行速度快

## 什么是Rete算法

### rete概述
Rete算法是一种前向规则快速匹配算法，其匹配速度与规则数目无关。Rete是拉丁文，对应英文是net，也就是网络。Rete算法通过形成一个rete网络进行模式匹配，利用基于规则的系统的两个特征，即时间冗余性（Temporal redundancy）和结构相似性（structural similarity），提高系统模式匹配效率。

### 相关概念
#### 事实（fact）
事实：对象之间及对象属性之间的多元关系。为简单起见，事实用一个三元组来表示：（identifier ^attribute  value），例如如下事实：

```
w1:(B1  ^ on B2)     w6:(B2  ^color blue)
w2:(B1  ^ on B3)     w7:(B3  ^left-of B4)
w3:(B1  ^ color red)   w8:(B3  ^on table)
w4:(B2  ^on table)    w9:(B3  ^color red)
w5:(B2  ^left-of B3)
```
#### 规则（rule）
由条件和结论构成的推理语句，当存在事实满足条件时，相应结论被激活。一条规则的一般形式如下：

```
(name-of-this-production
LHS /*one or more conditions*/
-->
RHS /*one or more actions*/
)
其中LHS为条件部分，RHS为结论部分。
下面为一条规则的例子：
(find-stack-of-two-blocks-to-the-left-of-a-red-block
(^on)
(^left-of)
(^color red)
-->
...RHS...
)
```

#### 模式（patten）
模式：规则的IF部分，已知事实的泛化形式，未实例化的多元关系。

```
(^on)
(^left-of)
(^color red)
```
### 模式匹配的一般算法
规则主要由两部分组成：条件和结论，条件部分也称为左端（记为LHS, left-hand side），结论部分也称为右端（记为RHS, right-hand side）。为分析方便，假设系统中有N条规则，每个规则的条件部分平均有P个模式，工作内存中有M个事实，事实可以理解为需要处理的数据对象。
规则匹配，就是对每一个规则r, 判断当前的事实o是否使LHS(r)=True，如果是，就把规则r的实例r(o)加到冲突集当中。所谓规则r的实例就是用数据对象o的值代替规则r的相应参数，即绑定了数据对象o的规则r。
规则匹配的一般算法：
1) 从N条规则中取出一条r；
2) 从M个事实中取出P个事实的一个组合c；
3) 用c测试LHS(r)，如果LHS(r（c）)=True，将RHS(r（c）)加入冲突集中；
4) 取出下一个组合c，goto 3；
5) 取出下一条规则r，goto 2；

### RETE算法
Rete算法的编译结果是规则集对应的Rete网络,如下图。Rete网络是一个事实可以在其中流动的图。Rete网络的节点可以分为四类：根节点（root）、类型节点（typenode）、alpha节点、beta节点。其中，根结点是一个虚拟节点，是构建rete网络的入口。类型节点中存储事实的各种类型，各个事实从对应的类型节点进入rete网络。

#### 建立rete网络

Rete网络的编译算法如下：
1) 创建根；
2) 加入规则1(Alpha节点从1开始，Beta节点从2开始)；
a. 取出模式1，检查模式中的参数类型，如果是新类型，则加入一个类型节点；
b. 检查模式1对应的Alpha节点是否已存在，如果存在则记录下节点位置，如果没有则将模式1作为一个Alpha节点加入到网络中，同时根据Alpha节点的模式建立Alpha内存表；
c. 重复b直到所有的模式处理完毕；
d. 组合Beta节点，按照如下方式：
　　　Beta(2)左输入节点为Alpha(1)，右输入节点为Alpha(2)
　　　Beta(i)左输入节点为Beta(i-1)，右输入节点为Alpha(i)  i>2
  并将两个父节点的内存表内联成为自己的内存表；
e. 重复d直到所有的Beta节点处理完毕；
f. 将动作（Then部分）封装成叶节点（Action节点）作为Beta(n)的输出节点；
3) 重复2)直到所有规则处理完毕；
可以把rete算法类比到关系型数据库操作。
把事实集合看作一个关系，每条规则看作一个查询，将每个事实绑定到每个模式上的操作看作一个Select操作，记一条规则为P，规则中的模式为c1,c2,…,ci, Select操作的结果记为r(ci),则规则P的匹配即为r(c1)◇r(c2)◇…◇(rci)。其中◇表示关系的连接（Join）操作。

#### 使用rete网络进行匹配
使用一个rete的过程：
1) 对于每个事实，通过select 操作进行过滤，使事实沿着rete网达到合适的alpha节点。
2) 对于收到的每一个事实的alpha节点，用Project(投影操作)将那些适当的变量绑定分离出来。使各个新的变量绑定集沿rete网到达适当的bete节点。
3) 对于收到新的变量绑定的beta节点，使用Project操作产生新的绑定集，使这些新的变量绑定沿rete网络至下一个beta节点以至最后的Project。
4) 对于每条规则，用project操作将结论实例化所需的绑定分离出来。

下面为的图示显示了连接（Join）操作和投影（Project）的执行过程。


#### Rete算法的特点
Rete算法有两个特点使其优于传统的模式匹配算法。
1、状态保存
事实集合中的每次变化，其匹配后的状态都被保存再alpha和beta节点中。在下一次事实集合发生变化时，绝大多数的结果都不需要变化，rete算法通过保存操作过程中的状态，避免了大量的重复计算。Rete算法主要是为那些事实集合变化不大的系统设计的，当每次事实集合的变化非常剧烈时，rete的状态保存算法效果并不理想。
2、节点共享
另一个特点就是不同规则之间含有相同的模式，从而可以共享同一个节点。Rete网络的各个部分包含各种不同的节点共享。


## 简单搭建一个 springboot +Drools 环境

### 样列参考地址：


### 初始化springboot
- 从 https://start.spring.io/ 快速搭建一个springboot
- 在pom文件中添加如下依赖：

```
	    <dependency>
			<groupId>org.codehaus.mojo</groupId>
			<artifactId>exec-maven-plugin</artifactId>
		</dependency>
		<dependency>
			<groupId>org.drools</groupId>
			<artifactId>drools-core</artifactId>
			<version>7.0.0.Final</version>
		</dependency>
		<dependency>
			<groupId>org.drools</groupId>
			<artifactId>drools-compiler</artifactId>
			<version>7.0.0.Final</version>
		</dependency>
		<dependency>
			<groupId>org.drools</groupId>
			<artifactId>drools-decisiontables</artifactId>
			<version>7.0.0.Final</version>
		</dependency>
		<dependency>
			<groupId>org.drools</groupId>
			<artifactId>drools-templates</artifactId>
			<version>7.0.0.Final</version>
		</dependency>

		<dependency>
			<groupId>org.kie</groupId>
			<artifactId>kie-api</artifactId>
			<version>7.0.0.Final</version>
		</dependency>


		在build中加入：
	<build>
		<plugins>
			<plugin>
				<artifactId>maven-compiler-plugin</artifactId>
			</plugin>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
			<plugin>
				<groupId>org.codehaus.mojo</groupId>
				<artifactId>exec-maven-plugin</artifactId>
				<configuration>
					<executable>java</executable>
					<arguments>
					    <!-- 入口类-->
						<argument>com.test.sd.SdApplication</argument>
					</arguments>
				</configuration>
			</plugin>
		</plugins>
	</build>
```

### 添加drools相关文件

- 在resources 里创建2个文件夹 一个是 META-INF  一个是 rules
- 在META-INF内 创建 kmodule.xml 文件 文件内容：

```
<kmodule xmlns="http://jboss.org/kie/6.0.0/kmodule">
    <kbase name="rules" packages="com.test.sd">
        <ksession name="test"/>
    </kbase>
</kmodule>
```
- 在rules文件夹内创建 一个demo rule文件，为 test.drl 内容如下：

```
package com.test.sd    // package 是对于规则文件中规则的管理只限于逻辑上的管理，而不管其在物理上的位置如何。
import com.test.bean.Message //需要引入的Object
dialect  "mvel"  //定义语言类型，不定义默认则为java

rule "Hello World"
    dialect "mvel"
    when
        m : Message(status.equals(Message.HELLO), message : message )
    then
        System.out.println( message);
    modify ( m ) { message = "Goodbye cruel world",status = Message.GOODBYE };
end

rule "Good Bye"
    dialect "java"
    when
       Message( status == Message.GOODBYE, message : message )
    then
        System.out.println( message );
end
```
### 创建对应java 文件
bean:

```

package com.test.bean;

public class Message {
    public static final Integer HELLO = 0;
    public static final Integer GOODBYE = 1;

    private String message;

    private Integer status;

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}




```

intf:
```

package com.test.service;


public interface Dservice {
     String fireRule(String value);
     String execRules(String session, String value);
}

```
impl:

```
package com.test.service;
import com.test.bean.Message;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import org.springframework.stereotype.Service;

@Service("drservice")
public class DroolsService  implements Dservice{

    private final   KieContainer kContainer =KieServices.Factory.get().getKieClasspathContainer();



    @Override
    public String fireRule(String value) {
        // load up the knowledge base
        KieServices ks = KieServices.Factory.get();
        KieContainer kContainer = ks.getKieClasspathContainer();
        KieSession kSession = kContainer.newKieSession("test");

        // go !
        Message message = new Message();
        message.setMessage("Hello World");
        message.setMessage(value);
        message.setStatus(Message.HELLO);
        kSession.insert(message);//插入
        kSession.fireAllRules();//执行规则
        kSession.dispose();
        return message.getMessage();
    }

    @Override
    public String execRules(String session, String value) {
        KieSession kSession = kContainer.newKieSession(session);
        Message message = new Message();
        message.setMessage(value);
        message.setStatus(Message.HELLO);
        kSession.insert(message);//插入
        kSession.fireAllRules();//执行规则
        kSession.dispose();
        return message.getMessage();
    }
}
```
入口类如下：
```
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
		System.out.println(value==null);
		//http://127.0.0.1:8080/test?value=Hello%20World
//		return dservice1.fireRule(value);

		return dservice1.execRules("test",value);
	}

	@RequestMapping("/hellow")
	@ResponseBody
	private String hello(){
		return "Hello aaa";
	}

	public static void main(String[] args) {
		SpringApplication.run(SdApplication.class, args);
	}
}






```
### 测试demo

- 启动项目，在浏览器输入 //http://127.0.0.1:8080/test?value=Hello%20World
- 测试结果是否在页面输出 Goodbye cruel world

### 错误
- 启动报错 在pom里添加如下依赖
```

		<!-- https://mvnrepository.com/artifact/org.eclipse.jdt/core -->
		<dependency>
			<groupId>org.eclipse.jdt</groupId>
			<artifactId>core</artifactId>
			<version>3.3.0-v_771</version>
		</dependency>


        <!-- 1.4.10 涉及安全问题，目前不推荐使用 -->
		<dependency>
			<groupId>com.thoughtworks.xstream</groupId>
			<artifactId>xstream</artifactId>
			<version>1.4.9</version>
		</dependency>

```
- clean项目 重启IDE




## drools规则解析

### Drools版本信息
目前Drools发布的最新版本为7.0.0.Final，其他版本正在研发过程中。官方表示后续版本会加快迭代速度。本系列也是基于此版本进行讲解。

从Drools6.x到7版本发生重大的变化项：

- @PropertyReactive不需要再配置，在Drools7中作为默认配置项。
- 同时向下兼容。
- Drools6版本中执行sum方法计算结果数据类型问题修正。
- 重命名TimedRuleExecutionOption。
- 重命名和统一配置文件。

Drools7新功能：
1. 支持多线程执行规则引擎，默认为开启，处于试验阶段。
1. OOPath改进，处于试验阶段。
1. OOPath Maven 插件支持。
1. 事件的软过期。
1. 规则单元RuleUnit。

#### 官方地址
- 官网地址：http://www.drools.org/
- 官方最新文档：https://docs.jboss.org/drools/release/7.0.0.Final/drools-docs/html_single/index.html

#### jar包


jar包名称 | 作用
---|---
knowledge-api.jar | 这提供了接口和工厂。它还有助于清楚地显示什么是用户API，什么是引擎API
knowledge-internal-api.jar | 这提供了内部接口和工厂
drools-core.jar | 他是核心引擎，运行时组件。包含RETE引擎和LEAPS引擎。如果您正在预编译规则(并通过包或RuleBase对象进行部署)，这是惟一的运行时依赖性。
drools-compiler.jar | 它包含编译器/构建器组件，以获取规则源，并构建可执行的规则库。这通常是应用程序的运行时依赖性，但如果您预先编译了规则，则不必如此。这取决于drools-core。
drools-jsr94.jar | 这是jsr- 4兼容实现，这实质上是drools编译器组件的一个层。注意，由于jsr-94规范的性质，并不是所有特性都很容易通过这个接口公开。在某些情况下，直接使用Drools API比较容易，但是在某些环境中，jsr-94是强制执行的。
drools-decisiontables.jar | 这是决策表的编译器组件，它使用drools编译器组件。这支持excel和CSV输入格式。


### kmodule.xml文件
- Kmodule中可以包含一个到多个kbase，分别对应drl的规则文件。
- Kbase需要一个唯一的name，可以取任意字符串。
- packages为drl文件所在resource目录下的路径。注意区分drl文件中的package与此处的package不一定相同。多个包用逗号分隔。默认情况下会扫描resources目录下所有（包含子目录）规则文件。
- 不同的包执行不同的rules文件
- kbase的default属性，标示当前KieBase是不是默认的，如果是默认的则不用名称就可以查找到该KieBase，但每个module最多只能有一个默认KieBase。
- kbase下面可以有一个或多个ksession，ksession的name属性必须设置，且必须唯一。

```
<kmodule xmlns="http://jboss.org/kie/6.0.0/kmodule">
    <kbase name="rules1" packages="packages1">
        <ksession name="test1"/>
    </kbase>
    <kbase name="rules2" packages="packages2" default="true">
        <ksession name="test2"/>
    </kbase>
</kmodule>
```


### drl 文件

#### 详解
- package com.sample   //包名，不可以与关键字冲突。一个包通过名称空间描绘，这样很好的保持了一组规则的独立性。
- import com.sample.DroolsTest.Message;//本文件需要导入的类 。    标记就像java中的含义一样。对于任何要用在规则中的对象，你需要指定完整的路径和类型名。Drools从同名的java包中自动导入类。
- global java.util.List myGlobalList;//全局变量。如果多个包定义了同样名称的全局变量，它们必须使用同样的类型，并且全部指向同一个全局值。全部变量通常用来返回数据，获得提供数据或服务给规则使用。为了使用全局变量，你必须：
在规则文件中声明全局变量并使用它，例子：

```
global java.util.List myGlobalList;
rule "Using a global"
when
    eval( true )
then
    myGlobalList.add( "Hello World" );
end
在working memory上设置全局变量的值。最好是在将fact插入working memory之前设置完所有全局变量，如：
List list = new ArrayList();
WorkingMemory wm = rulebase.newStatefulSession();
wm.setGlobal( "myGlobalList", list );
```

- //定义函数体。相对于正常的java类，函数是在你的规则代码中放置语言代码的方法。它们不可能做任何超过你可以在帮助类（在java中定义，被设置入规则的Working Memory中的类）中做到的事情。使用函数的优点是可以将逻辑保存在一个地方，并且你可以在需要的时候改变函数（这样做各有优缺点）。函数最大的用处是被规则的推论（then）部分中的行为所调用，特别是当一个行为操作需要反复被调用时（将公用代码抽取出来成为一个函数）。

```
function String hello(String name) {
    return "Hello "+name+"!";
}
```

- rule "myRule"       //名称可以在“”下取任何名字。
#####  属性列表
- no-loop true  //执行一次后，是否能被再次激活  。
- salience 100  //优先级别 。优先级数字高的规则会比优先级低的规则先执行。
- agenda-group String MAIN 只有在具有焦点的agenda group中的规则才能够激发。
- auto-focus Boolean false 如果该规则符合激活条件，则该规则所在agenda-group自动获得焦点，允许规则激发。
- activation-group String N/A 在同名activation-group中的规则将以互斥的方式激发
- dialect String "java" or "mvel" 指定在LHS代码表达式或RHS代码块中使用的语言。
- date-effective String, 包含日期/时间定义 N/A 规则只能在date-effective指定的日期和时间之后激活。
- date-exptires String, 包含日期/时间定义 N/A 如果当前时间在date-expires指定的时间之后，规则不能激活。
- duration long N/A 指出规则将在指定的一段时间后激发，如果那个时候规则的激活条件还是处于true的情况下。
- 例子：

```

```


#####  LHS (when) 条件元素

```
为了能够引用匹配的对象，使用一个模式绑定变量如‘$c’。变量的前缀使用的$是可选的，但是在复杂的规则中它会很方便用来区别变量与字段的不同。
$c : Cheese( type == "stilton", price < 10, age == "mature" )
&& 和|| 约束连接符
Cheese( type == "stilton" && price < 10, age == "mature" )
Cheese( type == "stilton" || price < 10, age == "mature" )
第一个有两个约束而第二个组有一个约束，可以通过圆括号来改变求值的顺序。
单值约束
Matches 操作
Cheese( type matches "(Buffalo)?\S*Mozerella" )
Cheese( type not matches "(Buffulo)?\S*Mozerella" )
Contains 操作
CheeseCounter( cheeses contains "stilton" )
CheeseCounter( cheeses not contains "cheddar" )
memberof操作
CheeseCounter( cheese memberof $matureCheeses )
CheeseCounter( cheese not memberof $matureCheeses )
字符串约束
字符串约束是最简单的约束格式，将字段与指定的字符串求值：数值，日期，string或者boolean。
Cheese( quantity == 5 )
Cheese( bestBefore < "27-Oct-2007" )
Cheese( type == "stilton" )
Cheese( smelly == true )
绑定变量约束
变量可以绑定到Fact和它们的字段，然后在后面的字段约束中使用。绑定变量被称为声明。有效的操作符由被约束的字段类型决定；在那里会进行强制转换。绑定变量约束使用'=='操作符，因为能够使用hash索引，因此提供非常快的执行速度。
Person( likes : favouriteCheese )
Cheese( type == likes )
返回值约束
返回值约束可以使用任何有效的Java元数据类型或对象。要避免使用任何Drools关键字作为声明标识。在返回值约束中使用的函数必须返回静态常量（time constant）结果。之前声明的绑定可以用在表达式中。
Person( girlAge : age, sex == "F" )
Person( age == ( girlAge + 2) ), sex == 'M' )
复合值约束
复合值约束用在可能有多个允许值的时候，当前只支持'in' 和'not in'两个操作。这些操作使用圆括号包含用逗号分开的值的列表，它可以是变量，字符串，返回值或限定标识符。'in' 和'not in'运算式实际上被语法分析器重写成多个!= and ==组成的多重约束。
Person( $cheese : favouriteCheese )
Cheese( type in ( "stilton", "cheddar", $cheese )
多重约束
多重约束允许你对一个字段通过使用'&&' 或者'||'约束连接符进行多个约束条件的判断。允许使用圆括号分组，它会让这种约束看起来更自然。
Person( age ( (> 30 && < 40) || (> 20 && < 25) ) )
Person( age > 30 && < 40 || location == "london" )
内联的Eval约束
eval约束可以使用任何有效的语言表达式，只要它最终能被求值为boolean元数据类型。表达式必须是静态常量（time constant）。任何在当前模式之前定义的变量都可以使用，自动代入（autovivification）机制用来自动建立字段绑定变量。当构建器发现标识不是当前定义的变量名是，它将尝试将它作为对象的字段来访问，这种情况下，构建器自动在inline-eval中建立该字段的同名变量。
Person( girlAge : age, sex = "F" )
Person( eval( girlAge == boyAge + 2 ), sex = 'M' )
```
##### RHS (then) 执行操作

```
这部分应当包含一系列需要执行的操作。规则的RHS部分应该保持简短的，这保持它是声明性和可读性的。
如果你发现你需要在RHS中使用命令式或and/or条件代码，那你可能需要将规则拆分为多个规则。RHS的主要目的是插入，删除修改working memory数据。
"update(object, handle);" 将告诉引擎对象已经改变（已经被绑定到LHS中的那一个），并且规则需要重新检查。
"insert(new Something());" 将在working memory中放置一个你新建的对象。
"insertLogical(new Something());" 与insert类似，但是当没有更多的fact支持当前激发规则的真值状态时，对象自动删除。
"retract(handle);" removes an object from working memory.
```

##### Query

```
查询中仅仅包含规则LHS部分的结构（不用指定when或then）。它提供了查询working memory 中符合约束条件的对象的一个简单办法。
query "people over the age of 30"
    person : Person( age > 30 )
end
通过在返回的查询结果(QueryResults)上进行标准的for循环遍历，每一行将返回一个QueryResult，该对象可以用来存取组元中的每一个Column。这些Column可以通过声明的名称或索引位置存取。
QueryResults results = workingMemory.getQueryResults( "people over the age of 30" );
for ( Iterator it = results.iterator; it.hasNext(); ) {
    QueryResult result = ( QueryResult ) it.next();
    Person person = ( Person ) result.get( "person" );
    }
```
