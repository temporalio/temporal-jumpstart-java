<script lang="ts">
	export let currentStep: number
	export let nextStep: () => void
	export let prevStep: () => void
	export let data: any = {}

	let lifeInsurance = data.lifeInsurance || { coverage: 0, beneficiaries: '' }
	let healthInsurance = data.healthInsurance || { coverage: 0, provider: '' }
	let disabilityInsurance = data.disabilityInsurance || { coverage: 0, provider: '' }
	let longTermCareInsurance = data.longTermCareInsurance || { coverage: 0, provider: '' }

	function handleSubmit() {
		data.lifeInsurance = lifeInsurance
		data.healthInsurance = healthInsurance
		data.disabilityInsurance = disabilityInsurance
		data.longTermCareInsurance = longTermCareInsurance
		nextStep()
	}
</script>

<div class="space-y-6">
	<h2 class="h2">Insurance Coverage</h2>
	
	<form on:submit|preventDefault={handleSubmit} class="space-y-6">
		<div class="space-y-4">
			<h3 class="h3">Life Insurance</h3>
			<div class="grid grid-cols-2 gap-4">
				<label class="label">
					<span>Coverage Amount ($)</span>
					<input class="input" type="number" min="0" bind:value={lifeInsurance.coverage} />
				</label>
				<label class="label">
					<span>Beneficiaries</span>
					<input class="input" type="text" bind:value={lifeInsurance.beneficiaries} />
				</label>
			</div>
		</div>
		
		<div class="space-y-4">
			<h3 class="h3">Health Insurance</h3>
			<div class="grid grid-cols-2 gap-4">
				<label class="label">
					<span>Coverage Amount ($)</span>
					<input class="input" type="number" min="0" bind:value={healthInsurance.coverage} />
				</label>
				<label class="label">
					<span>Provider</span>
					<input class="input" type="text" bind:value={healthInsurance.provider} />
				</label>
			</div>
		</div>
		
		<div class="space-y-4">
			<h3 class="h3">Disability Insurance</h3>
			<div class="grid grid-cols-2 gap-4">
				<label class="label">
					<span>Coverage Amount ($)</span>
					<input class="input" type="number" min="0" bind:value={disabilityInsurance.coverage} />
				</label>
				<label class="label">
					<span>Provider</span>
					<input class="input" type="text" bind:value={disabilityInsurance.provider} />
				</label>
			</div>
		</div>
		
		<div class="space-y-4">
			<h3 class="h3">Long-term Care Insurance</h3>
			<div class="grid grid-cols-2 gap-4">
				<label class="label">
					<span>Coverage Amount ($)</span>
					<input class="input" type="number" min="0" bind:value={longTermCareInsurance.coverage} />
				</label>
				<label class="label">
					<span>Provider</span>
					<input class="input" type="text" bind:value={longTermCareInsurance.provider} />
				</label>
			</div>
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