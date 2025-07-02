<script>
	import { account } from '$lib/stores/account.js';
	import { onMount } from 'svelte';
	import { page } from '$app/stores';
	let userId = $page.params.user_id;

	export let currentStep;
	export let nextStep;
	export let prevStep;

	let name = '';
	let ssn = '';
	let birthdate = '';
	let loading = false;
	let error = null;

	// Load existing data if available
	onMount(() => {
		const unsubscribe = account.subscribe(state => {
			// First try to get from personalInfo if already saved
			if (state.data) {
				name = state.data.name || '';
				ssn = state.data.ssn || '';
				birthdate = state.data.birthdate || '';
			}

		});

		return unsubscribe;
	});

	async function handleSubmit() {
		try {
			loading = true;
			error = null;
			console.log('Saving personal info...', { name, ssn, birthdate });
			// Save the personal info data
			await account.matchClient({ id: userId, ssn, birthdate, name });
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
			<span>Social Security Number</span>
			<input class="input" type="password" placeholder="Enter your SSN" bind:value={ssn} required />
		</label>
		<label class="label">
			<span>Birthdate</span>
			<input class="input" type="date" placeholder="Enter your birthdate" bind:value={birthdate} required />
		</label>

		<div class="flex justify-between mt-6">
<!--			<button -->
<!--				type="button" -->
<!--				class="btn variant-ghost-surface" -->
<!--				on:click={prevStep}-->
<!--				disabled={loading}-->
<!--			>Back</button>-->
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
