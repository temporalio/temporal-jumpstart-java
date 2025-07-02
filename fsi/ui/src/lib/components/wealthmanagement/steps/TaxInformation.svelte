<script lang="ts">
	export let currentStep: number
	export let nextStep: () => void
	export let prevStep: () => void
	export let data: any = {}

	let filingStatus = data.filingStatus || ''
	let recentTaxReturns = data.recentTaxReturns || ''
	let taxDeferredAccounts = data.taxDeferredAccounts || ''
	let taxLiabilities = data.taxLiabilities || 0
	let taxStrategies = data.taxStrategies || ''

	function handleSubmit() {
		data.filingStatus = filingStatus
		data.recentTaxReturns = recentTaxReturns
		data.taxDeferredAccounts = taxDeferredAccounts
		data.taxLiabilities = taxLiabilities
		data.taxStrategies = taxStrategies
		nextStep()
	}
</script>

<div class="space-y-6">
	<h2 class="h2">Tax Information</h2>
	
	<form on:submit|preventDefault={handleSubmit} class="space-y-4">
		<label class="label">
			<span>Tax Filing Status</span>
			<select class="select" bind:value={filingStatus} required>
				<option value="">Select filing status</option>
				<option value="single">Single</option>
				<option value="married-joint">Married Filing Jointly</option>
				<option value="married-separate">Married Filing Separately</option>
				<option value="head-of-household">Head of Household</option>
				<option value="qualifying-widow">Qualifying Widow(er)</option>
			</select>
		</label>
		
		<label class="label">
			<span>Recent Tax Returns</span>
			<textarea class="textarea" bind:value={recentTaxReturns} placeholder="Describe your recent tax returns and any notable items"></textarea>
		</label>
		
		<label class="label">
			<span>Tax-Deferred Accounts</span>
			<textarea class="textarea" bind:value={taxDeferredAccounts} placeholder="List 401(k), IRA, and other tax-deferred accounts"></textarea>
		</label>
		
		<label class="label">
			<span>Current Tax Liabilities ($)</span>
			<input class="input" type="number" min="0" bind:value={taxLiabilities} />
		</label>
		
		<label class="label">
			<span>Tax Planning Strategies</span>
			<textarea class="textarea" bind:value={taxStrategies} placeholder="Describe any current tax planning strategies or concerns"></textarea>
		</label>
		
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