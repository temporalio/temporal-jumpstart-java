package io.temporal.app.domain.orchestrations;

import io.temporal.activity.ActivityOptions;
import io.temporal.app.domain.messages.orchestrations.StartMyWorkflowRequest;
import io.temporal.app.domain.products.ProductHandlers;
import io.temporal.workflow.*;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.*;

public class MyWorkflow2Impl implements MyWorkflow{
    private Logger logger = Workflow.getLogger(MyWorkflow2Impl.class);
    private final ProductHandlers productHandlers =
            Workflow.newActivityStub(ProductHandlers.class,
                    ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(5)).build());
    @Override
    public void execute(StartMyWorkflowRequest args) {

        var partialFulfillment = false;
        var completedProducts = new ArrayList<String>();
        Map<String, String> productSpecs = new HashMap<String, String>();
        productSpecs.put("p1", "pspec1");
        productSpecs.put("p2", "pspec2");
        productSpecs.put("p3", "pspec3");

        List<Promise<>> results = new ArrayList<>();
        CancellationScope cancellationScope =
                Workflow.newCancellationScope(
                        () -> {
                            for(Map.Entry<String,String> entry: productSpecs.entrySet()) {
                                var p = Async.procedure(productHandlers::fulfillProduct).
                                        thenApply(r->{
                                            logger.info("Fulfilling product " + entry.getKey());
                                            completedProducts.add(entry.getKey());
                                            return r;
                                        });
                                results.add(p);
                            }
                        });
        var conditionMet = Workflow.await(Duration.ofSeconds(100), ()->completedProducts.size() == productSpecs.size());
        if(!conditionMet){
            // cancel all the activities that have not called back
            cancellationScope.cancel();
        }
        for(Promise p: results){
            if(p.getFailure() != null ) {
                //
                partialFulfillment = true;
            }
            else {
                p.get();
            }
        }
    }
}
