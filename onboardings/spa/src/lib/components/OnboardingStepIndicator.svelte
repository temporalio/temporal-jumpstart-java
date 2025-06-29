<script>
	export let steps = [];
	export let currentStepIndex = 0;
</script>

<div class="flex flex-col space-y-4 w-full">
	{#each steps as step, i}
		<div class="flex items-center">
			<div class="flex items-center justify-center w-10 h-10 rounded-full 
				{i === currentStepIndex ? 'bg-primary-500' : 
				i < currentStepIndex ? 'bg-success-500' : 'bg-surface-300'} 
				text-on-{i === currentStepIndex ? 'primary' : 
				i < currentStepIndex ? 'success' : 'surface'}-token">
				{i < currentStepIndex ? '✓' : step.id}
			</div>
			<div class="ml-4">
				<div class="font-semibold {i === currentStepIndex ? 'text-primary-500' : ''}">{
					step.title
				}</div>
				<div class="text-sm text-surface-600">{step.description}</div>
			</div>
		</div>
		{#if i < steps.length - 1}
			<div class="ml-5 h-12 w-0.5 {i < currentStepIndex ? 'bg-success-500' : 'bg-surface-300'}"></div>
		{/if}
	{/each}
</div>
