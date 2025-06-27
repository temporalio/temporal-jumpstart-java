<script>
	import { getModalStore } from '@skeletonlabs/skeleton';
	// Define the onboarding steps
	const steps = [
		{ id: 1, title: 'Welcome', description: 'Welcome to the Onboarding App' },
		{ id: 2, title: 'Personal Information', description: 'Tell us about yourself' },
		{ id: 3, title: 'Preferences', description: 'Set your preferences' },
		{ id: 4, title: 'Completion', description: 'All done!' }
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

<div class="container h-full mx-auto flex justify-center items-center">
	<div class="card p-8 shadow-xl w-full max-w-3xl">
		<!-- Progress indicator -->
		<div class="flex justify-between mb-8">
			{#each steps as step, i}
				<div class="flex flex-col items-center">
					<div class="flex items-center justify-center w-10 h-10 rounded-full 
						{i === currentStepIndex ? 'bg-primary-500' : 
							i < currentStepIndex ? 'bg-success-500' : 'bg-surface-300'} 
						text-on-{i === currentStepIndex ? 'primary' : 
							i < currentStepIndex ? 'success' : 'surface'}-token">
						{i < currentStepIndex ? '✓' : step.id}
					</div>
					<span class="text-xs mt-2">{step.title}</span>
				</div>
				{#if i < steps.length - 1}
					<div class="flex-1 flex items-center">
						<div class="h-0.5 w-full {i < currentStepIndex ? 'bg-success-500' : 'bg-surface-300'}"></div>
					</div>
				{/if}
			{/each}
		</div>

		<!-- Step content -->
		<h1 class="h1 mb-4">{currentStep.title}</h1>
		<p class="mb-8">{currentStep.description}</p>

		<!-- Step specific content -->
		{#if currentStepIndex === 0}
			<div class="space-y-4">
				<p>Welcome to our onboarding process. This will help you get started with our application.</p>
			</div>
		{:else if currentStepIndex === 1}
			<div class="space-y-4">
				<label class="label">
					<span>Full Name</span>
					<input class="input" type="text" placeholder="Enter your full name" />
				</label>
				<label class="label">
					<span>Email</span>
					<input class="input" type="email" placeholder="Enter your email" />
				</label>
			</div>
		{:else if currentStepIndex === 2}
			<div class="space-y-4">
				<label class="label">
					<span>Theme Preference</span>
					<select class="select">
						<option value="light">Light</option>
						<option value="dark">Dark</option>
						<option value="auto">System Default</option>
					</select>
				</label>
				<label class="label flex items-center space-x-2">
					<input class="checkbox" type="checkbox" />
					<span>Enable notifications</span>
				</label>
			</div>
		{:else if currentStepIndex === 3}
			<div class="space-y-4 text-center">
				<div class="text-5xl mb-4">🎉</div>
				<p>Congratulations! You have completed the onboarding process.</p>
			</div>
		{/if}

		<!-- Navigation buttons -->
		<div class="flex justify-between mt-8">
			<button class="btn variant-ghost-surface" on:click={prevStep} disabled={currentStepIndex === 0}>Previous</button>
			{#if currentStepIndex < steps.length - 1}
				<button class="btn variant-filled-primary" on:click={nextStep}>Next</button>
			{:else}
				<button class="btn variant-filled-success">Finish</button>
			{/if}
		</div>
	</div>
</div>
