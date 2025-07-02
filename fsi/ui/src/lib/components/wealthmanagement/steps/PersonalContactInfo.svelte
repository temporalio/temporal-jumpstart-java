<script lang="ts">
	export let currentStep: number
	export let nextStep: () => void
	export let prevStep: () => void
	export let data: any = {}

	let name = data.name || ''
	let dateOfBirth = data.dateOfBirth || ''
	let address = data.address || ''
	let phone = data.phone || ''
	let email = data.email || ''
	let maritalStatus = data.maritalStatus || ''
	let dependents = data.dependents || 0

	function handleSubmit() {
		data.name = name
		data.dateOfBirth = dateOfBirth
		data.address = address
		data.phone = phone
		data.email = email
		data.maritalStatus = maritalStatus
		data.dependents = dependents
		nextStep()
	}
</script>

<div class="space-y-6">
	<h2 class="h2">Personal and Contact Information</h2>
	
	<form on:submit|preventDefault={handleSubmit} class="space-y-4">
		<label class="label">
			<span>Full Legal Name</span>
			<input class="input" type="text" bind:value={name} required />
		</label>
		
		<label class="label">
			<span>Date of Birth</span>
			<input class="input" type="date" bind:value={dateOfBirth} required />
		</label>
		
		<label class="label">
			<span>Residential Address</span>
			<textarea class="textarea" bind:value={address} required></textarea>
		</label>
		
		<div class="grid grid-cols-2 gap-4">
			<label class="label">
				<span>Phone Number</span>
				<input class="input" type="tel" bind:value={phone} required />
			</label>
			
			<label class="label">
				<span>Email Address</span>
				<input class="input" type="email" bind:value={email} required />
			</label>
		</div>
		
		<div class="grid grid-cols-2 gap-4">
			<label class="label">
				<span>Marital Status</span>
				<select class="select" bind:value={maritalStatus} required>
					<option value="">Select status</option>
					<option value="single">Single</option>
					<option value="married">Married</option>
					<option value="divorced">Divorced</option>
					<option value="widowed">Widowed</option>
				</select>
			</label>
			
			<label class="label">
				<span>Number of Dependents</span>
				<input class="input" type="number" min="0" bind:value={dependents} />
			</label>
		</div>
		
		<div class="flex justify-between mt-6">
			<button type="button" class="btn variant-ghost-surface" on:click={prevStep} disabled={currentStep === 0}>
				Back
			</button>
			<button type="submit" class="btn variant-filled-primary">
				Continue
			</button>
		</div>
	</form>
</div>