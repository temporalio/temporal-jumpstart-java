<script>
	import { onboarding } from '$lib/stores/onboarding';
	export let nextStep;
	let loading = false;

	async function handleStart() {
		try {
			loading = true;
			// Since this is the welcome step, we just want to move to the next step
			// without saving specific data
			await onboarding.saveStep(0, {});
			nextStep();
		} catch (error) {
			console.error('Failed to start account:', error);
		} finally {
			loading = false;
		}
	}
</script>

<div class="space-y-6">
	<h2 class="h2">Welcome</h2>
	<p>
		Welcome to our account process. This will help you get started with our account.
		Follow the steps to configure your account and preferences.
	</p>
	<div class="flex justify-end mt-4">
		<button 
			class="btn variant-filled-primary" 
			on:click={handleStart}
			disabled={loading}
		>
			{#if loading}
				<span class="spinner-border spinner-border-sm mr-2" role="status" aria-hidden="true"></span>
			{/if}
			Get Started
		</button>
	</div>
</div>
