<script lang="ts">
	export let currentStep: number
	export let nextStep: () => void
	export let prevStep: () => void
	export let data: any = {}

	let employmentStatus = data.employmentStatus || ''
	let employer = data.employer || ''
	let jobTitle = data.jobTitle || ''
	let annualIncome = data.annualIncome || 0
	let incomeSources = data.incomeSources || ''
	let anticipatedChanges = data.anticipatedChanges || ''

	function handleSubmit() {
		data.employmentStatus = employmentStatus
		data.employer = employer
		data.jobTitle = jobTitle
		data.annualIncome = annualIncome
		data.incomeSources = incomeSources
		data.anticipatedChanges = anticipatedChanges
		nextStep()
	}
</script>

<div class="space-y-6">
	<h2 class="h2">Employment and Income Details</h2>
	
	<form on:submit|preventDefault={handleSubmit} class="space-y-4">
		<label class="label">
			<span>Employment Status</span>
			<select class="select" bind:value={employmentStatus} required>
				<option value="">Select status</option>
				<option value="employed">Employed</option>
				<option value="self-employed">Self-employed</option>
				<option value="unemployed">Unemployed</option>
				<option value="retired">Retired</option>
				<option value="student">Student</option>
			</select>
		</label>
		
		<div class="grid grid-cols-2 gap-4">
			<label class="label">
				<span>Employer</span>
				<input class="input" type="text" bind:value={employer} />
			</label>
			
			<label class="label">
				<span>Job Title</span>
				<input class="input" type="text" bind:value={jobTitle} />
			</label>
		</div>
		
		<label class="label">
			<span>Annual Income ($)</span>
			<input class="input" type="number" min="0" bind:value={annualIncome} required />
		</label>
		
		<label class="label">
			<span>Sources of Income</span>
			<textarea class="textarea" bind:value={incomeSources} placeholder="Salary, investments, rental income, etc."></textarea>
		</label>
		
		<label class="label">
			<span>Anticipated Changes in Employment or Income</span>
			<textarea class="textarea" bind:value={anticipatedChanges} placeholder="Describe any expected changes"></textarea>
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