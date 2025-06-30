<script>
	import { onMount } from 'svelte';
	import { page } from '$app/stores';

	import { account } from '$lib/stores/account.js';

	// Import step components
	import Welcome from '$lib/components/steps/Welcome.svelte';
	import PersonalInfo from '$lib/components/steps/PersonalInfo.svelte';
	import Preferences from '$lib/components/steps/Preferences.svelte';
	import Completion from '$lib/components/steps/Completion.svelte';
	import OnboardingStepIndicator from '$lib/components/OnboardingStepIndicator.svelte';
	import OnboardingHero from '$lib/components/OnboardingHero.svelte';

	// Define the account steps
	const steps = [
		{ id: 1, title: 'Welcome', description: 'Get started with our account' },
		{ id: 2, title: 'Personal Information', description: 'Tell us about yourself' },
		{ id: 3, title: 'Completion', description: 'Wealth is headed your way!' }
	];
	// { id: 3, title: 'Preferences', description: 'Customize your experience' },

	// Current step tracker
	let currentStepIndex = 0;
	let currentStep = steps[currentStepIndex];
	let initializing = true;
	let error = null;

	// Subscribe to account store changes
	onMount(async () => {
		try {
			// Check if we already have an ongoing account process
			const unsubscribe = account.subscribe(state => {
				// if (!state.id) {
				// 	// Redirect to landing page if no account process is found
				// 	window.location.href = '/';
				// 	return;
				// }

				// Update current step based on store data
				if (state.currentStep !== undefined) {
					currentStepIndex = state.currentStep;
					currentStep = steps[currentStepIndex];
				}
			});

			initializing = false;
			return unsubscribe;
		} catch (err) {
			error = err.message || 'Failed to load account process';
			console.error('Onboarding error:', err);
			initializing = false;
		}
	});

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
	{#if initializing}
		<div class="flex justify-center items-center h-full">
			<div class="card p-8 text-center">
				<div class="spinner-border spinner-border-lg" role="status"></div>
				<p class="mt-4">Loading your account...</p>
			</div>
		</div>
	{:else if error}
		<div class="flex justify-center items-center h-full">
			<div class="card p-8 text-center">
				<div class="alert variant-filled-error">
					<span>{error}</span>
				</div>
				<button 
					class="btn variant-filled-primary mt-4" 
					on:click={() => window.location.href = '/'}
				>Return to Homepage</button>
			</div>
		</div>
	{:else}
		<div class="card w-full h-[90vh] grid grid-cols-1 md:grid-cols-2 overflow-hidden">
			<!-- Left column: Hero section -->
			<div class="hidden md:block">
				<OnboardingHero {currentStep} />
			</div>

			<!-- Right column: Steps and forms -->
			<div class="p-8 overflow-y-auto">
				<div class="flex justify-between items-center mb-8">
					<h1 class="h3">Complete Your Application</h1>
					<div class="badge variant-filled">{currentStepIndex + 1} of {steps.length}</div>
				</div>

				<!-- Step indicator (vertical) -->
				<div class="hidden lg:block float-left mr-8 w-64">
					<OnboardingStepIndicator {steps} {currentStepIndex} />
				</div>

				<!-- Dynamic step content -->
				<div class="lg:ml-72">
					{#if currentStepIndex === 0}
						<Welcome {nextStep} currentStep={currentStepIndex}/>
					{:else if currentStepIndex === 1}
						<PersonalInfo {nextStep} {prevStep}  currentStep={currentStepIndex} />
					<!--{:else if currentStepIndex === 2}-->
					<!--	<Preferences {nextStep} {prevStep} currentStep={currentStepIndex}/>-->
					{:else if currentStepIndex === 2}
						<Completion {prevStep} currentStep={currentStepIndex} />
					{/if}
				</div>
			</div>
		</div>
	{/if}
</div>
