<script lang="ts">
	export let currentStep: number
	export let nextStep: () => void
	export let prevStep: () => void
	export let data: any = {}

	let bankAccounts = data.bankAccounts || 0
	let investmentAccounts = data.investmentAccounts || 0
	let retirementAccounts = data.retirementAccounts || 0
	let realEstate = data.realEstate || 0
	let otherAssets = data.otherAssets || ''
	let mortgages = data.mortgages || 0
	let loans = data.loans || 0
	let creditCardDebt = data.creditCardDebt || 0

	function handleSubmit() {
		data.bankAccounts = bankAccounts
		data.investmentAccounts = investmentAccounts
		data.retirementAccounts = retirementAccounts
		data.realEstate = realEstate
		data.otherAssets = otherAssets
		data.mortgages = mortgages
		data.loans = loans
		data.creditCardDebt = creditCardDebt
		nextStep()
	}
</script>

<div class="space-y-6">
	<h2 class="h2">Financial Assets and Liabilities</h2>
	
	<form on:submit|preventDefault={handleSubmit} class="space-y-6">
		<div class="space-y-4">
			<h3 class="h3">Assets</h3>
			
			<label class="label">
				<span>Bank Account Balances ($)</span>
				<input class="input" type="number" min="0" bind:value={bankAccounts} />
			</label>
			
			<label class="label">
				<span>Investment Accounts ($)</span>
				<input class="input" type="number" min="0" bind:value={investmentAccounts} />
			</label>
			
			<label class="label">
				<span>Retirement Accounts ($)</span>
				<input class="input" type="number" min="0" bind:value={retirementAccounts} />
			</label>
			
			<label class="label">
				<span>Real Estate Holdings ($)</span>
				<input class="input" type="number" min="0" bind:value={realEstate} />
			</label>
			
			<label class="label">
				<span>Other Significant Assets</span>
				<textarea class="textarea" bind:value={otherAssets} placeholder="Describe other valuable assets"></textarea>
			</label>
		</div>
		
		<div class="space-y-4">
			<h3 class="h3">Liabilities</h3>
			
			<label class="label">
				<span>Outstanding Mortgages ($)</span>
				<input class="input" type="number" min="0" bind:value={mortgages} />
			</label>
			
			<label class="label">
				<span>Other Loans ($)</span>
				<input class="input" type="number" min="0" bind:value={loans} />
			</label>
			
			<label class="label">
				<span>Credit Card Balances ($)</span>
				<input class="input" type="number" min="0" bind:value={creditCardDebt} />
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