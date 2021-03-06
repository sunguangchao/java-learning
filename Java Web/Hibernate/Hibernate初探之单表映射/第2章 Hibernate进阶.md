hibernate.cfg.xml常用配置
------------

| 属性名字                     | 含义                                       |
| ------------------------ | ---------------------------------------- |
| hibernate.show_sql       | 是否把Hibernate运行时的SQL语句输出到控制台，编码阶段便于测试     |
| hibernate.foramt_sql     | 输出到控制台的SQL是否进行排版，便于阅读                    |
| hbm2ddl.auto             | 可以帮助java代码生成数据库脚本，进而生成具体的表结构。`create|update|create-drop|validate` |
| hibernate.default_schema | 默认的数据库                                   |
| hibernate.dialect        | 配置Hibernate数据库方言，Hibernate可针对特殊的数据库进行优化  |

session简介
------------
hibernate执行流程：
![](http://o90jubpdi.bkt.clouddn.com/hibernate%E6%89%A7%E8%A1%8C%E6%B5%81%E7%A8%8B.png)

不建议直接使用jdbc的connection操作数据库，而是通过使用session操作数据库,session可以理解为是操作数据库的对象

session与connection，是多对一关系，每个session都有一个与之对应的connection，一个connection不同时刻可以供多个session使用。

把对象保存在关系数据库中需要调用session的各种方法，如：save(),update(),deleta(),createQuery()等。

transcation简介
------------
hibernate对数据的操作都是封装在事务中，并且默认是非自动提交的方式。所以用session保存对象时，如果不开启事务，并且手工提交事务，对象不会真正保存在数据库中。

如果你想让hibernate像jdbc那样自动提交事务，必须调用session对象的doWork()方法，获得jdbc的connection后，设置其为自动提交事务模式。（注意：通常并不推荐这样做）




session详解
----------
如何获取session对象？

* openSession
* getCurrentSession

如果使用getCurrentSession需要在hibernate.cfg.xml文件中进行配置：
如果是本地事务(jdbc事务)：  
`<property name="hibernate.current_session_context_class">thread</property>`  
如果是全局事务(jta事务)  
`<property name="hibernate.current_session_context_class">jta</property>`



```java
@Test 
public void testOpenSession(){
	Configuration config = new Configuration().configure();//获得配置对象
	//获得服务注册对象
	ServiceRegistry serviceRegistry = new ServiceRegistry().applySettings(config.getProperties()).buildServiceRegistry();
	//获得sessionFactory对象
	SessionFactory sessionFactory = config.buildSessionFactory(serviceRegistry);
	//获得session对象
	Session session = sessionFactory.openSession();
	if(session!=null)
	{
		System.out.println("session创建成功");
	}
}
```



`openSession`与`getCurrentSession`的区别

1. getCurrentSession在事务提交或者回滚之后会自动关闭，而openSession需要手动关闭。如果使用openSession而没有手动关闭，多次之后会导致连接池溢出。
2. openSession每次创建新的session对象，getCurrentSession使用现有的session对象。

