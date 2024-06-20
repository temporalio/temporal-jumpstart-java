/*
 * MIT License
 *
 * Copyright (c) 2024 temporal.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.temporal.jumpstart.workflows.domain.orchestrations;

import io.temporal.failure.ApplicationFailure;
import io.temporal.jumpstart.workflows.messages.orchestrations.OnboardEntityRequest;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import com.google.common.base.Strings;

@WorkflowImpl(taskQueues = "onboardings")
public class OnboardEntityImpl implements OnboardEntity {

  @Override
  public String executeAsync(OnboardEntityRequest args) {

    assertValidRequest(args);

    // .NET code has: await Workflow.DelayAsync(10000);
    // Equivalent is CompletableFuture or the EA-Async library
    // Not important at this point
    Workflow.sleep(1000);
    return null;
  }

  private void assertValidRequest(OnboardEntityRequest args) {
    if (Strings.isNullOrEmpty(args.getId()) || Strings.isNullOrEmpty(args.getValue())) {
      /*
       * Temporal is not prescriptive about the strategy you choose for indicating
       * failures in your Workflows.
       * 
       * We throw an ApplicationFailureException here which would ultimately result in
       * a `WorkflowFailedException`.
       * This is a common way to fail a Workflow which will never succeed due to bad
       * arguments or some other invariant.
       * 
       * It is common to use ApplicationFailure for business failures, but these
       * should be considered distinct from an intermittent failure such as
       * a bug in the code or some dependency which is temporarily unavailable.
       * Temporal can often recover from these kinds of intermittent failures
       * with a redeployment, downstream service correction, etc. These intermittent
       * failures would typically result in an Exception NOT descended from
       * TemporalFailure and would therefore NOT fail the Workflow Execution.
       * 
       * If you have explicit business metrics setup to monitor failed Workflows, you
       * could alternatively return a "Status" result with the business failure
       * and allow the Workflow Execution to "Complete" without failure.
       * 
       * Note that `WorkflowFailedException` will count towards the `workflow_failed`
       * SDK Metric (https://docs.temporal.io/references/sdk-metrics#workflow_failed).
       */
      throw ApplicationFailure.newNonRetryableFailure("OnboardEntity.Id and OnboardEntity.Value is required",
          null, null);
    }
  }
}
