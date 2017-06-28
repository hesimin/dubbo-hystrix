# dubbo-hystrix

##一、使用

引入jar

##二、hystrix-dashboard

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