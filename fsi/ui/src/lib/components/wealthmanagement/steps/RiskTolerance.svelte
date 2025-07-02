<script lang="ts">
	export let currentStep: number
	export let nextStep: () => void
	export let prevStep: () => void
	export let data: any = {}

	let riskTolerance = data.riskTolerance || ''
	let investmentExperience = data.investmentExperience || ''
	let investmentPreferences = data.investmentPreferences || ''
	let investmentRestrictions = data.investmentRestrictions || ''
	let timeHorizon = data.timeHorizon || ''

	function handleSubmit() {
		data.riskTolerance = riskTolerance
		data.investmentExperience = investmentExperience
		data.investmentPreferences = investmentPreferences
		data.investmentRestrictions = investmentRestrictions
		data.timeHorizon = timeHorizon
		nextStep()
	}
</script>

<div class="space-y-6">
	<h2 class="h2">Risk Tolerance and Investment Preferences</h2>
	
	<form on:submit|preventDefault={handleSubmit} class="space-y-4">
		<label class="label">
			<span>Investment Risk Tolerance</span>
			<select class="select" bind:value={riskTolerance} required>
				<option value="">Select risk tolerance</option>
				<option value="conservative">Conservative - Prefer stability over growth</option>
				<option value="moderate">Moderate - Balanced approach to risk and return</option>
				<option value="aggressive">Aggressive - Willing to accept higher risk for potential higher returns</option>
			</select>
		</label>
		
		<label class="label">
			<span>Investment Experience</span>
			<select class="select" bind:value={investmentExperience} required>
				<option value="">Select experience level</option>
				<option value="beginner">Beginner - Limited investment experience</option>
				<option value="intermediate">Intermediate - Some investment experience</option>
				<option value="advanced">Advanced - Extensive investment experience</option>
				<option value="professional">Professional - Work in finance/investing</option>
			</select>
		</label>
		
		<label class="label">
			<span>Investment Time Horizon</span>
			<select class="select" bind:value={timeHorizon} required>
				<option value="">Select time horizon</option>
				<option value="short">Short-term (1-3 years)</option>
				<option value="medium">Medium-term (3-10 years)</option>
				<option value="long">Long-term (10+ years)</option>
			</select>
		</label>
		
		<label class="label">
			<span>Preferred Investment Strategies</span>
			<textarea class="textarea" bind:value={investmentPreferences} placeholder="Growth stocks, dividend investing, index funds, real estate, etc."></textarea>
		</label>
		
		<label class="label">
			<span>Investment Restrictions or Preferences</span>
			<textarea class="textarea" bind:value={investmentRestrictions} placeholder="ESG investing, sector restrictions, ethical considerations, etc."></textarea>
		</label>
		
		<div class="flex justify-between mt-6">
			<button type="button" class="btn variant-ghost-surface" on:click={prevStep}>
				Back
			</button>
			<button type="submit" class="btn variant-filled-primary">
				Complete Application
			</button>
		</div>
	</form>
</div>