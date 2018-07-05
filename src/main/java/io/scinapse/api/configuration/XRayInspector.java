package io.scinapse.api.configuration;

import com.amazonaws.xray.entities.Subsegment;
import com.amazonaws.xray.spring.aop.AbstractXRayInterceptor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class XRayInspector extends AbstractXRayInterceptor {

    @Pointcut("@within(com.amazonaws.xray.spring.aop.XRayEnabled)")
    @Override
    protected void xrayEnabledClasses() {}

    @Override
    protected Map<String, Map<String, Object>> generateMetadata(ProceedingJoinPoint pjp, Subsegment subsegment) {
        final Map<String, Map<String, Object>> metadata = new HashMap<>();
        final Map<String, Object> classInfo = new HashMap<>();
        classInfo.put("Class", pjp.getSignature().getDeclaringTypeName());
        metadata.put("ClassInfo", classInfo);
        return metadata;
    }
}
