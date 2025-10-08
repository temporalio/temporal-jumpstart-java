package io.temporal.inframanager.domain.io.temporal.inframanager.domain.interceptors;

import io.nexusrpc.handler.OperationContext;
import io.temporal.common.interceptors.ActivityInboundCallsInterceptor;
import io.temporal.common.interceptors.NexusOperationInboundCallsInterceptor;
import io.temporal.common.interceptors.WorkflowInboundCallsInterceptor;
import org.springframework.stereotype.Component;

@Component
public class NoopInterceptorImpl implements NoopInterceptor {
    @Override
    public WorkflowInboundCallsInterceptor interceptWorkflow(WorkflowInboundCallsInterceptor next) {
        return next;
    }

    @Override
    public ActivityInboundCallsInterceptor interceptActivity(ActivityInboundCallsInterceptor next) {
        return next;
    }

    @Override
    public NexusOperationInboundCallsInterceptor interceptNexusOperation(OperationContext context, NexusOperationInboundCallsInterceptor next) {
        return next;
    }
}
