<script>
	import { account } from '$lib/stores/account.js';
	export let prevStep;
	export let currentStep;
	let loading = false;
	let error = null;
	let onboardingComplete = false;

	async function completeOnboarding() {
		try {
			loading = true;
			error = null;

			// Mark the account as complete
			// await account.complete();

			window.location.href = '/';
			onboardingComplete = true;
		} catch (err) {
			error = err.message || 'Failed to complete account';
			console.error('Error completing account:', err);
		} finally {
			loading = false;
		}
	}
</script>

<div class="space-y-6 text-center">
	<div class="text-5xl mb-4">🎉</div>
	<h2 class="h2">All Done!</h2>
	<p>Congratulations! You have completed the account process.</p>
	<p>One of our Wealth Management advisors will reach out to you shortly.</p>

	{#if error}
		<div class="alert variant-filled-error">
			<span>{error}</span>
		</div>
	{/if}

	<div class="flex justify-between mt-6">
<!--		<button -->
<!--			class="btn variant-ghost-surface" -->
<!--			on:click={prevStep}-->
<!--			disabled={loading || onboardingComplete}-->
<!--		>Back</button>-->

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
