<script lang="ts">
	export let currentStep: number
	export let nextStep: () => void
	export let prevStep: () => void
	export let data: any = {}

	let shortTermGoals = data.shortTermGoals || ''
	let longTermGoals = data.longTermGoals || ''
	let retirementAge = data.retirementAge || 0
	let retirementIncome = data.retirementIncome || 0
	let educationFunding = data.educationFunding || ''
	let charitableGiving = data.charitableGiving || ''

	function handleSubmit() {
		data.shortTermGoals = shortTermGoals
		data.longTermGoals = longTermGoals
		data.retirementAge = retirementAge
		data.retirementIncome = retirementIncome
		data.educationFunding = educationFunding
		data.charitableGiving = charitableGiving
		nextStep()
	}
</script>

<div class="space-y-6">
	<h2 class="h2">Financial Goals and Objectives</h2>
	
	<form on:submit|preventDefault={handleSubmit} class="space-y-4">
		<label class="label">
			<span>Short-term Financial Goals (1-5 years)</span>
			<textarea class="textarea" bind:value={shortTermGoals} placeholder="Emergency fund, home purchase, debt payoff, etc."></textarea>
		</label>
		
		<label class="label">
			<span>Long-term Financial Goals (5+ years)</span>
			<textarea class="textarea" bind:value={longTermGoals} placeholder="Retirement, wealth building, major purchases, etc."></textarea>
		</label>
		
		<div class="space-y-4">
			<h3 class="h3">Retirement Planning</h3>
			<div class="grid grid-cols-2 gap-4">
				<label class="label">
					<span>Desired Retirement Age</span>
					<input class="input" type="number" min="50" max="80" bind:value={retirementAge} />
				</label>
				<label class="label">
					<span>Desired Annual Retirement Income ($)</span>
					<input class="input" type="number" min="0" bind:value={retirementIncome} />
				</label>
			</div>
		</div>
		
		<label class="label">
			<span>Education Funding Plans</span>
			<textarea class="textarea" bind:value={educationFunding} placeholder="College savings, 529 plans, etc."></textarea>
		</label>
		
		<label class="label">
			<span>Charitable Giving Intentions</span>
			<textarea class="textarea" bind:value={charitableGiving} placeholder="Current giving, planned donations, charitable goals"></textarea>
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