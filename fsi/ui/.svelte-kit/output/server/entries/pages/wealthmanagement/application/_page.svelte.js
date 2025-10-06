import { c as create_ssr_component, j as each, e as escape, a as add_attribute, v as validate_component, m as missing_component } from "../../../../chunks/ssr.js";
const OnboardingStepIndicator = create_ssr_component(($$result, $$props, $$bindings, slots) => {
  let { steps = [] } = $$props;
  let { currentStepIndex = 0 } = $$props;
  if ($$props.steps === void 0 && $$bindings.steps && steps !== void 0)
    $$bindings.steps(steps);
  if ($$props.currentStepIndex === void 0 && $$bindings.currentStepIndex && currentStepIndex !== void 0)
    $$bindings.currentStepIndex(currentStepIndex);
  return `<div class="flex flex-col space-y-4 w-full">${each(steps, (step, i) => {
    return `<div class="flex items-center"><div class="${"flex items-center justify-center w-10 h-10 rounded-full " + escape(
      i === currentStepIndex ? "bg-primary-500" : i < currentStepIndex ? "bg-success-500" : "bg-surface-300",
      true
    ) + " text-on-" + escape(
      i === currentStepIndex ? "primary" : i < currentStepIndex ? "success" : "surface",
      true
    ) + "-token"}">${escape(i < currentStepIndex ? "✓" : step.id)}</div> <div class="ml-4"><div class="${"font-semibold " + escape(i === currentStepIndex ? "text-primary-500" : "", true)}">${escape(step.title)}</div> <div class="text-sm text-surface-600">${escape(step.description)}</div> </div></div> ${i < steps.length - 1 ? `<div class="${"ml-5 h-12 w-0.5 " + escape(
      i < currentStepIndex ? "bg-success-500" : "bg-surface-300",
      true
    )}"></div>` : ``}`;
  })}</div>`;
});
const PersonalContactInfo = create_ssr_component(($$result, $$props, $$bindings, slots) => {
  let { currentStep } = $$props;
  let { nextStep } = $$props;
  let { prevStep } = $$props;
  let { data = {} } = $$props;
  let name = data.name || "";
  let dateOfBirth = data.dateOfBirth || "";
  let address = data.address || "";
  let phone = data.phone || "";
  let email = data.email || "";
  data.maritalStatus || "";
  let dependents = data.dependents || 0;
  if ($$props.currentStep === void 0 && $$bindings.currentStep && currentStep !== void 0)
    $$bindings.currentStep(currentStep);
  if ($$props.nextStep === void 0 && $$bindings.nextStep && nextStep !== void 0)
    $$bindings.nextStep(nextStep);
  if ($$props.prevStep === void 0 && $$bindings.prevStep && prevStep !== void 0)
    $$bindings.prevStep(prevStep);
  if ($$props.data === void 0 && $$bindings.data && data !== void 0)
    $$bindings.data(data);
  return `<div class="space-y-6"><h2 class="h2" data-svelte-h="svelte-1x0x2re">Personal and Contact Information</h2> <form class="space-y-4"><label class="label"><span data-svelte-h="svelte-k4ffkn">Full Legal Name</span> <input class="input" type="text" required${add_attribute("value", name, 0)}></label> <label class="label"><span data-svelte-h="svelte-1mtqgri">Date of Birth</span> <input class="input" type="date" required${add_attribute("value", dateOfBirth, 0)}></label> <label class="label"><span data-svelte-h="svelte-1qa56i4">Residential Address</span> <textarea class="textarea" required>${escape(address || "")}</textarea></label> <div class="grid grid-cols-2 gap-4"><label class="label"><span data-svelte-h="svelte-ecako3">Phone Number</span> <input class="input" type="tel" required${add_attribute("value", phone, 0)}></label> <label class="label"><span data-svelte-h="svelte-1pum4x2">Email Address</span> <input class="input" type="email" required${add_attribute("value", email, 0)}></label></div> <div class="grid grid-cols-2 gap-4"><label class="label"><span data-svelte-h="svelte-rn4qiw">Marital Status</span> <select class="select" required><option value="" data-svelte-h="svelte-74n6qy">Select status</option><option value="single" data-svelte-h="svelte-mm641a">Single</option><option value="married" data-svelte-h="svelte-l4f57y">Married</option><option value="divorced" data-svelte-h="svelte-jcqm3m">Divorced</option><option value="widowed" data-svelte-h="svelte-1kv7nvw">Widowed</option></select></label> <label class="label"><span data-svelte-h="svelte-cg5zc4">Number of Dependents</span> <input class="input" type="number" min="0"${add_attribute("value", dependents, 0)}></label></div> <div class="flex justify-between mt-6"><button type="button" class="btn variant-ghost-surface" ${currentStep === 0 ? "disabled" : ""}>Back</button> <button type="submit" class="btn variant-filled-primary" data-svelte-h="svelte-10jd4bp">Continue</button></div></form></div>`;
});
const EmploymentIncome = create_ssr_component(($$result, $$props, $$bindings, slots) => {
  let { currentStep } = $$props;
  let { nextStep } = $$props;
  let { prevStep } = $$props;
  let { data = {} } = $$props;
  data.employmentStatus || "";
  let employer = data.employer || "";
  let jobTitle = data.jobTitle || "";
  let annualIncome = data.annualIncome || 0;
  let incomeSources = data.incomeSources || "";
  let anticipatedChanges = data.anticipatedChanges || "";
  if ($$props.currentStep === void 0 && $$bindings.currentStep && currentStep !== void 0)
    $$bindings.currentStep(currentStep);
  if ($$props.nextStep === void 0 && $$bindings.nextStep && nextStep !== void 0)
    $$bindings.nextStep(nextStep);
  if ($$props.prevStep === void 0 && $$bindings.prevStep && prevStep !== void 0)
    $$bindings.prevStep(prevStep);
  if ($$props.data === void 0 && $$bindings.data && data !== void 0)
    $$bindings.data(data);
  return `<div class="space-y-6"><h2 class="h2" data-svelte-h="svelte-hn91gj">Employment and Income Details</h2> <form class="space-y-4"><label class="label"><span data-svelte-h="svelte-17d5he2">Employment Status</span> <select class="select" required><option value="" data-svelte-h="svelte-74n6qy">Select status</option><option value="employed" data-svelte-h="svelte-p7a9ka">Employed</option><option value="self-employed" data-svelte-h="svelte-49o7vy">Self-employed</option><option value="unemployed" data-svelte-h="svelte-55gxda">Unemployed</option><option value="retired" data-svelte-h="svelte-lojnk0">Retired</option><option value="student" data-svelte-h="svelte-zujgbs">Student</option></select></label> <div class="grid grid-cols-2 gap-4"><label class="label"><span data-svelte-h="svelte-699z57">Employer</span> <input class="input" type="text"${add_attribute("value", employer, 0)}></label> <label class="label"><span data-svelte-h="svelte-10hgdb5">Job Title</span> <input class="input" type="text"${add_attribute("value", jobTitle, 0)}></label></div> <label class="label"><span data-svelte-h="svelte-olzphh">Annual Income ($)</span> <input class="input" type="number" min="0" required${add_attribute("value", annualIncome, 0)}></label> <label class="label"><span data-svelte-h="svelte-p3w82w">Sources of Income</span> <textarea class="textarea" placeholder="Salary, investments, rental income, etc.">${escape(incomeSources || "")}</textarea></label> <label class="label"><span data-svelte-h="svelte-v76i08">Anticipated Changes in Employment or Income</span> <textarea class="textarea" placeholder="Describe any expected changes">${escape(anticipatedChanges || "")}</textarea></label> <div class="flex justify-between mt-6"><button type="button" class="btn variant-ghost-surface" data-svelte-h="svelte-4bgkv2">Back</button> <button type="submit" class="btn variant-filled-primary" data-svelte-h="svelte-10jd4bp">Continue</button></div></form></div>`;
});
const FinancialAssets = create_ssr_component(($$result, $$props, $$bindings, slots) => {
  let { currentStep } = $$props;
  let { nextStep } = $$props;
  let { prevStep } = $$props;
  let { data = {} } = $$props;
  let bankAccounts = data.bankAccounts || 0;
  let investmentAccounts = data.investmentAccounts || 0;
  let retirementAccounts = data.retirementAccounts || 0;
  let realEstate = data.realEstate || 0;
  let otherAssets = data.otherAssets || "";
  let mortgages = data.mortgages || 0;
  let loans = data.loans || 0;
  let creditCardDebt = data.creditCardDebt || 0;
  if ($$props.currentStep === void 0 && $$bindings.currentStep && currentStep !== void 0)
    $$bindings.currentStep(currentStep);
  if ($$props.nextStep === void 0 && $$bindings.nextStep && nextStep !== void 0)
    $$bindings.nextStep(nextStep);
  if ($$props.prevStep === void 0 && $$bindings.prevStep && prevStep !== void 0)
    $$bindings.prevStep(prevStep);
  if ($$props.data === void 0 && $$bindings.data && data !== void 0)
    $$bindings.data(data);
  return `<div class="space-y-6"><h2 class="h2" data-svelte-h="svelte-1bmzzsd">Financial Assets and Liabilities</h2> <form class="space-y-6"><div class="space-y-4"><h3 class="h3" data-svelte-h="svelte-gq6lsx">Assets</h3> <label class="label"><span data-svelte-h="svelte-7z0y21">Bank Account Balances ($)</span> <input class="input" type="number" min="0"${add_attribute("value", bankAccounts, 0)}></label> <label class="label"><span data-svelte-h="svelte-13154ai">Investment Accounts ($)</span> <input class="input" type="number" min="0"${add_attribute("value", investmentAccounts, 0)}></label> <label class="label"><span data-svelte-h="svelte-rbjmmk">Retirement Accounts ($)</span> <input class="input" type="number" min="0"${add_attribute("value", retirementAccounts, 0)}></label> <label class="label"><span data-svelte-h="svelte-1tahzpl">Real Estate Holdings ($)</span> <input class="input" type="number" min="0"${add_attribute("value", realEstate, 0)}></label> <label class="label"><span data-svelte-h="svelte-1b1lq5s">Other Significant Assets</span> <textarea class="textarea" placeholder="Describe other valuable assets">${escape(otherAssets || "")}</textarea></label></div> <div class="space-y-4"><h3 class="h3" data-svelte-h="svelte-zgwnun">Liabilities</h3> <label class="label"><span data-svelte-h="svelte-q2xb8y">Outstanding Mortgages ($)</span> <input class="input" type="number" min="0"${add_attribute("value", mortgages, 0)}></label> <label class="label"><span data-svelte-h="svelte-290xq4">Other Loans ($)</span> <input class="input" type="number" min="0"${add_attribute("value", loans, 0)}></label> <label class="label"><span data-svelte-h="svelte-1qqj8r7">Credit Card Balances ($)</span> <input class="input" type="number" min="0"${add_attribute("value", creditCardDebt, 0)}></label></div> <div class="flex justify-between mt-6"><button type="button" class="btn variant-ghost-surface" data-svelte-h="svelte-4bgkv2">Back</button> <button type="submit" class="btn variant-filled-primary" data-svelte-h="svelte-10jd4bp">Continue</button></div></form></div>`;
});
const InsuranceCoverage = create_ssr_component(($$result, $$props, $$bindings, slots) => {
  let { currentStep } = $$props;
  let { nextStep } = $$props;
  let { prevStep } = $$props;
  let { data = {} } = $$props;
  let lifeInsurance = data.lifeInsurance || { coverage: 0, beneficiaries: "" };
  let healthInsurance = data.healthInsurance || { coverage: 0, provider: "" };
  let disabilityInsurance = data.disabilityInsurance || { coverage: 0, provider: "" };
  let longTermCareInsurance = data.longTermCareInsurance || { coverage: 0, provider: "" };
  if ($$props.currentStep === void 0 && $$bindings.currentStep && currentStep !== void 0)
    $$bindings.currentStep(currentStep);
  if ($$props.nextStep === void 0 && $$bindings.nextStep && nextStep !== void 0)
    $$bindings.nextStep(nextStep);
  if ($$props.prevStep === void 0 && $$bindings.prevStep && prevStep !== void 0)
    $$bindings.prevStep(prevStep);
  if ($$props.data === void 0 && $$bindings.data && data !== void 0)
    $$bindings.data(data);
  return `<div class="space-y-6"><h2 class="h2" data-svelte-h="svelte-1oyosb5">Insurance Coverage</h2> <form class="space-y-6"><div class="space-y-4"><h3 class="h3" data-svelte-h="svelte-vm3wyo">Life Insurance</h3> <div class="grid grid-cols-2 gap-4"><label class="label"><span data-svelte-h="svelte-nndpy5">Coverage Amount ($)</span> <input class="input" type="number" min="0"${add_attribute("value", lifeInsurance.coverage, 0)}></label> <label class="label"><span data-svelte-h="svelte-97h2vh">Beneficiaries</span> <input class="input" type="text"${add_attribute("value", lifeInsurance.beneficiaries, 0)}></label></div></div> <div class="space-y-4"><h3 class="h3" data-svelte-h="svelte-1yduwjs">Health Insurance</h3> <div class="grid grid-cols-2 gap-4"><label class="label"><span data-svelte-h="svelte-nndpy5">Coverage Amount ($)</span> <input class="input" type="number" min="0"${add_attribute("value", healthInsurance.coverage, 0)}></label> <label class="label"><span data-svelte-h="svelte-19ypbgj">Provider</span> <input class="input" type="text"${add_attribute("value", healthInsurance.provider, 0)}></label></div></div> <div class="space-y-4"><h3 class="h3" data-svelte-h="svelte-o14es4">Disability Insurance</h3> <div class="grid grid-cols-2 gap-4"><label class="label"><span data-svelte-h="svelte-nndpy5">Coverage Amount ($)</span> <input class="input" type="number" min="0"${add_attribute("value", disabilityInsurance.coverage, 0)}></label> <label class="label"><span data-svelte-h="svelte-19ypbgj">Provider</span> <input class="input" type="text"${add_attribute("value", disabilityInsurance.provider, 0)}></label></div></div> <div class="space-y-4"><h3 class="h3" data-svelte-h="svelte-la8vf2">Long-term Care Insurance</h3> <div class="grid grid-cols-2 gap-4"><label class="label"><span data-svelte-h="svelte-nndpy5">Coverage Amount ($)</span> <input class="input" type="number" min="0"${add_attribute("value", longTermCareInsurance.coverage, 0)}></label> <label class="label"><span data-svelte-h="svelte-19ypbgj">Provider</span> <input class="input" type="text"${add_attribute("value", longTermCareInsurance.provider, 0)}></label></div></div> <div class="flex justify-between mt-6"><button type="button" class="btn variant-ghost-surface" data-svelte-h="svelte-4bgkv2">Back</button> <button type="submit" class="btn variant-filled-primary" data-svelte-h="svelte-10jd4bp">Continue</button></div></form></div>`;
});
const EstatePlanning = create_ssr_component(($$result, $$props, $$bindings, slots) => {
  let { currentStep } = $$props;
  let { nextStep } = $$props;
  let { prevStep } = $$props;
  let { data = {} } = $$props;
  let hasWill = data.hasWill || false;
  let willDetails = data.willDetails || "";
  let hasTrusts = data.hasTrusts || false;
  let trustDetails = data.trustDetails || "";
  let hasPowerOfAttorney = data.hasPowerOfAttorney || false;
  let powerOfAttorneyDetails = data.powerOfAttorneyDetails || "";
  let beneficiaryDesignations = data.beneficiaryDesignations || "";
  if ($$props.currentStep === void 0 && $$bindings.currentStep && currentStep !== void 0)
    $$bindings.currentStep(currentStep);
  if ($$props.nextStep === void 0 && $$bindings.nextStep && nextStep !== void 0)
    $$bindings.nextStep(nextStep);
  if ($$props.prevStep === void 0 && $$bindings.prevStep && prevStep !== void 0)
    $$bindings.prevStep(prevStep);
  if ($$props.data === void 0 && $$bindings.data && data !== void 0)
    $$bindings.data(data);
  return `<div class="space-y-6"><h2 class="h2" data-svelte-h="svelte-1buw54w">Estate Planning Documents</h2> <form class="space-y-6"><div class="space-y-4"><h3 class="h3" data-svelte-h="svelte-qajfq6">Will</h3> <label class="flex items-center space-x-2"><input class="checkbox" type="checkbox"${add_attribute("checked", hasWill, 1)}> <span data-svelte-h="svelte-1jacemq">I have a will</span></label> ${hasWill ? `<label class="label"><span data-svelte-h="svelte-5qb6u8">Will Details</span> <textarea class="textarea" placeholder="Describe your will and any recent updates">${escape(willDetails || "")}</textarea></label>` : ``}</div> <div class="space-y-4"><h3 class="h3" data-svelte-h="svelte-1e63fr7">Trusts</h3> <label class="flex items-center space-x-2"><input class="checkbox" type="checkbox"${add_attribute("checked", hasTrusts, 1)}> <span data-svelte-h="svelte-1vuzq7w">I have trusts</span></label> ${hasTrusts ? `<label class="label"><span data-svelte-h="svelte-1oasqia">Trust Details</span> <textarea class="textarea" placeholder="Describe your trusts">${escape(trustDetails || "")}</textarea></label>` : ``}</div> <div class="space-y-4"><h3 class="h3" data-svelte-h="svelte-10q9mz0">Power of Attorney</h3> <label class="flex items-center space-x-2"><input class="checkbox" type="checkbox"${add_attribute("checked", hasPowerOfAttorney, 1)}> <span data-svelte-h="svelte-1a4fl0f">I have power of attorney documents</span></label> ${hasPowerOfAttorney ? `<label class="label"><span data-svelte-h="svelte-1j9q938">Power of Attorney Details</span> <textarea class="textarea" placeholder="Describe your power of attorney arrangements">${escape(powerOfAttorneyDetails || "")}</textarea></label>` : ``}</div> <div class="space-y-4"><h3 class="h3" data-svelte-h="svelte-nd9tir">Beneficiary Designations</h3> <label class="label"><span data-svelte-h="svelte-17axdff">Beneficiary Designations on Financial Accounts</span> <textarea class="textarea" placeholder="List beneficiaries on retirement accounts, insurance policies, etc.">${escape(beneficiaryDesignations || "")}</textarea></label></div> <div class="flex justify-between mt-6"><button type="button" class="btn variant-ghost-surface" data-svelte-h="svelte-4bgkv2">Back</button> <button type="submit" class="btn variant-filled-primary" data-svelte-h="svelte-10jd4bp">Continue</button></div></form></div>`;
});
const TaxInformation = create_ssr_component(($$result, $$props, $$bindings, slots) => {
  let { currentStep } = $$props;
  let { nextStep } = $$props;
  let { prevStep } = $$props;
  let { data = {} } = $$props;
  data.filingStatus || "";
  let recentTaxReturns = data.recentTaxReturns || "";
  let taxDeferredAccounts = data.taxDeferredAccounts || "";
  let taxLiabilities = data.taxLiabilities || 0;
  let taxStrategies = data.taxStrategies || "";
  if ($$props.currentStep === void 0 && $$bindings.currentStep && currentStep !== void 0)
    $$bindings.currentStep(currentStep);
  if ($$props.nextStep === void 0 && $$bindings.nextStep && nextStep !== void 0)
    $$bindings.nextStep(nextStep);
  if ($$props.prevStep === void 0 && $$bindings.prevStep && prevStep !== void 0)
    $$bindings.prevStep(prevStep);
  if ($$props.data === void 0 && $$bindings.data && data !== void 0)
    $$bindings.data(data);
  return `<div class="space-y-6"><h2 class="h2" data-svelte-h="svelte-181s5h0">Tax Information</h2> <form class="space-y-4"><label class="label"><span data-svelte-h="svelte-1sdfvvi">Tax Filing Status</span> <select class="select" required><option value="" data-svelte-h="svelte-11zj9r9">Select filing status</option><option value="single" data-svelte-h="svelte-mm641a">Single</option><option value="married-joint" data-svelte-h="svelte-imi6qb">Married Filing Jointly</option><option value="married-separate" data-svelte-h="svelte-7hwb07">Married Filing Separately</option><option value="head-of-household" data-svelte-h="svelte-v8idss">Head of Household</option><option value="qualifying-widow" data-svelte-h="svelte-1nuhodr">Qualifying Widow(er)</option></select></label> <label class="label"><span data-svelte-h="svelte-6xru17">Recent Tax Returns</span> <textarea class="textarea" placeholder="Describe your recent tax returns and any notable items">${escape(recentTaxReturns || "")}</textarea></label> <label class="label"><span data-svelte-h="svelte-4uch11">Tax-Deferred Accounts</span> <textarea class="textarea" placeholder="List 401(k), IRA, and other tax-deferred accounts">${escape(taxDeferredAccounts || "")}</textarea></label> <label class="label"><span data-svelte-h="svelte-pzbdi">Current Tax Liabilities ($)</span> <input class="input" type="number" min="0"${add_attribute("value", taxLiabilities, 0)}></label> <label class="label"><span data-svelte-h="svelte-7bro1b">Tax Planning Strategies</span> <textarea class="textarea" placeholder="Describe any current tax planning strategies or concerns">${escape(taxStrategies || "")}</textarea></label> <div class="flex justify-between mt-6"><button type="button" class="btn variant-ghost-surface" data-svelte-h="svelte-4bgkv2">Back</button> <button type="submit" class="btn variant-filled-primary" data-svelte-h="svelte-10jd4bp">Continue</button></div></form></div>`;
});
const FinancialGoals = create_ssr_component(($$result, $$props, $$bindings, slots) => {
  let { currentStep } = $$props;
  let { nextStep } = $$props;
  let { prevStep } = $$props;
  let { data = {} } = $$props;
  let shortTermGoals = data.shortTermGoals || "";
  let longTermGoals = data.longTermGoals || "";
  let retirementAge = data.retirementAge || 0;
  let retirementIncome = data.retirementIncome || 0;
  let educationFunding = data.educationFunding || "";
  let charitableGiving = data.charitableGiving || "";
  if ($$props.currentStep === void 0 && $$bindings.currentStep && currentStep !== void 0)
    $$bindings.currentStep(currentStep);
  if ($$props.nextStep === void 0 && $$bindings.nextStep && nextStep !== void 0)
    $$bindings.nextStep(nextStep);
  if ($$props.prevStep === void 0 && $$bindings.prevStep && prevStep !== void 0)
    $$bindings.prevStep(prevStep);
  if ($$props.data === void 0 && $$bindings.data && data !== void 0)
    $$bindings.data(data);
  return `<div class="space-y-6"><h2 class="h2" data-svelte-h="svelte-jrkasl">Financial Goals and Objectives</h2> <form class="space-y-4"><label class="label"><span data-svelte-h="svelte-fs47qw">Short-term Financial Goals (1-5 years)</span> <textarea class="textarea" placeholder="Emergency fund, home purchase, debt payoff, etc.">${escape(shortTermGoals || "")}</textarea></label> <label class="label"><span data-svelte-h="svelte-lybp2j">Long-term Financial Goals (5+ years)</span> <textarea class="textarea" placeholder="Retirement, wealth building, major purchases, etc.">${escape(longTermGoals || "")}</textarea></label> <div class="space-y-4"><h3 class="h3" data-svelte-h="svelte-1dd0qio">Retirement Planning</h3> <div class="grid grid-cols-2 gap-4"><label class="label"><span data-svelte-h="svelte-16p3z4s">Desired Retirement Age</span> <input class="input" type="number" min="50" max="80"${add_attribute("value", retirementAge, 0)}></label> <label class="label"><span data-svelte-h="svelte-y3ht16">Desired Annual Retirement Income ($)</span> <input class="input" type="number" min="0"${add_attribute("value", retirementIncome, 0)}></label></div></div> <label class="label"><span data-svelte-h="svelte-ak46df">Education Funding Plans</span> <textarea class="textarea" placeholder="College savings, 529 plans, etc.">${escape(educationFunding || "")}</textarea></label> <label class="label"><span data-svelte-h="svelte-l05oso">Charitable Giving Intentions</span> <textarea class="textarea" placeholder="Current giving, planned donations, charitable goals">${escape(charitableGiving || "")}</textarea></label> <div class="flex justify-between mt-6"><button type="button" class="btn variant-ghost-surface" data-svelte-h="svelte-4bgkv2">Back</button> <button type="submit" class="btn variant-filled-primary" data-svelte-h="svelte-10jd4bp">Continue</button></div></form></div>`;
});
const RiskTolerance = create_ssr_component(($$result, $$props, $$bindings, slots) => {
  let { currentStep } = $$props;
  let { nextStep } = $$props;
  let { prevStep } = $$props;
  let { data = {} } = $$props;
  data.riskTolerance || "";
  data.investmentExperience || "";
  let investmentPreferences = data.investmentPreferences || "";
  let investmentRestrictions = data.investmentRestrictions || "";
  data.timeHorizon || "";
  if ($$props.currentStep === void 0 && $$bindings.currentStep && currentStep !== void 0)
    $$bindings.currentStep(currentStep);
  if ($$props.nextStep === void 0 && $$bindings.nextStep && nextStep !== void 0)
    $$bindings.nextStep(nextStep);
  if ($$props.prevStep === void 0 && $$bindings.prevStep && prevStep !== void 0)
    $$bindings.prevStep(prevStep);
  if ($$props.data === void 0 && $$bindings.data && data !== void 0)
    $$bindings.data(data);
  return `<div class="space-y-6"><h2 class="h2" data-svelte-h="svelte-vhumy3">Risk Tolerance and Investment Preferences</h2> <form class="space-y-4"><label class="label"><span data-svelte-h="svelte-6ja8iz">Investment Risk Tolerance</span> <select class="select" required><option value="" data-svelte-h="svelte-1drn0h0">Select risk tolerance</option><option value="conservative" data-svelte-h="svelte-g89kod">Conservative - Prefer stability over growth</option><option value="moderate" data-svelte-h="svelte-npl4ds">Moderate - Balanced approach to risk and return</option><option value="aggressive" data-svelte-h="svelte-f9ruzt">Aggressive - Willing to accept higher risk for potential higher returns</option></select></label> <label class="label"><span data-svelte-h="svelte-15mgweb">Investment Experience</span> <select class="select" required><option value="" data-svelte-h="svelte-1e2z7s6">Select experience level</option><option value="beginner" data-svelte-h="svelte-181xdg0">Beginner - Limited investment experience</option><option value="intermediate" data-svelte-h="svelte-hfy1vu">Intermediate - Some investment experience</option><option value="advanced" data-svelte-h="svelte-1cvjnsh">Advanced - Extensive investment experience</option><option value="professional" data-svelte-h="svelte-rfhzyl">Professional - Work in finance/investing</option></select></label> <label class="label"><span data-svelte-h="svelte-ixdepl">Investment Time Horizon</span> <select class="select" required><option value="" data-svelte-h="svelte-1mbzutm">Select time horizon</option><option value="short" data-svelte-h="svelte-109j0gl">Short-term (1-3 years)</option><option value="medium" data-svelte-h="svelte-10643bl">Medium-term (3-10 years)</option><option value="long" data-svelte-h="svelte-1528n9a">Long-term (10+ years)</option></select></label> <label class="label"><span data-svelte-h="svelte-1ftk5af">Preferred Investment Strategies</span> <textarea class="textarea" placeholder="Growth stocks, dividend investing, index funds, real estate, etc.">${escape(investmentPreferences || "")}</textarea></label> <label class="label"><span data-svelte-h="svelte-yen4od">Investment Restrictions or Preferences</span> <textarea class="textarea" placeholder="ESG investing, sector restrictions, ethical considerations, etc.">${escape(investmentRestrictions || "")}</textarea></label> <div class="flex justify-between mt-6"><button type="button" class="btn variant-ghost-surface" data-svelte-h="svelte-4bgkv2">Back</button> <button type="submit" class="btn variant-filled-primary" data-svelte-h="svelte-16ju4y1">Complete Application</button></div></form></div>`;
});
const Page = create_ssr_component(($$result, $$props, $$bindings, slots) => {
  let { data } = $$props;
  let currentStep = 0;
  let applicationData = {};
  const steps = [
    {
      title: "Personal & Contact Info",
      component: PersonalContactInfo
    },
    {
      title: "Employment & Income",
      component: EmploymentIncome
    },
    {
      title: "Financial Assets",
      component: FinancialAssets
    },
    {
      title: "Insurance Coverage",
      component: InsuranceCoverage
    },
    {
      title: "Estate Planning",
      component: EstatePlanning
    },
    {
      title: "Tax Information",
      component: TaxInformation
    },
    {
      title: "Financial Goals",
      component: FinancialGoals
    },
    {
      title: "Risk Tolerance",
      component: RiskTolerance
    }
  ];
  function nextStep() {
    if (currentStep < steps.length - 1) {
      currentStep++;
    } else {
      console.log("Application completed:", applicationData);
    }
  }
  function prevStep() {
    if (currentStep > 0) {
      currentStep--;
    }
  }
  if ($$props.data === void 0 && $$bindings.data && data !== void 0)
    $$bindings.data(data);
  let $$settled;
  let $$rendered;
  let previous_head = $$result.head;
  do {
    $$settled = true;
    $$result.head = previous_head;
    $$rendered = `<div class="container mx-auto p-6 max-w-4xl"><h1 class="h1 mb-8" data-svelte-h="svelte-1tnk3ao">Wealth Management Application</h1> ${validate_component(OnboardingStepIndicator, "OnboardingStepIndicator").$$render(
      $$result,
      {
        steps: steps.map((s) => s.title),
        currentStep
      },
      {},
      {}
    )} <div class="mt-8">${each(steps, (step, index) => {
      return `${index === currentStep ? `${validate_component(step.component || missing_component, "svelte:component").$$render(
        $$result,
        {
          currentStep,
          nextStep,
          prevStep,
          data: applicationData
        },
        {
          data: ($$value) => {
            applicationData = $$value;
            $$settled = false;
          }
        },
        {}
      )}` : ``}`;
    })}</div></div>`;
  } while (!$$settled);
  return $$rendered;
});
export {
  Page as default
};
