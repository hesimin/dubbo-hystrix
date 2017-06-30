package com.github.hesimin.hystrix.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 这里的隔离指的是在消费方对有问题的服务发起调用时进行阻止，这样有问题的服务方就不会接受到该调用，以便进行恢复
 * <p>服务方service抛出的异常是被 dubbo 包在 Result.getException 里面的，不会触发熔断，只有超时、网络等原因导致 invoker.invoke(invocation) 抛出了异常才会进入熔断计数 </p>
 *
 * @author hesimin 2017-03-19
 */
public class DubboHystrixCommand extends HystrixCommand<Result> {
    private static       Logger logger                       = LoggerFactory.getLogger(DubboHystrixCommand.class);
    private static final int    DEFAULT_THREADPOOL_CORE_SIZE = 100;
    private Invoker<?> invoker;
    private Invocation invocation;

    // 默认错误超过50%且10秒内超过20个请求进行中断拦截.
    public DubboHystrixCommand(Invoker<?> invoker, Invocation invocation) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(invoker.getInterface().getName()))
                //依赖隔离的根本就是对相同CommandKey的依赖做隔离
                // invoker.getUrl().getHost()+invoker.getUrl().getPort() 把ip加上就做到对机器的隔离(使用HystrixThreadPoolKey？)（最好的方式应该是使用dubbo自身的降级功能，不对问题机器发起调用）
                .andCommandKey(HystrixCommandKey.Factory.asKey(String.format("%s_%d", invocation.getMethodName(),
                        invocation.getArguments() == null ? 0 : invocation.getArguments().length)))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withCircuitBreakerRequestVolumeThreshold(20)// 熔断触发的最小个数/10s
                        .withCircuitBreakerSleepWindowInMilliseconds(15000)// 熔断后的重试时间窗口:熔断开关打开后，在该时间窗口允许有一次重试，如果重试成功，则将重置Health采样统计并闭合熔断开关实现快速恢复，否则熔断开关还是打开状态，执行快速失败。
                        .withCircuitBreakerErrorThresholdPercentage(50)// 错误率达到50开启熔断保护
                        .withExecutionTimeoutEnabled(false))// 使用dubbo的超时，禁用这里的超时
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(getThreadPoolCoreSize(invoker.getUrl()))));//线程池数

        this.invoker = invoker;
        this.invocation = invocation;
    }

    /**
     * 获取线程池大小
     *
     * @param url
     * @return
     */
    private static int getThreadPoolCoreSize(URL url) {
        if (url != null) {
            int size = url.getParameter("ThreadPoolCoreSize", DEFAULT_THREADPOOL_CORE_SIZE);
            if (logger.isDebugEnabled()) {
                logger.debug("ThreadPoolCoreSize:" + size);
            }
            return size;
        }

        return DEFAULT_THREADPOOL_CORE_SIZE;
    }

    @Override
    protected Result run() throws Exception {
        return invoker.invoke(invocation);
    }

//  以下四种情况将触发getFallback调用：
// (1):run()方法抛出非HystrixBadRequestException异常。
//            (2):run()方法调用超时
// (3):熔断器开启拦截调用
//            (4):线程池/队列/信号量是否跑满
//    @Override
//    protected Result getFallback() {
//        return null;
//    }
}
