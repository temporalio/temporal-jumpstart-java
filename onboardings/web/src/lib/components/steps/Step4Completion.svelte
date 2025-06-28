<script>
	import { onboarding } from '$lib/stores/onboarding';
	export let prevStep;

	let loading = false;
	let error = null;
	let onboardingComplete = false;

	async function completeOnboarding() {
		try {
			loading = true;
			error = null;

			// Mark the onboarding as complete
			await onboarding.complete();
			onboardingComplete = true;
		} catch (err) {
			error = err.message || 'Failed to complete onboarding';
			console.error('Error completing onboarding:', err);
		} finally {
			loading = false;
		}
	}
</script>

<div class="space-y-6 text-center">
	<div class="text-5xl mb-4">🎉</div>
	<h2 class="h2">All Done!</h2>
	<p>Congratulations! You have completed the onboarding process.</p>
	<p>You can now start using all the features of our application.</p>

	{#if error}
		<div class="alert variant-filled-error">
			<span>{error}</span>
		</div>
	{/if}

	<div class="flex justify-between mt-6">
		<button 
			class="btn variant-ghost-surface" 
			on:click={prevStep}
			disabled={loading || onboardingComplete}
		>Back</button>

		{#if !onboardingComplete}
			<button 
				class="btn variant-filled-success" 
				on:click={completeOnboarding}
				disabled={loading}
			>
				{#if loading}
					<span class="spinner-border spinner-border-sm mr-2" role="status" aria-hidden="true"></span>
				{/if}
				Complete Onboarding
			</button>
		{:else}
			<a href="/dashboard" class="btn variant-filled-success">Go to Dashboard</a>
		{/if}
	</div>
</div>
