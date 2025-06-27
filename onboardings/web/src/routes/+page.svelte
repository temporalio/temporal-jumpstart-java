<script>
	// Import step components
	import Step1Welcome from '$lib/components/steps/Step1Welcome.svelte';
	import Step2PersonalInfo from '$lib/components/steps/Step2PersonalInfo.svelte';
	import Step3Preferences from '$lib/components/steps/Step3Preferences.svelte';
	import Step4Completion from '$lib/components/steps/Step4Completion.svelte';
	import OnboardingStepIndicator from '$lib/components/OnboardingStepIndicator.svelte';
	import OnboardingHero from '$lib/components/OnboardingHero.svelte';

	// Define the onboarding steps
	const steps = [
		{ id: 1, title: 'Welcome', description: 'Get started with our application' },
		{ id: 2, title: 'Personal Information', description: 'Tell us about yourself' },
		{ id: 3, title: 'Preferences', description: 'Customize your experience' },
		{ id: 4, title: 'Completion', description: 'All set and ready to go' }
	];

	// Current step tracker
	let currentStepIndex = 0;
	let currentStep = steps[currentStepIndex];

	// Navigation functions
	function nextStep() {
		if (currentStepIndex < steps.length - 1) {
			currentStepIndex++;
			currentStep = steps[currentStepIndex];
		}
	}

	function prevStep() {
		if (currentStepIndex > 0) {
			currentStepIndex--;
			currentStep = steps[currentStepIndex];
		}
	}
</script>

<div class="container h-full mx-auto p-4">
	<div class="card w-full h-[90vh] grid grid-cols-1 md:grid-cols-2 overflow-hidden">
		<!-- Left column: Hero section -->
		<div class="hidden md:block">
			<OnboardingHero {currentStep} />
		</div>

		<!-- Right column: Steps and forms -->
		<div class="p-8 overflow-y-auto">
			<div class="flex justify-between items-center mb-8">
				<h1 class="h3">Onboarding</h1>
				<div class="badge variant-filled">{currentStepIndex + 1} of {steps.length}</div>
			</div>

			<!-- Step indicator (vertical) -->
			<div class="hidden lg:block float-left mr-8 w-64">
				<OnboardingStepIndicator {steps} {currentStepIndex} />
			</div>

			<!-- Dynamic step content -->
			<div class="lg:ml-72">
				{#if currentStepIndex === 0}
					<Step1Welcome {nextStep} />
				{:else if currentStepIndex === 1}
					<Step2PersonalInfo {nextStep} {prevStep} />
				{:else if currentStepIndex === 2}
					<Step3Preferences {nextStep} {prevStep} />
				{:else if currentStepIndex === 3}
					<Step4Completion {prevStep} />
				{/if}
			</div>
		</div>
	</div>
</div>
