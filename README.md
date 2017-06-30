# dubbo-hystrix

熔断的目的：隔离有问题的服务，防止问题扩散导致整个服务不可用。

## 一、使用

引入jar  (com.github.hesimin.hystrix.dubbo.DubboHystrixCommand 里面的参数需要根据情况进行调整)

## 二、hystrix-dashboard

开放 http 接口，向 hystrix-dashboard 提供数据(dubbo 非web启动不可用？)

web.xml中配置servlet

    <servlet>  
        <description></description>  
        <display-name>HystrixMetricsStreamServlet</display-name>  
        <servlet-name>HystrixMetricsStreamServlet</servlet-name>  
        <servlet-class>com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet</servlet-class>  
    </servlet>  
  
    <servlet-mapping>  
        <servlet-name>HystrixMetricsStreamServlet</servlet-name>  
        <url-pattern>/hystrix.stream</url-pattern>  
    </servlet-mapping>