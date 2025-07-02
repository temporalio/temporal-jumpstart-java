<script lang="ts">
	export let currentStep: number
	export let nextStep: () => void
	export let prevStep: () => void
	export let data: any = {}

	let hasWill = data.hasWill || false
	let willDetails = data.willDetails || ''
	let hasTrusts = data.hasTrusts || false
	let trustDetails = data.trustDetails || ''
	let hasPowerOfAttorney = data.hasPowerOfAttorney || false
	let powerOfAttorneyDetails = data.powerOfAttorneyDetails || ''
	let beneficiaryDesignations = data.beneficiaryDesignations || ''

	function handleSubmit() {
		data.hasWill = hasWill
		data.willDetails = willDetails
		data.hasTrusts = hasTrusts
		data.trustDetails = trustDetails
		data.hasPowerOfAttorney = hasPowerOfAttorney
		data.powerOfAttorneyDetails = powerOfAttorneyDetails
		data.beneficiaryDesignations = beneficiaryDesignations
		nextStep()
	}
</script>

<div class="space-y-6">
	<h2 class="h2">Estate Planning Documents</h2>
	
	<form on:submit|preventDefault={handleSubmit} class="space-y-6">
		<div class="space-y-4">
			<h3 class="h3">Will</h3>
			<label class="flex items-center space-x-2">
				<input class="checkbox" type="checkbox" bind:checked={hasWill} />
				<span>I have a will</span>
			</label>
			{#if hasWill}
				<label class="label">
					<span>Will Details</span>
					<textarea class="textarea" bind:value={willDetails} placeholder="Describe your will and any recent updates"></textarea>
				</label>
			{/if}
		</div>
		
		<div class="space-y-4">
			<h3 class="h3">Trusts</h3>
			<label class="flex items-center space-x-2">
				<input class="checkbox" type="checkbox" bind:checked={hasTrusts} />
				<span>I have trusts</span>
			</label>
			{#if hasTrusts}
				<label class="label">
					<span>Trust Details</span>
					<textarea class="textarea" bind:value={trustDetails} placeholder="Describe your trusts"></textarea>
				</label>
			{/if}
		</div>
		
		<div class="space-y-4">
			<h3 class="h3">Power of Attorney</h3>
			<label class="flex items-center space-x-2">
				<input class="checkbox" type="checkbox" bind:checked={hasPowerOfAttorney} />
				<span>I have power of attorney documents</span>
			</label>
			{#if hasPowerOfAttorney}
				<label class="label">
					<span>Power of Attorney Details</span>
					<textarea class="textarea" bind:value={powerOfAttorneyDetails} placeholder="Describe your power of attorney arrangements"></textarea>
				</label>
			{/if}
		</div>
		
		<div class="space-y-4">
			<h3 class="h3">Beneficiary Designations</h3>
			<label class="label">
				<span>Beneficiary Designations on Financial Accounts</span>
				<textarea class="textarea" bind:value={beneficiaryDesignations} placeholder="List beneficiaries on retirement accounts, insurance policies, etc."></textarea>
			</label>
		</div>
		
		<div class="flex justify-between mt-6">
			<button type="button" class="btn variant-ghost-surface" on:click={prevStep}>
				Back
			</button>
			<button type="submit" class="btn variant-filled-primary">
				Continue
			</button>
		</div>
	</form>
</div>