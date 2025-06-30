<script>
	import { registrations } from '$lib/stores/registrations';

	// Form data
	let email = '';
	let loading = false;
	let error = null;
	let success = false;

	// Function to handle form submission
	async function handleApply() {
		if (!email) return;

		try {
			loading = true;
			error = null;

			// Initialize a new account process
			let {id: registrationId} = await registrations.register({ email });

			// Set the email in the store
			// await account.saveStep(0, { email });

			// Redirect to the account process
			window.location.href = '/registrations/' + registrationId;
		} catch (err) {
			error = err.message || 'Failed to  register';
			console.error('Application error:', err);
		} finally {
			loading = false;
		}
	}
</script>

<div class="container h-full mx-auto p-4">
	<div class="card w-full min-h-[90vh] grid grid-cols-1 md:grid-cols-2 overflow-hidden">
		<!-- Left column: Form section -->
		<div class="p-8 flex flex-col justify-center">
			<div class="max-w-lg mx-auto w-full">
				<h1 class="h1 mb-2">Start Your Journey</h1>
				<p class="text-xl mb-8">Apply now to unlock the full potential of Temporal</p>

				{#if error}
					<div class="alert variant-filled-error mb-4">
						<span>{error}</span>
					</div>
				{/if}

				<form on:submit|preventDefault={handleApply} class="space-y-6">
					<label class="label">
						<span>Email</span>
						<input 
							class="input" 
							type="email"
							placeholder="Enter your email address"
							bind:value={email}
							required 
						/>
					</label>

					<div class="flex flex-col space-y-4">
						<button 
							type="submit" 
							class="btn variant-filled-primary text-lg py-3"
							disabled={loading || !email}
						>
							{#if loading}
								<span class="spinner-border spinner-border-sm mr-2" role="status" aria-hidden="true"></span>
							{/if}
							Apply
						</button>
						<p class="text-sm text-center">By applying, you agree to our Terms of Service and Privacy Policy</p>
					</div>
				</form>

				<div class="mt-12">
					<h3 class="h4 mb-4">Why join our platform?</h3>
					<ul class="list space-y-2">
						<li class="flex">
							<div class="mr-2">✓</div>
							<div>Build resilient applications with durable execution</div>
						</li>
						<li class="flex">
							<div class="mr-2">✓</div>
							<div>Scale seamlessly with our distributed architecture</div>
						</li>
						<li class="flex">
							<div class="mr-2">✓</div>
							<div>Access expert support and comprehensive documentation</div>
						</li>
					</ul>
				</div>
			</div>
		</div>

		<!-- Right column: Hero image section -->
		<div class="hidden md:block bg-gradient-to-br from-primary-900 to-tertiary-900">
			<div class="h-full flex flex-col justify-center items-center p-8 text-white">
				<img 
					src="https://placehold.co/600x400/1e293b/ffffff?text=Temporal+Durable+Wealth"
					alt="Temporal Wealth"
					class="w-3/4 max-w-lg rounded-lg shadow-xl mb-8" 
				/>
				<h2 class="h2 text-center mb-4">Manage Your Durability</h2>
				<p class="text-lg text-center max-w-md">
					Temporal provides a developer-first platform for writing reliable applications without worrying about timeouts, retries, or infrastructure failures.

				</p>
				<p>THAT will make you wealthy :)</p>
			</div>
		</div>
	</div>
</div>
