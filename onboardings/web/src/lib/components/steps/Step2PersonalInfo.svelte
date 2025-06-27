<script>
	export let nextStep;
	export let prevStep;

	let name = '';
	let email = '';

	function handleSubmit() {
		// Validate form data here
		nextStep();
	}
</script>

<div class="space-y-6">
	<h2 class="h2">Personal Information</h2>
	<form on:submit|preventDefault={handleSubmit} class="space-y-4">
		<label class="label">
			<span>Full Name</span>
			<input class="input" type="text" placeholder="Enter your full name" bind:value={name} required />
		</label>
		<label class="label">
			<span>Email</span>
			<input class="input" type="email" placeholder="Enter your email" bind:value={email} required />
		</label>

		<div class="flex justify-between mt-6">
			<button type="button" class="btn variant-ghost-surface" on:click={prevStep}>Back</button>
			<button type="submit" class="btn variant-filled-primary">Continue</button>
		</div>
	</form>
</div>
