<script>
	export let prevStep;
</script>

<div class="space-y-6 text-center">
	<div class="text-5xl mb-4">🎉</div>
	<h2 class="h2">All Done!</h2>
	<p>Congratulations! You have completed the onboarding process.</p>
	<p>You can now start using all the features of our application.</p>

	<div class="flex justify-between mt-6">
		<button class="btn variant-ghost-surface" on:click={prevStep}>Back</button>
		<a href="/dashboard" class="btn variant-filled-success">Go to Dashboard</a>
	</div>
</div>
