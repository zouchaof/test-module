package com.logfilter.dubbo;

import com.alibaba.dubbo.rpc.*;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvokerTimeFilter implements Filter {

    private Logger logger = LoggerFactory.getLogger("logDebug");

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String name = invoker.getInterface().getName();
        String method = invocation.getMethodName();
        StringBuilder stringBuilder = new StringBuilder("[");
        for(Object obj:invocation.getArguments()){
            if(obj == null){
                continue;
            }
            stringBuilder.append(obj.getClass().getName()).append("=").append(JSONObject.toJSONString(obj)).append(",");
        }
        stringBuilder.append("]");
        logger.info("========invoke dubbo interface:{},method:{} params:{} start=======================",name,method,stringBuilder.toString());
        long start = System.currentTimeMillis();
        Result result = invoker.invoke(invocation);
        long end = System.currentTimeMillis();
        logger.info("========invoke dubbo interface:{},method:{} end,use {}ms,resp:{}==============",name,method,end-start,JSONObject.toJSONString(result.getValue()));
        return result;
    }
}
