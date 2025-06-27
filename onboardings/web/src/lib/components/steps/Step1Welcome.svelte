<script>
	export let nextStep;
</script>

<div class="space-y-6">
	<h2 class="h2">Welcome</h2>
	<p>
		Welcome to our onboarding process. This will help you get started with our application.
		Follow the steps to configure your account and preferences.
	</p>
	<div class="flex justify-end mt-4">
		<button class="btn variant-filled-primary" on:click={nextStep}>Get Started</button>
	</div>
</div>
