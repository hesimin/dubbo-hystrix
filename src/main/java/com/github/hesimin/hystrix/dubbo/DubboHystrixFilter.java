package com.github.hesimin.hystrix.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;

/**
 * @author hesimin 2017-03-19
 */
@Activate(group = Constants.CONSUMER)
public class DubboHystrixFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        DubboHystrixCommand command = new DubboHystrixCommand(invoker, invocation);
        return command.execute();
    }

}
