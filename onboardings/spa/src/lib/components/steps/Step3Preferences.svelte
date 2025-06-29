<script>
	import { onboarding } from '$lib/stores/onboarding';
	import { onMount } from 'svelte';
	export let nextStep;
	export let prevStep;

	let theme = 'light';
	let enableNotifications = false;
	let loading = false;
	let error = null;

	// Load existing data if available
	onMount(() => {
		const unsubscribe = onboarding.subscribe(state => {
			if (state.data && state.data.preferences) {
				theme = state.data.preferences.theme || 'light';
				enableNotifications = state.data.preferences.enableNotifications || false;
			}
		});

		return unsubscribe;
	});

	async function handleSubmit() {
		try {
			loading = true;
			error = null;

			// Save the preferences data
			await onboarding.saveStep(2, { theme, enableNotifications });
			nextStep();
		} catch (err) {
			error = err.message || 'Failed to save preferences';
			console.error('Error saving preferences:', err);
		} finally {
			loading = false;
		}
	}
</script>

<div class="space-y-6">
	<h2 class="h2">Preferences</h2>

	{#if error}
		<div class="alert variant-filled-error">
			<span>{error}</span>
		</div>
	{/if}

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
			<button 
				type="button" 
				class="btn variant-ghost-surface" 
				on:click={prevStep}
				disabled={loading}
			>Back</button>
			<button 
				type="submit" 
				class="btn variant-filled-primary"
				disabled={loading}
			>
				{#if loading}
					<span class="spinner-border spinner-border-sm mr-2" role="status" aria-hidden="true"></span>
				{/if}
				Continue
			</button>
		</div>
	</form>
</div>
