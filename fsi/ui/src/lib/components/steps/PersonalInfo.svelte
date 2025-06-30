<script>
	import { application } from '$lib/stores/application.js';
	import { onMount } from 'svelte';
	export let nextStep;
	export let prevStep;

	let name = '';
	let email = '';
	let loading = false;
	let error = null;

	// Load existing data if available
	onMount(() => {
		const unsubscribe = application.subscribe(state => {
			// First try to get from personalInfo if already saved
			if (state.data && state.data.personalInfo) {
				name = state.data.personalInfo.name || '';
				email = state.data.personalInfo.email || '';
			}

			// If email is still empty, try to get from initial capture
			if (!email && state.data && state.data.email) {
				email = state.data.email;
			}
		});

		return unsubscribe;
	});

	async function handleSubmit() {
		try {
			loading = true;
			error = null;

			// Save the personal info data
			await application.saveStep(1, { name, email });
			nextStep();
		} catch (err) {
			error = err.message || 'Failed to save personal information';
			console.error('Error saving personal info:', err);
		} finally {
			loading = false;
		}
	}
</script>

<div class="space-y-6">
	<h2 class="h2">Personal Information</h2>

	{#if error}
		<div class="alert variant-filled-error">
			<span>{error}</span>
		</div>
	{/if}

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
