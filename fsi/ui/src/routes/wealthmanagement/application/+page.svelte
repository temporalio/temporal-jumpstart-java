<script lang="ts">
	import PersonalContactInfo from '$lib/components/wealthmanagement/steps/PersonalContactInfo.svelte'
	import EmploymentIncome from '$lib/components/wealthmanagement/steps/EmploymentIncome.svelte'
	import FinancialAssets from '$lib/components/wealthmanagement/steps/FinancialAssets.svelte'
	import InsuranceCoverage from '$lib/components/wealthmanagement/steps/InsuranceCoverage.svelte'
	import EstatePlanning from '$lib/components/wealthmanagement/steps/EstatePlanning.svelte'
	import TaxInformation from '$lib/components/wealthmanagement/steps/TaxInformation.svelte'
	import FinancialGoals from '$lib/components/wealthmanagement/steps/FinancialGoals.svelte'
	import RiskTolerance from '$lib/components/wealthmanagement/steps/RiskTolerance.svelte'
	import OnboardingStepIndicator from '$lib/components/OnboardingStepIndicator.svelte'

	export let data
	
	let currentStep = 0
	let applicationData = {}
	
	const steps = [
		{ title: 'Personal & Contact Info', component: PersonalContactInfo },
		{ title: 'Employment & Income', component: EmploymentIncome },
		{ title: 'Financial Assets', component: FinancialAssets },
		{ title: 'Insurance Coverage', component: InsuranceCoverage },
		{ title: 'Estate Planning', component: EstatePlanning },
		{ title: 'Tax Information', component: TaxInformation },
		{ title: 'Financial Goals', component: FinancialGoals },
		{ title: 'Risk Tolerance', component: RiskTolerance }
	]
	
	function nextStep() {
		if (currentStep < steps.length - 1) {
			currentStep++
		} else {
			// Application complete
			console.log('Application completed:', applicationData)
			// Here you would typically submit the data
		}
	}
	
	function prevStep() {
		if (currentStep > 0) {
			currentStep--
		}
	}
</script>

<div class="container mx-auto p-6 max-w-4xl">
	<h1 class="h1 mb-8">Wealth Management Application</h1>
	
	<OnboardingStepIndicator 
		steps={steps.map(s => s.title)} 
		{currentStep} 
	/>
	
	<div class="mt-8">
		{#each steps as step, index}
			{#if index === currentStep}
				<svelte:component 
					this={step.component}
					{currentStep}
					{nextStep}
					{prevStep}
					bind:data={applicationData}
				/>
			{/if}
		{/each}
	</div>
</div>
