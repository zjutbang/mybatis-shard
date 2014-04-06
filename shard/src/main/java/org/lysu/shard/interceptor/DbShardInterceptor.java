package org.lysu.shard.interceptor;

import org.apache.ibatis.plugin.Invocation;
import org.lysu.shard.context.ExecuteInfoContext;
import org.lysu.shard.context.RouteDataSourceContext;
import org.lysu.shard.execute.DataSourceInfo;
import org.lysu.shard.execute.ExecuteInfo;
import org.lysu.shard.locator.Locator;
import org.lysu.shard.locator.Locators;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author lysu created on 14-4-6 下午3:57
 * @version $Id$
 */
enum DbShardInterceptor {

    instance;

    public Object intercept(Invocation invocation) throws Throwable {

        String routingKey = routeKey();

        if (isNullOrEmpty(routingKey)) {
            return invocation.proceed();
        }

        try {
            RouteDataSourceContext.setRouteKey(routingKey);
            return invocation.proceed();
        } finally {
            RouteDataSourceContext.clearRouteKey();
        }

    }

    private String routeKey() {

        ExecuteInfo executeInfo = ExecuteInfoContext.getExecuteInfo();

        if (executeInfo == null) {
            return null;
        }

        DataSourceInfo dataSourceInfo = executeInfo.getDataSourceInfo();

        if (dataSourceInfo == null) {
            return null;
        }

        Locator locator = Locators.instance.takeLocator(dataSourceInfo.getRule());
        String dbSuffix = locator.locate(dataSourceInfo.getParams());

        if (isNullOrEmpty(dbSuffix)) {
            return null;
        }

        return dataSourceInfo.getDataSourceName() + "_" + dbSuffix;

    }

}
