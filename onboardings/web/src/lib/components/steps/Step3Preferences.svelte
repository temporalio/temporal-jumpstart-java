<script>
	export let nextStep;
	export let prevStep;

	let theme = 'light';
	let enableNotifications = false;

	function handleSubmit() {
		// Save preferences
		nextStep();
	}
</script>

<div class="space-y-6">
	<h2 class="h2">Preferences</h2>
	<form on:submit|preventDefault={handleSubmit} class="space-y-4">
		<label class="label">
			<span>Theme Preference</span>
			<select class="select" bind:value={theme}>
				<option value="light">Light</option>
				<option value="dark">Dark</option>
				<option value="auto">System Default</option>
			</select>
		</label>
		<label class="label flex items-center space-x-2">
			<input class="checkbox" type="checkbox" bind:checked={enableNotifications} />
			<span>Enable notifications</span>
		</label>

		<div class="flex justify-between mt-6">
			<button type="button" class="btn variant-ghost-surface" on:click={prevStep}>Back</button>
			<button type="submit" class="btn variant-filled-primary">Continue</button>
		</div>
	</form>
</div>
