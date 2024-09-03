package cn.gofree.lingxi.eventbridge.config;

import cn.gofree.lingxi.eventbridge.enums.CommonConstants;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.StringValueResolver;

@Configuration
public class PropertiesResolveService implements EmbeddedValueResolverAware, PriorityOrdered {

    private StringValueResolver stringValueResolver;

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        stringValueResolver = resolver;
    }

    public String getPropertiesValue(String name) {
        if (!name.contains(CommonConstants.V_PREFIX)) {
            name = CommonConstants.V_PREFIX + name + CommonConstants.V_POSTFIX;
        }
        return stringValueResolver.resolveStringValue(name);
    }

    @Override
    public int getOrder() {
        return PriorityOrdered.HIGHEST_PRECEDENCE;
    }
}
